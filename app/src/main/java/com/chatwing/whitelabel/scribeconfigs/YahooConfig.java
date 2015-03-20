package com.chatwing.whitelabel.scribeconfigs;

import com.chatwing.whitelabel.BuildConfig;
import com.chatwing.whitelabel.Constants;
import com.chatwing.whitelabel.pojos.oauth.OAuthParams;
import com.chatwing.whitelabel.pojos.oauth.YahooJsonParams;
import com.chatwingsdk.pojos.params.oauth.AuthenticationParams;

import org.scribe.builder.api.Api;
import org.scribe.model.Token;

/**
 * Author: Huy Nguyen
 * Date: 8/30/13
 * Time: 10:50 AM
 */
public class YahooConfig implements ScribeConfig {

    @Override
    public Class<? extends Api> getProvider() {
        return YahooApi.class;
    }

    @Override
    public String getApiKey() {
        return BuildConfig.YAHOO_API_KEY;
    }

    @Override
    public String getApiSecret() {
        return BuildConfig.YAHOO_API_SECRET;
    }

    @Override
    public String getCallbackURL() {
        return BuildConfig.YAHOO_CALLBACK_URL;
    }

    @Override
    public AuthenticationParams getAuthenticationParams(Token token) {
        String guid = ((YahooToken) token).getGuid();
        AuthenticationParams params = new OAuthParams(
                Constants.TYPE_YAHOO,
                token.getToken(),
                token.getSecret(),
                new YahooJsonParams(guid));
        return params;
    }
}
