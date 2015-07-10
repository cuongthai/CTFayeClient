package com.chatwing.whitelabel.events;


import com.chatwing.whitelabel.pojos.Message;

import java.util.List;

/**
 * Author: Huy Nguyen
 * Date: 7/4/13
 * Time: 2:43 PM
 */
public class GotMoreMessagesEvent extends MessageEvent {
    private List<Message> messagesFromServer;
    private List<Message> insertedMessages;

    public GotMoreMessagesEvent(int chatBoxId,
                                List<Message> messagesFromServer,
                                List<Message> insertedMessages) {
        super(chatBoxId);
        this.messagesFromServer = messagesFromServer;
        this.insertedMessages = insertedMessages;
    }

    public GotMoreMessagesEvent(String conversationId,
                                List<Message> messagesFromServer,
                                List<Message> insertedMessages) {
        super(conversationId);
        this.messagesFromServer = messagesFromServer;
        this.insertedMessages = insertedMessages;
    }

    public GotMoreMessagesEvent(int chatBoxId, Exception exception) {
        super(chatBoxId, exception);
    }

    public GotMoreMessagesEvent(String conversationId, Exception exception) {
        super(conversationId, exception);
    }

    public List<Message> getMessagesFromServer() {
        return messagesFromServer;
    }

    public List<Message> getInsertedMessages() {
        return insertedMessages;
    }
}
