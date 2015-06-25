package com.chatwing.whitelabel.pojos.responses;

import java.util.Map;

/**
 * Created by steve on 23/01/2015.
 */
public class SubscriptionResponse extends BaseResponse {
    private Map<String, String> data;

    public Map<String, String> getData() {
        return data;
    }
}
