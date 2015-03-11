package com.chatwing.whitelabel.pojos.params;


import com.chatwingsdk.pojos.params.Params;

/**
 * Created by nguyenthanhhuy on 10/26/13.
 */
public class SearchChatBoxParams extends Params {
    private String query;
    private int offset;
    private int limit;

    public SearchChatBoxParams(String query, int offset, int limit) {
        this.query = query;
        this.offset = offset;
        this.limit = limit;
    }
}
