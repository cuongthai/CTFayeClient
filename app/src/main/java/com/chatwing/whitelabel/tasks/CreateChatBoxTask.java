package com.chatwing.whitelabel.tasks;

import com.chatwing.whitelabel.managers.ApiManager;
import com.chatwing.whitelabel.managers.UserManager;
import com.chatwing.whitelabel.pojos.responses.CreateChatBoxResponse;
import com.squareup.otto.Bus;

import javax.inject.Inject;

/**
 * Created by nguyenthanhhuy on 10/26/13.
 */
public class CreateChatBoxTask extends CallbackTask<Void, Void, CreateChatBoxResponse> {
    private ApiManager mApiManager;
    private UserManager mUserManager;
    private String mName;

    @Inject
    CreateChatBoxTask(Bus bus, ApiManager apiManager, UserManager userManager) {
        super(bus);
        mApiManager = apiManager;
        mUserManager = userManager;
    }

    public void setName(String name) {
        mName = name;
    }

    @Override
    protected CreateChatBoxResponse run(Void... params) throws Exception {
        return mApiManager.createChatBox(mUserManager.getCurrentUser(), mName);
    }
}
