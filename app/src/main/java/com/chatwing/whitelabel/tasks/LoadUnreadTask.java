package com.chatwing.whitelabel.tasks;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import com.chatwing.whitelabel.managers.ApiManager;
import com.chatwing.whitelabel.pojos.responses.LoadOnlineUsersResponse;
import com.chatwingsdk.contentproviders.ChatWingContentProvider;
import com.chatwingsdk.managers.UserManager;
import com.chatwingsdk.tables.ChatBoxTable;
import com.chatwingsdk.tasks.CallbackTask;
import com.chatwingsdk.utils.LogUtils;
import com.squareup.otto.Bus;

import javax.inject.Inject;

public class LoadUnreadTask extends AsyncTask<Integer, Void, Boolean> {
    private final UserManager userManager;
    private final Context context;
    private ApiManager mApiManager;

    public LoadUnreadTask(Context context, UserManager userManager, ApiManager mApiManager) {
        this.context = context;
        this.mApiManager = mApiManager;
        this.userManager = userManager;
    }

    @Override
    protected Boolean doInBackground(Integer... params) {
        Integer chatboxId = params[0];

        try {
            int count = mApiManager.getUnreadCountForChatbox(userManager.getCurrentUser(), chatboxId);

            Uri uri = ChatWingContentProvider.getChatBoxWithIdUri(chatboxId);
            ContentValues contentValues = new ContentValues();
            contentValues.put(ChatBoxTable.UNREAD_COUNT, count);

            context.getContentResolver().update(uri, contentValues, null, null);
            LogUtils.v("Download unread task DOWNLOADED + INSERTED");
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.v("Download unread task FAILED "+chatboxId);
        }
        return null;
    }
}
