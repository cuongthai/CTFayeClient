package com.chatwing.whitelabel.pojos.oauth;

import com.chatwing.whitelabel.ChatWingApplication;
import com.chatwing.whitelabel.Constants;
import com.chatwingsdk.ChatWing;
import com.chatwingsdk.pojos.params.oauth.AuthenticationParams;

/**
 * Author: Huy Nguyen
 * Date: 9/10/13
 * Time: 11:50 AM
 */
public class ChatwingOAuthParams extends AuthenticationParams {
    public ChatwingOAuthParams(String email, String password) {
        super(Constants.TYPE_CHATWING, new String[]{email, password});
    }

    public String getEmail() {
        return (String) params[0];
    }

    public String getPassword() {
        return (String) params[1];
    }
}
