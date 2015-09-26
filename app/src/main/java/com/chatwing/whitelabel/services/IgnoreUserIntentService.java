package com.chatwing.whitelabel.services;

import android.content.Intent;

import com.chatwing.whitelabel.events.IgnoreUserEvent;
import com.chatwing.whitelabel.managers.ApiManager;
import com.chatwing.whitelabel.pojos.User;
import com.chatwing.whitelabel.pojos.responses.IgnoreUserResponse;

import javax.inject.Inject;

/**
 * Created by steve on 30/06/2014.
 */
public class IgnoreUserIntentService extends BaseIntentService {
    public static final String EXTRA_USER_ID = "EXTRA_USER_ID";
    public static final String EXTRA_USER_TYPE = "EXTRA_USER_TYPE";
    public static final String EXTRA_REQUEST_IGNORE = "EXTRA_REQUEST_IGNORE";

    @Inject
    protected ApiManager mApiManager;

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
            boolean requestIgnore = intent.getBooleanExtra(EXTRA_REQUEST_IGNORE, true);

            IgnoreUserResponse ignoreUserResponse = mApiManager.ignoreUser(currentUser,
                    userId,
                    userType,
                    requestIgnore);
            if (requestIgnore) {
                currentUser.ignoreUser(ignoreUserResponse.getIgnoreUser());
            } else {
                currentUser.unignoreUser(ignoreUserResponse.getIgnoreUser());
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
