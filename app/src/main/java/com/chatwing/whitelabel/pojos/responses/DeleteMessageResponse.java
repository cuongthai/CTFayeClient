package com.chatwing.whitelabel.pojos.responses;

import com.chatwingsdk.pojos.Message;
import com.chatwingsdk.pojos.responses.BaseResponse;
import com.google.gson.annotations.SerializedName;

/**
 * Created by nguyenthanhhuy on 12/21/13.
 */
public class DeleteMessageResponse extends BaseResponse {
    @SerializedName("data")
    private Message message;

    public Message getMessage() {
        return message;
    }
}
