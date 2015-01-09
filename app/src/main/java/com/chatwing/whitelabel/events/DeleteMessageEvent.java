package com.chatwing.whitelabel.events;

import com.chatwing.whitelabel.pojos.responses.DeleteMessageResponse;
import com.chatwingsdk.events.internal.MessageEvent;

/**
 * Created by nguyenthanhhuy on 12/21/13.
 */
public class DeleteMessageEvent extends MessageEvent {
    private String messageId;
    private DeleteMessageResponse response;

    public DeleteMessageEvent(int chatBoxId, String messageId, Exception exception) {
        super(chatBoxId, exception);
        this.messageId = messageId;
    }

    public DeleteMessageEvent(int chatBoxId, String messageId, DeleteMessageResponse response) {
        super(chatBoxId);
        this.messageId = messageId;
        this.response = response;
    }

    public String getMessageId() {
        return messageId;
    }

    public DeleteMessageResponse getResponse() {
        return response;
    }
}
