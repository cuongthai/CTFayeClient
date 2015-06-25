package com.chatwing.whitelabel.pojos.responses;


import com.chatwing.whitelabel.pojos.SyncedBookmark;

/**
 * Created by steve
 */
public class BookmarkResponse extends BaseResponse {
    private SyncedBookmark[] data;

    public SyncedBookmark[] getData() {
        return data;
    }
}
