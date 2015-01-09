package com.chatwing.whitelabel.pojos.params;

import com.chatwingsdk.pojos.params.BaseChatBoxParams;
import com.google.gson.annotations.SerializedName;

/**
 * Created by nguyenthanhhuy on 12/21/13.
 */
public class DeleteMessageParams extends BaseChatBoxParams {
    @SerializedName("id")
    private String messageId;

    public DeleteMessageParams(int chatBoxId, String messageId) {
        super(chatBoxId);
        this.messageId = messageId;
    }
}
