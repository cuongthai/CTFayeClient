package com.chatwing.whitelabel.pojos.params;



public class ResetPasswordParams extends Params {
    private String username;

    public ResetPasswordParams(String email) {
        this.username = email;
    }
}
