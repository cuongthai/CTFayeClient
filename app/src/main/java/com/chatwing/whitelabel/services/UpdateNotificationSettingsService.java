package com.chatwing.whitelabel.services;

import android.content.Intent;

import com.chatwing.whitelabel.events.UpdateSubscriptionEvent;
import com.chatwing.whitelabel.pojos.User;
import com.chatwing.whitelabel.pojos.responses.SubscriptionResponse;


/**
 * Created by steve on 23/01/2015.
 */
public class UpdateNotificationSettingsService extends BaseIntentService {

    public static final String CHATBOX_ID = "CHATBOX_ID";
    public static final String CONVERSATION_ID = "CONVERSATION_ID";
    public static final String ACTION_SUBSCRIBE = "ACTION_SUBSCRIBE";
    public static final String ACTION_UNSUBSCRIBE = "ACTION_UNSUBSCRIBE";
    public static final String TARGET = "TARGET";
    public static final String TARGET_EMAIL = "email";
    public static final String TARGET_PUSH = "push";

    public UpdateNotificationSettingsService() {
        super("UpdateNotificationSettingsService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        User user = mUserManager.getCurrentUser();
        if (user == null) {
            return;
        }
        int chatboxID = intent.getIntExtra(CHATBOX_ID, 0);
        String conversationID = intent.getStringExtra(CONVERSATION_ID);
        String target = intent.getStringExtra(TARGET);
        String action = intent.getAction();
        try {
            post(UpdateSubscriptionEvent.startedEvent());
            SubscriptionResponse subscriptionResponse;
            if (conversationID == null) {
                subscriptionResponse = mApiManager.
                        updateNotificationSubscription(user, action, chatboxID, target);
            } else {
                subscriptionResponse = mApiManager.
                        updateNotificationSubscription(user, action, conversationID, target);
            }

            storeNotificationSetting(user, conversationID != null
                            ? conversationID
                            : String.valueOf(chatboxID),
                    UpdateNotificationSettingsService.ACTION_SUBSCRIBE.equals(action) ? true : false);

            post(UpdateSubscriptionEvent.succeedEvent(subscriptionResponse, action));
        } catch (Exception e) {
            post(UpdateSubscriptionEvent.failedEvent(e));
        }
    }

    private void storeNotificationSetting(User user, String channel, boolean on) {
        mUserManager.setNotificationSetting(user, channel, on);
    }

    private void post(final UpdateSubscriptionEvent event) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mBus.post(event);
            }
        });
    }
}
