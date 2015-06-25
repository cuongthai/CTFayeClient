package com.chatwing.whitelabel.services;

import android.content.Intent;

import com.chatwing.whitelabel.events.FlagMessageEvent;
import com.chatwing.whitelabel.managers.ApiManager;
import com.chatwing.whitelabel.pojos.User;
import com.chatwing.whitelabel.pojos.responses.FlagMessageResponse;

import javax.inject.Inject;

/**
 * Created by steve on 30/06/2014.
 */
public class FlagMessageIntentService extends ExtendBaseIntentService {
    public static final String EXTRA_MESSAGE_ID = "EXTRA_MESSAGE_ID";

    @Inject
    ApiManager mApiManager;

    public FlagMessageIntentService() {
        super("FlagMessageIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        User currentUser = mUserManager.getCurrentUser();
        if (currentUser == null) {
            return;
        }
        FlagMessageEvent event;
        try {
            String messageID = intent.getStringExtra(EXTRA_MESSAGE_ID);
            FlagMessageResponse flagMessageResponse = mApiManager.flagMessage(currentUser, messageID);
            event = new FlagMessageEvent(flagMessageResponse);
        } catch (Exception e) {
            event = new FlagMessageEvent(e);
        }

        post(event);
    }

    private void post(final FlagMessageEvent event) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mBus.post(event);
            }
        });
    }
}
