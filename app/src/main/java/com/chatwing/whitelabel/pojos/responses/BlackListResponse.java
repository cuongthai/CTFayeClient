package com.chatwing.whitelabel.pojos.responses;

import com.chatwing.whitelabel.pojos.params.BlockUserParams;

/**
 * Created by steve on 30/06/2014.
 */
public class BlackListResponse extends BaseResponse {
    private BlockUserParams data;

    public BlockUserParams getData() {
        return data;
    }
}
