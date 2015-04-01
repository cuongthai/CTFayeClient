package com.chatwing.whitelabel.services;

import android.content.Intent;

import com.chatwing.whitelabel.events.IgnoreUserEvent;
import com.chatwing.whitelabel.managers.ApiManager;
import com.chatwing.whitelabel.modules.ExtendChatWingModule;
import com.chatwing.whitelabel.pojos.responses.IgnoreUserResponse;
import com.chatwingsdk.modules.ChatWingModule;
import com.chatwingsdk.pojos.User;
import com.chatwingsdk.services.BaseIntentService;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by steve on 30/06/2014.
 */
public class IgnoreUserIntentService extends ExtendBaseIntentService {
    public static final String EXTRA_USER_ID = "EXTRA_USER_ID";
    public static final String EXTRA_USER_TYPE = "EXTRA_USER_TYPE";
    public static final String EXTRA_IGNORED = "EXTRA_MESSAGE_ID";

    @Inject
    ApiManager mApiManager;

    public IgnoreUserIntentService() {
        super("IgnoreUserIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        User currentUser = mUserManager.getCurrentUser();
        if (currentUser == null) {
            return;
        }
        IgnoreUserEvent event;
        try {
            String userId = intent.getStringExtra(EXTRA_USER_ID);
            String userType = intent.getStringExtra(EXTRA_USER_TYPE);
            boolean ignored = intent.getBooleanExtra(EXTRA_IGNORED, false);
            IgnoreUserResponse ignoreUserResponse = mApiManager.ignoreUser(currentUser,
                    userId,
                    userType,
                    ignored);
            if (ignored) {
                currentUser.unignoreUser(ignoreUserResponse.getIgnoreUser());
            } else {
                currentUser.ignoreUser(ignoreUserResponse.getIgnoreUser());
            }
            mUserManager.saveCurrentUser();

            event = new IgnoreUserEvent(ignoreUserResponse);
        } catch (Exception e) {
            event = new IgnoreUserEvent(e);
        }

        post(event);
    }

    private void post(final IgnoreUserEvent event) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mBus.post(event);
            }
        });
    }
}
