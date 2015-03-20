package com.chatwing.whitelabel.scribeconfigs;

import org.scribe.exceptions.OAuthException;
import org.scribe.extractors.TokenExtractorImpl;
import org.scribe.model.Token;
import org.scribe.utils.OAuthEncoder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author: Huy Nguyen
 * Date: 8/30/13
 * Time: 5:10 PM
 */
public class YahooTokenExtractorImpl extends TokenExtractorImpl {
    private static final Pattern GUID_REGEX = Pattern.compile("xoauth_yahoo_guid=([^&]*)");

    @Override
    public Token extract(String response) {
        Token token = super.extract(response);
        String guid = extract(response, GUID_REGEX);
        return new YahooToken(token.getToken(), token.getSecret(), response, guid);
    }

    private String extract(String response, Pattern p) {
        Matcher matcher = p.matcher(response);
        if (matcher.find() && matcher.groupCount() >= 1) {
            return OAuthEncoder.decode(matcher.group(1));
        } else {
            throw new OAuthException("Response body is incorrect. Can't extract token and secret from this: '" + response + "'", null);
        }
    }
}
