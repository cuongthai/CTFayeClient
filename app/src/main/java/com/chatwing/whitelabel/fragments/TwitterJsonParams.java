package com.chatwing.whitelabel.fragments;

import com.chatwingsdk.pojos.params.oauth.JsonParams;
import com.google.gson.annotations.SerializedName;

/**
 * Author: Huy Nguyen
 * Date: 8/30/13
 * Time: 4:50 PM
 */
public class TwitterJsonParams extends JsonParams {
    @SerializedName("user_id")
    private long userId;
    @SerializedName("screen_name")
    private String screenName;

    public TwitterJsonParams(long userId, String screenName) {
        this.userId = userId;
        this.screenName = screenName;
    }
}
