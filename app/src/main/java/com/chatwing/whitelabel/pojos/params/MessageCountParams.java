package com.chatwing.whitelabel.pojos.params;

import com.google.gson.annotations.SerializedName;

/**
 * Created by steve on 25/01/2015.
 */
public class MessageCountParams extends Params {
    @SerializedName("id")
    private final Integer chatboxId;

    public MessageCountParams(Integer chatboxId) {
        this.chatboxId = chatboxId;
    }
}
