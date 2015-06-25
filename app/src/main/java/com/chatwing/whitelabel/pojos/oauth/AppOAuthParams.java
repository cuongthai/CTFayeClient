package com.chatwing.whitelabel.pojos.oauth;

import com.chatwing.whitelabel.ChatWing;
import com.chatwing.whitelabel.Constants;
import com.chatwing.whitelabel.pojos.params.oauth.AuthenticationParams;

/**
 * Author: Huy Nguyen
 * Date: 9/10/13
 * Time: 11:50 AM
 */
public class AppOAuthParams extends AuthenticationParams {
    public AppOAuthParams(String email, String password) {
        super(Constants.TYPE_APP, new String[]{email, password, ChatWing.getAppId()});
    }

    public String getEmail() {
        return (String) params[0];
    }

    public String getPassword() {
        return (String) params[1];
    }
}
