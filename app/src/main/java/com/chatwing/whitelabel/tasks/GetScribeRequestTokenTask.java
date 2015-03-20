package com.chatwing.whitelabel.tasks;

import com.squareup.otto.Bus;

import org.scribe.model.Token;
import org.scribe.oauth.OAuthService;

/**
 * Author: Huy Nguyen
 * Date: 8/30/13
 * Time: 11:41 AM
 */
public class GetScribeRequestTokenTask extends GetScribeTokenTask {

    public GetScribeRequestTokenTask(Bus bus) {
        super(bus);
    }

    @Override
    protected Token run(OAuthService... params) throws Exception {
        return params[0].getRequestToken();
    }
}
