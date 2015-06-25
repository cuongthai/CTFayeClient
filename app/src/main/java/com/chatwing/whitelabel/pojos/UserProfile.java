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

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Author: Huy Nguyen
 * Date: 7/11/13
 * Time: 4:46 PM
 */
public class UserProfile implements Serializable {
    @SerializedName("enable_sound")
    private boolean enableSound;
    @SerializedName("scroll_to_latest_message")
    private boolean scrollToLatestMessage;
    @SerializedName("remember_previous_style")
    private boolean rememberPreviousStyle;
    @SerializedName("is_verified")
    private boolean isVerified;
    private String email;
    private String name;
    private String username;

    public UserProfile(boolean enableSound, boolean scrollToLatestMessage,
                       boolean rememberPreviousStyle, String name,
                       String username, String email) {
        this.enableSound = enableSound;
        this.scrollToLatestMessage = scrollToLatestMessage;
        this.rememberPreviousStyle = rememberPreviousStyle;
        this.name = name;
        this.username = username;
        this.email = email;
    }

    public boolean isSoundEnabled() {
        return enableSound;
    }

    public boolean shouldScrollToLatestMessage() {
        return scrollToLatestMessage;
    }

    public boolean shouldRememberPreviousStyle() {
        return rememberPreviousStyle;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public String getEmail() {
        return email;
    }
}
