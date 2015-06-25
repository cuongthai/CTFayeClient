package com.chatwing.whitelabel.services;

import android.content.Intent;

import com.chatwing.whitelabel.utils.LogUtils;


/**
 * Created by steve on 02/02/2015.
 */

public class OfflineIntentService extends BaseIntentService{
    public OfflineIntentService() {
        super("OfflineIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            mApiManager.offline(mUserManager.getCurrentUser());
            LogUtils.v("Offline");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
