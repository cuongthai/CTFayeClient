package com.chatwing.whitelabel.pojos.params;


import com.chatwingsdk.pojos.params.Params;

public class ResetPasswordParams extends Params {
    private String username;

    public ResetPasswordParams(String email) {
        this.username = email;
    }
}
