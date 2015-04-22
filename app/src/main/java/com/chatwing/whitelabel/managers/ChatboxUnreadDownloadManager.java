package com.chatwing.whitelabel.managers;

import android.content.Context;
import android.database.Cursor;

import com.chatwing.whitelabel.tasks.LoadUnreadTask;
import com.chatwingsdk.contentproviders.ChatWingContentProvider;
import com.chatwingsdk.events.internal.SyncChatboxUnreadComplete;
import com.chatwingsdk.managers.UserManager;
import com.chatwingsdk.modules.ForApplication;
import com.chatwingsdk.tables.ChatBoxTable;
import com.chatwingsdk.utils.LogUtils;
import com.squareup.otto.Bus;

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

    public void downloadUnread() {
        Cursor cursor = mContext.getContentResolver().query(ChatWingContentProvider.getChatBoxesUri(),
                new String[]{ChatBoxTable._ID},
                null,
                null,
                null);
        boolean has = cursor.moveToFirst();
        while (has) {
            int chatboxID = cursor.getInt(0);
            new LoadUnreadTask(mContext, mUserManager, mApiManager).execute(chatboxID);
            LogUtils.v("Download unread task started");
            has = cursor.moveToNext();
        }
        mBus.post(new SyncChatboxUnreadComplete());
    }

}
