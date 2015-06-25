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

    public OnlineUser(boolean authenticated, String id, String loginId, String loginType, OnlineUserProfile onlineUserProfile) {
        this.authenticated = authenticated;
        this.id = id;
        this.loginId = loginId;
        this.loginType = loginType;
        this.onlineUserProfile = onlineUserProfile;
    }

    public String getName() {
        return onlineUserProfile.getName();
    }

    public String getLoginType() {
        return loginType;
    }

    public String getLoginId() {
        return loginId;
    }

    @Override
    public boolean equals(Object o) {
        OnlineUser target = (OnlineUser) o;
        return o!=null && o instanceof OnlineUser && BaseUser.computeIdentifier(loginId, loginType).
                equals(BaseUser.computeIdentifier(target.getLoginId(), target.getLoginType()));
    }

    @Override
    public int hashCode() {
        return BaseUser.computeIdentifier(loginId, loginType).hashCode();
    }

    public boolean isAuthenticated() {
        return authenticated;
    }
}
