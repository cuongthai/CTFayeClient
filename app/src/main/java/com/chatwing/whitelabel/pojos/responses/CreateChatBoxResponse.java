package com.chatwing.whitelabel.pojos.responses;


import com.chatwingsdk.pojos.LightWeightChatBox;
import com.chatwingsdk.pojos.responses.BaseResponse;

/**
 * Created by nguyenthanhhuy on 10/26/13.
 */
public class CreateChatBoxResponse extends BaseResponse {
    private LightWeightChatBox data;

    public LightWeightChatBox getData() {
        return data;
    }
}
