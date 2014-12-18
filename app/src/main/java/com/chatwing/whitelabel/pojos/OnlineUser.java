package com.chatwing.whitelabel.pojos;

import com.google.gson.annotations.SerializedName;

/**
 * Author: Huy Nguyen
 * Date: 7/25/13
 * Time: 2:41 PM
 */
public class OnlineUser {
    private boolean authenticated;
    private String id;
    @SerializedName("login_id")
    private String loginId;
    @SerializedName("login_type")
    private String loginType;
    @SerializedName("profile")
    private OnlineUserProfile onlineUserProfile;

    public String getName() {
        return onlineUserProfile.getName();
    }

    public String getLoginType() {
        return loginType;
    }

    public String getLoginId() {
        return loginId;
    }
}
