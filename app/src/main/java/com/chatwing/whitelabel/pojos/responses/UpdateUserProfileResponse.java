package com.chatwing.whitelabel.pojos.responses;

import com.chatwingsdk.pojos.UserProfile;
import com.chatwingsdk.pojos.responses.BaseResponse;
import com.google.gson.annotations.SerializedName;

/**
 * Author: Huy Nguyen
 * Date: 8/6/13
 * Time: 4:11 AM
 */
public class UpdateUserProfileResponse extends BaseResponse {
    @SerializedName("data")
    private UserProfile userProfile;

    public UserProfile getUserProfile() {
        return userProfile;
    }
}
