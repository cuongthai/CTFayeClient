package com.chatwing.whitelabel.services;

import android.content.Intent;

import com.chatwing.whitelabel.events.SubscriptionStatusEvent;
import com.chatwing.whitelabel.pojos.User;
import com.chatwing.whitelabel.pojos.responses.SubscriptionStatusResponse;
import com.chatwing.whitelabel.utils.LogUtils;

import java.util.Map;


/**
 * Created by steve on 23/01/2015.
 */
public class NotificationStatusIntentService extends BaseIntentService {

    public static final String CHATBOX_ID = "CHATBOX_ID";
    public static final String CONVERSATION_ID = "CONVERSATION_ID";

    public NotificationStatusIntentService() {
        super("NotificationStatusIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        User user = mUserManager.getCurrentUser();
        if (user == null) {
            return;
        }
        int chatboxID = intent.getIntExtra(CHATBOX_ID, 0);
        String conversationID = intent.getStringExtra(CONVERSATION_ID);
        try {
            post(SubscriptionStatusEvent.startedEvent());
            SubscriptionStatusResponse subscriptionResponse;
            if (conversationID == null) {
                subscriptionResponse = mApiManager.
                        loadCommunicationSetting(user, chatboxID);
            } else {
                subscriptionResponse = mApiManager.
                        loadCommunicationSetting(user, conversationID);
            }

            //Sync November 2015, we override push notification setting on local.
            //The reason for this is that faye message doesn't contains notification settings
            //So we have to manage push setting on local
            overrideNotificationSettings(user,
                    subscriptionResponse,
                    chatboxID != 0 ?
                            String.valueOf(chatboxID) :
                            conversationID,
                    chatboxID != 0 ?
                            true : false);

            post(SubscriptionStatusEvent.succeedEvent(subscriptionResponse));
        } catch (Exception e) {
            post(SubscriptionStatusEvent.failedEvent(e));
        }
    }

    private void overrideNotificationSettings(User user,
                                              SubscriptionStatusResponse subscriptionResponse,
                                              String channel,
                                              boolean isChatbox) {
        Map<String, Boolean> data = subscriptionResponse.getData();
        boolean setting = mUserManager.getNotificationSetting(user, channel, isChatbox);
        data.put("push", setting);
    }

    private void post(final SubscriptionStatusEvent event) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mBus.post(event);
            }
        });
    }
}
