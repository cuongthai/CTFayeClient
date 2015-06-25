package com.chatwing.whitelabel.scribeconfigs;


import com.chatwing.whitelabel.BuildConfig;
import com.chatwing.whitelabel.Constants;
import com.chatwing.whitelabel.pojos.oauth.OAuthParams;
import com.chatwing.whitelabel.pojos.params.oauth.AuthenticationParams;

import org.scribe.builder.api.Api;
import org.scribe.builder.api.TumblrApi;
import org.scribe.model.Token;

public class TumblrConfig implements ScribeConfig {

    @Override
    public Class<? extends Api> getProvider() {
        return TumblrApi.class;
    }

    @Override
    public String getApiKey() {
        return BuildConfig.TUMBLR_API_KEY;
    }

    @Override
    public String getApiSecret() {
        return BuildConfig.TUMBLR_API_SECRET;
    }

    @Override
    public String getCallbackURL() {
        return BuildConfig.TUMBLR_CALLBACK_URL;
    }

    @Override
    public AuthenticationParams getAuthenticationParams(Token token) {
        AuthenticationParams params = new OAuthParams(
                Constants.TYPE_TUMBLR,
                token.getToken(),
                token.getSecret(),
                null);
        return params;
    }
}
