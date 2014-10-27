package com.chatwing.whitelabel.pojos.oauth;

import com.chatwing.whitelabel.Constants;
import com.chatwingsdk.pojos.params.oauth.AuthenticationParams;

/**
 * Created by steve on 03/07/2014.
 */
public class GuestOAuthParams extends AuthenticationParams {
    public GuestOAuthParams(String guestName, String avatar) {
        super(Constants.TYPE_GUEST, new String[]{guestName, avatar});
    }
}
