package com.chatwing.whitelabel.tasks;

import android.content.Context;
import android.os.Bundle;

import com.chatwing.whitelabel.utils.LogUtils;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.squareup.otto.Bus;

/**
 * Author: Huy Nguyen
 * Date: 9/5/13
 * Time: 11:28 AM
 */
public class GetGooglePlusAccessTokenTask extends CallbackTask<Void, Void, String> {
    private Context mContext;
    private String mUsername;
    private String mScope;
    private Bundle mExtras;

    public GetGooglePlusAccessTokenTask(Bus bus, Context context,
                                        String username, String scope,
                                        Bundle extras) {
        super(bus);
        mContext = context;
        mUsername = username;
        mScope = scope;
        mExtras = extras;
    }

    @Override
    protected String run(Void... params) throws Exception {
        String token = GoogleAuthUtil.getToken(mContext, mUsername, mScope, mExtras);
        LogUtils.v("Google Authenticate: " + token);
        return token;
    }
}
