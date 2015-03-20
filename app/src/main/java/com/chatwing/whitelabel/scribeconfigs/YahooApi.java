package com.chatwing.whitelabel.scribeconfigs;

import org.scribe.extractors.AccessTokenExtractor;

/**
 * Author: Huy Nguyen
 * Date: 8/30/13
 * Time: 5:18 PM
 */
public class YahooApi extends org.scribe.builder.api.YahooApi {
    @Override
    public AccessTokenExtractor getAccessTokenExtractor() {
        return new YahooTokenExtractorImpl();
    }
}
