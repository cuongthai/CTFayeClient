package com.chatwing.whitelabel.tasks;

import com.squareup.otto.Bus;

import org.scribe.model.Token;
import org.scribe.oauth.OAuthService;

/**
 * Author: Huy Nguyen
 * Date: 8/30/13
 * Time: 11:54 AM
 */
public abstract class GetScribeTokenTask extends CallbackTask<OAuthService, Void, Token> {
    protected GetScribeTokenTask(Bus bus) {
        super(bus);
    }
}
