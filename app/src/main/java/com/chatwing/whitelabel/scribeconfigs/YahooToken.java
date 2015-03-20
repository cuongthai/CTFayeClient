package com.chatwing.whitelabel.scribeconfigs;

import org.scribe.model.Token;

/**
 * Author: Huy Nguyen
 * Date: 8/30/13
 * Time: 5:12 PM
 */
public class YahooToken extends Token {
    private String guid;

    public YahooToken(String token, String secret, String rawResponse, String guid) {
        super(token, secret, rawResponse);
        this.guid = guid;
    }

    public String getGuid() {
        return guid;
    }
}
