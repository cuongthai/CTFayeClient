package com.chatwing.whitelabel.services;

import android.content.Intent;

import com.chatwing.whitelabel.events.DeleteMessageEvent;
import com.chatwing.whitelabel.managers.ApiManager;
import com.chatwing.whitelabel.pojos.responses.DeleteMessageResponse;

import javax.inject.Inject;

/**
 * Created by nguyenthanhhuy on 12/21/13.
 */
public class DeleteMessageIntentService extends ExtendBaseIntentService {
    public static final String EXTRA_CHAT_BOX_ID = "chat_box_id";
    public static final String EXTRA_MESSAGE_ID = "message_id";

    @Inject
    ApiManager mApiManager;

    public DeleteMessageIntentService() {
        super("DeleteMessageIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int chatBoxId = intent.getIntExtra(EXTRA_CHAT_BOX_ID, 0);
        String messageId = intent.getStringExtra(EXTRA_MESSAGE_ID);

        DeleteMessageEvent event;
        try {
            DeleteMessageResponse response = mApiManager.deleteMessage(
                    mUserManager.getCurrentUser(), chatBoxId, messageId);
            event = new DeleteMessageEvent(chatBoxId, messageId, response);
        } catch (Exception e) {
            event = new DeleteMessageEvent(chatBoxId, messageId, e);
        }
        post(event);
    }

    private void post(final DeleteMessageEvent event) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mBus.post(event);
            }
        });
    }
}
