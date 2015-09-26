package com.chatwing.whitelabel.services;

import android.content.Intent;

import com.chatwing.whitelabel.events.UpdateUserEvent;
import com.chatwing.whitelabel.managers.ApiManager;
import com.chatwing.whitelabel.pojos.User;
import com.chatwing.whitelabel.pojos.responses.UserResponse;
import com.chatwing.whitelabel.utils.LogUtils;

import javax.inject.Inject;

/**
 * Created by steve on 15/05/2014.
 */
public class DownloadUserDetailIntentService extends BaseIntentService {

    @Inject
    protected ApiManager mApiManager;

    private static boolean sIsInProgress;
    private static final Object sLock = new Object();

    public DownloadUserDetailIntentService() {
        super("DownloadUserDetailIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        User user = mUserManager.getCurrentUser();
        if (user == null || user.isGuest()) {
            post(new UpdateUserEvent(UpdateUserEvent.STATE.CANCELLED));
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSyncManager.removeServiceFromQueue(this);
    }
}
