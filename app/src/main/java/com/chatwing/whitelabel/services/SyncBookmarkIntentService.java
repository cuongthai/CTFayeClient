package com.chatwing.whitelabel.services;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import com.chatwing.whitelabel.contentproviders.ChatWingContentProvider;
import com.chatwing.whitelabel.events.SyncBookmarkEvent;
import com.chatwing.whitelabel.managers.ApiManager;
import com.chatwing.whitelabel.pojos.ChatBox;
import com.chatwing.whitelabel.pojos.LightWeightChatBox;
import com.chatwing.whitelabel.pojos.SyncedBookmark;
import com.chatwing.whitelabel.pojos.responses.BookmarkResponse;
import com.chatwing.whitelabel.tables.ChatBoxTable;
import com.chatwing.whitelabel.tables.SyncedBookmarkTable;
import com.chatwing.whitelabel.utils.LogUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;


/**
 * Created by steve on 21/06/2014.
 */
public class SyncBookmarkIntentService extends BaseIntentService {

    @Inject
    protected ApiManager mApiManager;

    private static boolean sIsInProgress;
    private static final Object sLock = new Object();

    public SyncBookmarkIntentService() {
        super("SyncBookmarkIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        LogUtils.v("Syncing Bookmark");
        setIsInProgress(true);
        post(SyncBookmarkEvent.startedEvent());
        SyncBookmarkEvent result;
        try {
            ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();

            //Load bookmarks from server
            BookmarkResponse bookmarkResponse = mApiManager.loadBookmarks(mUserManager.getCurrentUser());
            SyncedBookmark[] bookmarks = bookmarkResponse.getData();
            /**
             * 1. Get out of sync bookmark
             * 2. Remove all 'synced' bookmark
             * 3. Store bookmarks
             *   3.1: Create chatbox
             *   3.2: Create bookmarks
             * 4. Submit out of sync bookmark to server
             */
            Map<Integer, Integer> chatboxLastReads = getChatboxUnreadCount();

            List<LightWeightChatBox> unSyncedBookmarks = getUnSyncedBookmarks();
            refillUnreadCount(bookmarks, chatboxLastReads);
            fillAddOrUpdateOperationsChatBoxes(batch, bookmarks);
            fillRemoveOperationsSyncedBookmarks(batch);
            fillAddOperationsRemoteBookmarks(batch, bookmarks);

            getContentResolver().applyBatch(ChatWingContentProvider.AUTHORITY, batch);
            getContentResolver().notifyChange(
                    ChatWingContentProvider.getSyncedBookmarksUri(),
                    null);

            /**
             * Kick off services to create unsync bookmarks.
             * This might affect performance if user has a lot of unsync bookmarks on local.
             * But I dont think it's likely to happen.
             * If that happen, we should use thread pool to limit number of requests
             */
            for (LightWeightChatBox lightWeightChatBox : unSyncedBookmarks) {
                CreateBookmarkIntentService.start(getApplicationContext(), lightWeightChatBox);
            }

            result = SyncBookmarkEvent.succeedEvent();
        } catch (Exception e) {
            //Ignore known exceptions
            if (!(e instanceof ApiManager.InvalidIdentityException)) {
                LogUtils.e(e);
            }
            result = SyncBookmarkEvent.failedEvent(e);
        }
        setIsInProgress(false);
        post(result);
    }

    private void refillUnreadCount(SyncedBookmark[] bookmarks,
                                   Map<Integer, Integer> chatboxUnreadCounts) {
        for (SyncedBookmark syncedBookmark : bookmarks) {
            ChatBox chatBox = syncedBookmark.getChatBox();
            if (chatboxUnreadCounts.containsKey(chatBox.getId())) {
                chatBox.setUnreadCount(chatboxUnreadCounts.get(chatBox.getId()));
            }
        }
    }

    private Map<Integer, Integer> getChatboxUnreadCount() {
        Map<Integer, Integer> maps = new HashMap<Integer, Integer>();
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(ChatWingContentProvider.getChatBoxesUri(),
                    new String[]{ChatBoxTable._ID, ChatBoxTable.UNREAD_COUNT},
                    null,
                    null,
                    null);
            boolean hasNext = cursor.moveToFirst();
            while (hasNext) {
                int id = cursor.getInt(cursor.getColumnIndex(ChatBoxTable._ID));
                int unreadCount = cursor.getInt(cursor.getColumnIndex(ChatBoxTable.UNREAD_COUNT));
                LogUtils.v("Get last read " + id + ": unreadCount =" + unreadCount);
                maps.put(id, unreadCount);
                hasNext = cursor.moveToNext();
            }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }


        return maps;
    }

