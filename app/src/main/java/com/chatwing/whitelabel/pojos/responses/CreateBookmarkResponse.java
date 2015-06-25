package com.chatwing.whitelabel.pojos.responses;


import com.chatwing.whitelabel.pojos.SyncedBookmark;

/**
 * Created by steve on 23/06/2014.
 */
public class CreateBookmarkResponse extends BaseResponse {
    private SyncedBookmark data;

    public SyncedBookmark getData() {
        return data;
    }
}
