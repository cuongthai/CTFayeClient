package com.chatwing.whitelabel.services;

import android.content.Intent;

import com.chatwing.whitelabel.events.SubscriptionStatusEvent;
import com.chatwing.whitelabel.pojos.responses.SubscriptionStatusResponse;


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
        int chatboxID = intent.getIntExtra(CHATBOX_ID, 0);
        String conversationID = intent.getStringExtra(CONVERSATION_ID);
        try {
            post(SubscriptionStatusEvent.startedEvent());
            SubscriptionStatusResponse subscriptionResponse;
            if (conversationID == null) {
                subscriptionResponse = mApiManager.
                        loadCommunicationSetting(mUserManager.getCurrentUser(), chatboxID);
            } else {
                subscriptionResponse = mApiManager.
                        loadCommunicationSetting(mUserManager.getCurrentUser(), conversationID);
            }
            post(SubscriptionStatusEvent.succeedEvent(subscriptionResponse));
        } catch (Exception e) {
            post(SubscriptionStatusEvent.failedEvent(e));
        }
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