    private void post(final SyncBookmarkEvent event) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mBus.post(event);
            }
        });
    }

    private void fillAddOrUpdateOperationsChatBoxes(ArrayList<ContentProviderOperation> batch,
                                                    SyncedBookmark[] bookmarks) {
        for (SyncedBookmark bookmark : bookmarks) {
            ChatBox chatBox = bookmark.getChatBox();

            if (ChatWingContentProvider.hasChatBoxInDB(
                    getContentResolver(),
                    chatBox.getId())) {
                //We update local chatbox
                //because not all local chatbox has enough information for rendering Bookmarks
                ContentValues chatBoxContentValues = new ContentValues();
                chatBoxContentValues.put(ChatBoxTable.ALIAS, chatBox.getAlias());
                batch.add(ContentProviderOperation
                        .newUpdate(ChatWingContentProvider.getChatBoxWithIdUri(chatBox.getId()))
                        .withValues(chatBoxContentValues)
                        .build());
            } else {
                //FIXME: The same issue with {@link com.chatwing.services.CreateBookmarkIntentService}
                ContentValues chatBoxContentValues = ChatBoxTable.getContentValues(
                        chatBox, " ");
                batch.add(ContentProviderOperation
                        .newInsert(ChatWingContentProvider.getChatBoxesUri())
                        .withValues(chatBoxContentValues)
                        .build());
            }
        }
    }

    private void fillAddOperationsRemoteBookmarks(ArrayList<ContentProviderOperation> batch,
                                                  SyncedBookmark[] bookmarks) {
        for (SyncedBookmark bookmark : bookmarks) {
            bookmark.setIsSynced(true);
            Uri syncedBookmarksUri = ChatWingContentProvider.getSyncedBookmarksUri();
            batch.add(ContentProviderOperation
                    .newInsert(syncedBookmarksUri)
                    .withValues(SyncedBookmarkTable.getContentValues(bookmark))
                    .build());
        }
    }

    private void fillRemoveOperationsSyncedBookmarks(ArrayList<ContentProviderOperation> batch) {
        batch.add(ContentProviderOperation
                .newDelete(ChatWingContentProvider.getSyncedBookmarksUri())
                .withSelection(SyncedBookmarkTable.SYNCED + "=1", null)
                .build());
    }

    private List<LightWeightChatBox> getUnSyncedBookmarks() {
        Cursor cursor = getContentResolver().query(
                ChatWingContentProvider.getSyncedBookmarksUri(),
                new String[]{
                        SyncedBookmarkTable.CHAT_BOX_ID,
                        SyncedBookmarkTable.BOOKMARK_ID,
                        SyncedBookmarkTable.CREATED_DATE,
                        SyncedBookmarkTable.SYNCED,
                        ChatBoxTable.KEY,
                        ChatBoxTable.NAME,
                        ChatBoxTable.DATA,
                        ChatBoxTable.FAYE_CHANNEL
                },
                SyncedBookmarkTable.SYNCED + "=0",
                null,
                null
        );

        List<LightWeightChatBox> lightWeightChatBoxes = new ArrayList<LightWeightChatBox>();
        while (cursor.moveToNext()) {
            SyncedBookmark bookmark = SyncedBookmarkTable.getSyncedBookmark(cursor);
            lightWeightChatBoxes.add(LightWeightChatBox.copyFromChatbox(bookmark.getChatBox()));
        }
        cursor.close();
        return lightWeightChatBoxes;
    }

    private static void setIsInProgress(boolean isInProgress) {
        synchronized (sLock) {
            sIsInProgress = isInProgress;
        }
    }

    public static boolean isInProgress() {
        synchronized (sLock) {
            return sIsInProgress;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSyncManager.removeServiceFromQueue(this);
    }
}
