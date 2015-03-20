package com.chatwing.whitelabel.pojos.oauth;

import com.chatwingsdk.pojos.params.oauth.JsonParams;
import com.google.gson.annotations.SerializedName;

/**
 * Author: Huy Nguyen
 * Date: 8/30/13
 * Time: 4:56 PM
 */
public class YahooJsonParams extends JsonParams {
    @SerializedName("xoauth_yahoo_guid")
    private String guid;

    public YahooJsonParams(String guid) {
        this.guid = guid;
    }
}
