package com.chatwing.whitelabel.managers;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.arasthel.asyncjob.AsyncJob;
import com.chatwing.whitelabel.contentproviders.ChatWingContentProvider;
import com.chatwing.whitelabel.events.SyncUnreadEvent;
import com.chatwing.whitelabel.modules.ForApplication;
import com.chatwing.whitelabel.pojos.ChatBox;
import com.chatwing.whitelabel.pojos.responses.UnreadCountResponse;
import com.chatwing.whitelabel.tables.ChatBoxTable;
import com.chatwing.whitelabel.utils.LogUtils;
import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

/**
 * Created by steve on 14/04/2015.
 */
public class ChatboxUnreadDownloadManager {
    @Inject
    @ForApplication
    Context mContext;
    @Inject
    UserManager mUserManager;
    @Inject
    ApiManager mApiManager;
    @Inject
    Bus mBus;
    @Inject
    CurrentChatBoxManager mCurrentChatboxManager;

    private static boolean isRunning;

    public synchronized void downloadUnread() {
        LogUtils.v("downloadUnread " + isRunning + ":" + mUserManager.getCurrentUser());
        if (isRunning || mUserManager.getCurrentUser() == null) {
            return;
        }
        final ArrayList<Integer> unAckChatboxIds = new ArrayList<>();
        AsyncJob.OnBackgroundJob backgroundJob = new AsyncJob.OnBackgroundJob() {
            @Override
            public void doOnBackground() {
                isRunning = true;
                Cursor cursor = null;
                try {
                    cursor = mContext.getContentResolver().query(ChatWingContentProvider.getChatBoxesUri(),
                            new String[]{ChatBoxTable._ID},
                            null,
                            null,
                            null);
                    boolean has = cursor.moveToFirst();
                    ExecutorService executorService = Executors.newFixedThreadPool(4);
                    final Map<Integer, Integer> chatboxIDUnreadMap = new HashMap<Integer, Integer>();
                    while (has) {
                        final int chatboxID = cursor.getInt(0);

                        AsyncJob.OnBackgroundJob fetchUnreadJob = new AsyncJob.OnBackgroundJob() {
                            @Override
                            public void doOnBackground() {
                                try {
                                    UnreadCountResponse unreadCountForChatbox = mApiManager
                                            .getUnreadCountForChatbox(mUserManager.getCurrentUser(),
                                                    chatboxID);

                                    int count = unreadCountForChatbox.getData().getCount();
                                    if (!unreadCountForChatbox.getData().hasChatboxFirstAck()) {
                                        unAckChatboxIds.add(chatboxID);
                                    }
                                    LogUtils.v("Unread Count = " + count + " since > 0? " + unreadCountForChatbox.getData().hasChatboxFirstAck());
                                    chatboxIDUnreadMap.put(chatboxID, count);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    LogUtils.v("Download unread task FAILED " + chatboxID);
                                }
                            }
                        };

                        //Execute each fetch job
                        AsyncJob.doInBackground(fetchUnreadJob, executorService);

//                        LogUtils.v("Download unread task added");
                        has = cursor.moveToNext();
                    }
                    executorService.shutdown();
//                    LogUtils.v("Download unread waiting to shutdown service");
                    try {
                        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                    } catch (InterruptedException e) {
                        LogUtils.e(e);
                    }
//                    LogUtils.v("Download unread DOWNLOADED, service SHUTDOWN");

                    //Insert to database
                    insertToDatabase(chatboxIDUnreadMap);

                    // This toast should show a difference of 1000ms between calls
                    AsyncJob.doOnMainThread(new AsyncJob.OnMainThreadJob() {
                        @Override
                        public void doInUIThread() {
                            mBus.post(new SyncUnreadEvent(unAckChatboxIds));
//                            LogUtils.v("Download unread: Synced Done");
                        }
                    });

                    isRunning = false;
                } finally {
                    if (cursor != null && !cursor.isClosed()) {
                        cursor.close();
                    }
                }
            }
        };


        AsyncJob.doInBackground(backgroundJob);
    }

    private void insertToDatabase(Map<Integer, Integer> chatboxIDUnreadMap) {
        ArrayList<ContentProviderOperation> batch = new ArrayList<>();
        Set<Integer> chatboxIDs = chatboxIDUnreadMap.keySet();
        for (Integer chatboxID : chatboxIDs) {
            ContentValues contentValues = new ContentValues();
            ChatBox currentChatBox = mCurrentChatboxManager.getCurrentChatBox();
            if (currentChatBox != null && currentChatBox.getId() == chatboxID) {
                //We ignore updating unread count for opening chatbox
                contentValues.put(ChatBoxTable.UNREAD_COUNT, 0);
            } else {
                contentValues.put(ChatBoxTable.UNREAD_COUNT, chatboxIDUnreadMap.get(chatboxID));
            }
            batch.add(ContentProviderOperation.newUpdate(ChatWingContentProvider.getChatBoxWithIdUri(chatboxID))
                    .withValues(contentValues)
                    .build());
        }

        try {
            mContext.getContentResolver().applyBatch(ChatWingContentProvider.AUTHORITY, batch);
            LogUtils.v("Download unread task INSERTED");
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.v("Download unread task FAILED batch");
        }

    }

    public static boolean isRunning() {
        return isRunning;
    }
}
