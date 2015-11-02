/*
 * Copyright (C) 2014 ChatWing
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chatwing.whitelabel.pojos;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Author: Huy Nguyen
 * Date: 3/26/13
 * Time: 7:07 AM
 */
public class User extends BaseUser implements Serializable {
    private String id;
    private String ip;
    private UserProfile profile;
    @SerializedName("access_token")
    private String accessToken;
    private String avatar;
    @SerializedName("ignore_list")
    private ArrayList<IgnoreUser> ignoreList;
    private Set<String> ignoreChatUserIDSet;
    @SerializedName("conversation_push_notification")
    private boolean conversationPushNotification;
    @SerializedName("conversation_email_notification")
    private boolean conversationEmailNotification;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public UserProfile getProfile() {
        return profile;
    }

    public void setProfile(UserProfile profile) {
        this.profile = profile;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public boolean isSessionValid() {
        return !TextUtils.isEmpty(accessToken);
    }

    public String getUsername() {
        return profile.getUsername();
    }

    public String getName() {
        return profile.getName();
    }

    public String getAvatar() {
        return avatar;
    }

    public ArrayList<IgnoreUser> getIgnoreList() {
        return ignoreList == null ? new ArrayList<IgnoreUser>() : ignoreList;
    }

    public Set<String> getIgnoreChatUserIDSet() {
        if (ignoreChatUserIDSet == null) {
            ignoreChatUserIDSet = new HashSet<String>();
            ArrayList<IgnoreUser> ignoreList = getIgnoreList();
            for (IgnoreUser ignoreUser : ignoreList) {
                ignoreChatUserIDSet.add(ignoreUser.getIdentifier());
            }
        }
        return ignoreChatUserIDSet;
    }

    public void unignoreUser(IgnoreUser ignoreUser) {
        getIgnoreList().remove(ignoreUser);
        ignoreChatUserIDSet = null;
    }

    public void ignoreUser(IgnoreUser ignoreUser) {
        getIgnoreList().add(ignoreUser);
        ignoreChatUserIDSet = null;
    }

    public boolean isIgnoring(String userIdentifier) {
        ignoreChatUserIDSet = getIgnoreChatUserIDSet();
        return ignoreChatUserIDSet.contains(userIdentifier);
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

}
