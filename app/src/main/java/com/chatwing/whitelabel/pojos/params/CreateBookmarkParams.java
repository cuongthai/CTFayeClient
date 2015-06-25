package com.chatwing.whitelabel.pojos.params;

import com.google.gson.annotations.SerializedName;

/**
 * Created by steve on 22/06/2014.
 */
public class CreateBookmarkParams extends Params {
    @SerializedName("chatbox_id")
    private int chatboxId;

    public void setChatboxId(int chatboxId) {
        this.chatboxId = chatboxId;
    }
}
