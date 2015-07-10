package com.chatwing.whitelabel.pojos.params;

import com.google.gson.annotations.SerializedName;

/**
 * Author: Huy Nguyen
 * Date: 7/31/13
 * Time: 3:42 PM
 */
public class ChatBoxMessagesParams extends BaseChatBoxParams {
    @SerializedName("date_created")
    private long dateCreated;

    public ChatBoxMessagesParams(int chatBoxId) {
        super(chatBoxId);
    }

    public void setDateCreated(long dateCreated) {
        this.dateCreated = dateCreated;
    }
}
