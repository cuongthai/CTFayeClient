package com.chatwing.whitelabel.services;

import android.content.Intent;

import com.chatwing.whitelabel.events.VerifyEmailEvent;
import com.chatwing.whitelabel.managers.ApiManager;
import com.chatwing.whitelabel.pojos.User;
import com.chatwing.whitelabel.utils.LogUtils;

import javax.inject.Inject;

/**
 * Created by steve on 15/05/2014.
 */
public class VerifyEmailIntentService extends ExtendBaseIntentService {
    private static boolean sIsInProgress;
    private static final Object sLock = new Object();
    @Inject
    ApiManager mApiManager;

    public VerifyEmailIntentService() {
        super("VerifyEmailIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        User user = mUserManager.getCurrentUser();
        if (user == null) {
            return;
        }
        setIsInProgress(true);
        post(VerifyEmailEvent.started());
        LogUtils.v("Syncing user detail");
        try {
             mApiManager.verifyEmail(user);
            post(VerifyEmailEvent.success());
        } catch (Exception e) {
            LogUtils.e(e);
            post(new VerifyEmailEvent(e));
        }
        setIsInProgress(false);
    }

    private void post(final VerifyEmailEvent event) {
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
