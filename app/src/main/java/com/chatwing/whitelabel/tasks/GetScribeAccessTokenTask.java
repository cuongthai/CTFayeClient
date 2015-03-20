
package com.chatwing.whitelabel.tasks;

import com.squareup.otto.Bus;

import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

/**
 * Author: Huy Nguyen
 * Date: 8/30/13
 * Time: 11:52 AM
 */
public class GetScribeAccessTokenTask extends GetScribeTokenTask {

    private Token mRequestToken;
    private Verifier mVerifier;

    public GetScribeAccessTokenTask(Bus bus, Token requestToken, Verifier verifier) {
        super(bus);
        mRequestToken = requestToken;
        mVerifier = verifier;
    }

    @Override
    protected Token run(OAuthService... params) throws Exception {
        return params[0].getAccessToken(mRequestToken, mVerifier);
    }
}
