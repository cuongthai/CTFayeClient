package com.chatwing.whitelabel.pojos.params;

import com.chatwingsdk.pojos.params.Params;
import com.google.gson.annotations.SerializedName;

/**
 * Created by steve on 16/06/2014.
 */
public class OnlineUserParams extends Params {
    @SerializedName("id")
    private int mChatboxId;
    @SerializedName("forcelist")
    private int forceList = 1;

    public OnlineUserParams(int chatboxId) {
        mChatboxId = chatboxId;
    }
}
