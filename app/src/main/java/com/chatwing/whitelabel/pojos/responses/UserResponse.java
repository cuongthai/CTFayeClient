package com.chatwing.whitelabel.pojos.responses;

import com.chatwing.whitelabel.pojos.User;
import com.google.gson.annotations.SerializedName;

/**
 * Created by steve on 25/07/2014.
 */
public class UserResponse extends BaseResponse {
    @SerializedName("data")
    private User user;

    public User getUser() {
        return user;
    }
}
