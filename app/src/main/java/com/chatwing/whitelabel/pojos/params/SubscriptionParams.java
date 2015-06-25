package com.chatwing.whitelabel.pojos.params;

import com.google.gson.annotations.SerializedName;

/**
 * Created by steve on 23/01/2015.
 */
public class SubscriptionParams extends Params {
    @SerializedName("conversation_id")
    private String conversationID;
    private String type;
    @SerializedName("chatbox_id")
    private int chatboxId;

    public SubscriptionParams(String type, int chatboxId) {
        this.type = type;
        this.chatboxId = chatboxId;
    }

    public SubscriptionParams(String type, String conversationID) {
        this.type = type;
        this.conversationID = conversationID;
    }
}
