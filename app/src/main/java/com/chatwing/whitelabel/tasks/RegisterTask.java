package com.chatwing.whitelabel.tasks;

import com.chatwing.whitelabel.managers.ApiManager;
import com.chatwing.whitelabel.pojos.responses.RegisterResponse;
import com.chatwingsdk.tasks.CallbackTask;
import com.squareup.otto.Bus;

import javax.inject.Inject;

public class RegisterTask extends CallbackTask<Void, Void, RegisterResponse> {
    private ApiManager mApiManager;
    private String mEmail;
    private String mPassword;
    private boolean mAgreeConditions;
    private boolean mAutoCreateChatbox;

    @Inject
    RegisterTask(Bus bus, ApiManager apiManager) {
        super(bus);
        this.mApiManager = apiManager;
    }

    public String getEmail() {
        return mEmail;
    }

    public String getPassword() {
        return mPassword;
    }

    public void setParams(String email, String password, boolean agreeConditions,
                          boolean autoCreateChatbox) {
        mEmail = email;
        mPassword = password;
        mAgreeConditions = agreeConditions;
        mAutoCreateChatbox = autoCreateChatbox;
    }

    @Override
    protected RegisterResponse run(Void... params) throws Exception {
        return mApiManager.register(mEmail, mPassword, mAgreeConditions,
                mAutoCreateChatbox);
    }
}
