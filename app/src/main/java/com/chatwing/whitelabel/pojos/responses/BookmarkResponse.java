package com.chatwing.whitelabel.pojos.responses;

import com.chatwingsdk.pojos.SyncedBookmark;
import com.chatwingsdk.pojos.responses.BaseResponse;

/**
 * Created by steve
 */
public class BookmarkResponse extends BaseResponse {
    private SyncedBookmark[] data;

    public SyncedBookmark[] getData() {
        return data;
    }
}
