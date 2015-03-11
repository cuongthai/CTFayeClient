package com.chatwing.whitelabel.pojos.responses;


import com.chatwingsdk.pojos.SyncedBookmark;
import com.chatwingsdk.pojos.responses.BaseResponse;

/**
 * Created by steve on 23/06/2014.
 */
public class CreateBookmarkResponse extends BaseResponse {
    private SyncedBookmark data;

    public SyncedBookmark getData() {
        return data;
    }
}
