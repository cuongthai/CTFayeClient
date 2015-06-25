package com.chatwing.whitelabel.tasks;

import com.chatwing.whitelabel.managers.ApiManager;
import com.chatwing.whitelabel.pojos.responses.ResetPasswordResponse;
import com.squareup.otto.Bus;

import javax.inject.Inject;

public class ResetPasswordTask extends CallbackTask<String, Void, ResetPasswordResponse> {
    private ApiManager mApiManager;

    @Inject
    ResetPasswordTask(Bus bus, ApiManager apiManager) {
        super(bus);
        mApiManager = apiManager;
    }

    @Override
    protected ResetPasswordResponse run(String... emails) throws Exception {
        return mApiManager.resetPassword(emails[0]);
    }
}
