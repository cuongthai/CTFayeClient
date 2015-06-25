package com.chatwing.whitelabel.tasks;

import com.chatwing.whitelabel.managers.ApiManager;
import com.chatwing.whitelabel.managers.UserManager;
import com.chatwing.whitelabel.utils.LogUtils;
import com.squareup.otto.Bus;

import javax.inject.Inject;


public class PingUserTask extends CallbackTask<Void, Void, Void> {
    private final UserManager mUserManager;
    private ApiManager mApiManager;

    @Inject
    PingUserTask(Bus bus, ApiManager mApiManager, UserManager userManager) {
        super(bus);
        this.mApiManager = mApiManager;
        this.mUserManager = userManager;
    }

    @Override
    protected Void run(Void... params) throws Exception {
        mApiManager.ping(mUserManager.getCurrentUser());
        LogUtils.v("Ping");
        return null;
    }
}
