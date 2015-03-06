package com.chatwing.whitelabel.services;

import android.content.Intent;

import com.chatwing.whitelabel.managers.ApiManager;
import com.chatwingsdk.events.internal.UpdateUserEvent;
import com.chatwingsdk.pojos.User;
import com.chatwingsdk.pojos.responses.UserResponse;
import com.chatwingsdk.utils.LogUtils;

import javax.inject.Inject;

/**
 * Created by steve on 15/05/2014.
 */
public class DownloadUserDetailIntentService extends ExtendBaseIntentService {
    private static boolean sIsInProgress;
    private static final Object sLock = new Object();
    @Inject
    ApiManager mApiManager;

    public DownloadUserDetailIntentService() {
        super("DownloadUserDetailIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        User user = mUserManager.getCurrentUser();
        if (user == null) {
            return;
        }
        setIsInProgress(true);
        LogUtils.v("Syncing user detail");
        try {
            UserResponse userResponse = mApiManager.loadUserDetails(user);
            User newUser = userResponse.getUser();
            mUserManager.updateUser(user, newUser);
            post(UpdateUserEvent.success());
        } catch (Exception e) {
            LogUtils.e(e);
            post(new UpdateUserEvent(e));
        }
        setIsInProgress(false);
    }

    private void post(final UpdateUserEvent event) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mBus.post(event);
            }
        });
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

}
