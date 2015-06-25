package com.chatwing.whitelabel.tasks;

import com.chatwing.whitelabel.managers.ApiManager;
import com.chatwing.whitelabel.pojos.responses.LoadOnlineUsersResponse;
import com.squareup.otto.Bus;

import javax.inject.Inject;

/**
 * Created by nguyenthanhhuy on 1/8/14.
 */
public class LoadOnlineUsersTask extends CallbackTask<Integer, Void, LoadOnlineUsersResponse> {
    private ApiManager mApiManager;

    @Inject
    LoadOnlineUsersTask(Bus bus, ApiManager mApiManager) {
        super(bus);
        this.mApiManager = mApiManager;
    }

    @Override
    protected LoadOnlineUsersResponse run(Integer... params) throws Exception {
        return mApiManager.loadOnlineUsers(params[0]);
    }
}
