package com.chatwing.whitelabel.tasks;

import com.chatwing.whitelabel.managers.ApiManager;
import com.chatwing.whitelabel.managers.UserManager;
import com.chatwing.whitelabel.pojos.responses.LoadOnlineUsersResponse;
import com.squareup.otto.Bus;

import javax.inject.Inject;

/**
 * Created by nguyenthanhhuy on 1/8/14.
 */
public class LoadOnlineUsersTask extends CallbackTask<Integer, Void, LoadOnlineUsersResponse> {
    private final UserManager mUserManager;
    private ApiManager mApiManager;

    @Inject
    LoadOnlineUsersTask(Bus bus, ApiManager apiManager, UserManager userManager) {
        super(bus);
        this.mApiManager = apiManager;
        this.mUserManager = userManager;
    }

    @Override
    protected LoadOnlineUsersResponse run(Integer... params) throws Exception {
        return mApiManager.loadOnlineUsers(mUserManager.getCurrentUser(), params[0]);
    }
}
