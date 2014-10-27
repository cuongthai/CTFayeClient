package com.chatwing.whitelabel.pojos.responses;

import com.chatwingsdk.pojos.responses.BaseResponse;

public class RegisterResponse extends BaseResponse {
    private Data data;

    public Data getData() {
        return data;
    }

    private static class Data {
        private String email;
    }
}
