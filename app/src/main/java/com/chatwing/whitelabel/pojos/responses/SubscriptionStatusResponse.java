package com.chatwing.whitelabel.pojos.responses;

import java.util.Map;

/**
 * Created by steve on 23/01/2015.
 */
public class SubscriptionStatusResponse extends BaseResponse {
    private Map<String, Boolean> data;

    public Map<String, Boolean> getData() {
        return data;
    }
}
