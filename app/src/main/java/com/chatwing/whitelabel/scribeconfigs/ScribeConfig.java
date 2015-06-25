package com.chatwing.whitelabel.scribeconfigs;


import com.chatwing.whitelabel.pojos.params.oauth.AuthenticationParams;

import org.scribe.builder.api.Api;
import org.scribe.model.Token;

/**
 * Created by nguyenthanhhuy on 12/1/13.
 */
public interface ScribeConfig {
    Class<? extends Api> getProvider();

    String getApiKey();

    String getApiSecret();

    String getCallbackURL();

    AuthenticationParams getAuthenticationParams(Token token);
}
