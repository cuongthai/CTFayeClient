package com.chatwing.whitelabel.pojos.params;

import com.chatwing.whitelabel.pojos.UserProfile;
import com.google.gson.annotations.SerializedName;

/**
 * Author: Huy Nguyen
 * Date: 8/6/13
 * Time: 4:04 AM
 */
public class UpdateUserProfileParams extends Params {
    @SerializedName("enable_sound")
    private boolean enableSound;
    @SerializedName("scroll_to_latest_message")
    private boolean scrollToLatestMessage;
    @SerializedName("remember_previous_style")
    private boolean rememberPreviousStyle;
    private String name;
    private String username;
    private String email;

    public UpdateUserProfileParams(UserProfile userProfile) {
        enableSound = userProfile.isSoundEnabled();
        scrollToLatestMessage = userProfile.shouldScrollToLatestMessage();
        rememberPreviousStyle = userProfile.shouldRememberPreviousStyle();
        name = userProfile.getName();
        username = userProfile.getUsername();
        email = userProfile.getEmail();
    }
}
