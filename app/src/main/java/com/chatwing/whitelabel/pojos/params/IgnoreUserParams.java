package com.chatwing.whitelabel.pojos.params;

import com.chatwingsdk.pojos.params.Params;
import com.google.gson.annotations.SerializedName;

/**
 * Created by steve on 25/07/2014.
 */
public class IgnoreUserParams extends Params {
    @SerializedName("login_id")
    private String loginId;
    @SerializedName("login_type")
    private String loginType;

    public IgnoreUserParams(String loginId, String loginType) {
        this.loginId = loginId;
        this.loginType = loginType;
    }
}
