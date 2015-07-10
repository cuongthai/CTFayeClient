package com.chatwing.whitelabel.pojos.params;

import com.google.gson.annotations.SerializedName;

/**
 * Created by steve on 4/2/14.
 */
public class ConversationMessageParams extends Params {
    @SerializedName("conversation_id")
    private String conversationId;
    @SerializedName("date_created")
    private long createdDate;

    public ConversationMessageParams(String conversationId) {
        this.conversationId = conversationId;
    }

    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }
}
