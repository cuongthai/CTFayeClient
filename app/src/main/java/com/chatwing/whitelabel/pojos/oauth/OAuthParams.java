package com.chatwing.whitelabel.pojos.oauth;


import com.chatwing.whitelabel.pojos.params.oauth.AuthenticationParams;
import com.chatwing.whitelabel.pojos.params.oauth.JsonParams;

/**
 * Author: Huy Nguyen
 * Date: 6/17/13
 * Time: 10:26 AM
 */
public class OAuthParams extends AuthenticationParams {

    public OAuthParams(String type, String token, String tokenSecret,
                       JsonParams jsonParams) {
        super(type, new Object[]{token, tokenSecret, jsonParams});
    }
}
