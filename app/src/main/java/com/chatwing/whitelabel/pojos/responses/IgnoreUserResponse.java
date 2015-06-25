package com.chatwing.whitelabel.pojos.responses;


import com.chatwing.whitelabel.pojos.IgnoreUser;

/**
 * Created by steve on 25/07/2014.
 */
public class IgnoreUserResponse extends BaseResponse {
    private IgnoreUser data;

    public IgnoreUser getIgnoreUser() {
        return data;
    }
}
