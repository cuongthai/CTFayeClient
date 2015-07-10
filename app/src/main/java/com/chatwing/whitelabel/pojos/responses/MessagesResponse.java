package com.chatwing.whitelabel.pojos.responses;

import com.chatwing.whitelabel.pojos.Message;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Author: Huy Nguyen
 * Date: 5/11/13
 * Time: 10:34 PM
 */
public class MessagesResponse extends BaseResponse {
    @SerializedName("data")
    private List<Message> messages;

    public List<Message> getMessages() {
        return messages;
    }
}
