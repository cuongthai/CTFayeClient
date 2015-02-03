package com.chatwing.whitelabel.pojos.responses;


import com.chatwingsdk.pojos.IgnoreUser;
import com.chatwingsdk.pojos.responses.BaseResponse;

/**
 * Created by steve on 25/07/2014.
 */
public class IgnoreUserResponse extends BaseResponse {
    private IgnoreUser data;

    public IgnoreUser getIgnoreUser() {
        return data;
    }
}
