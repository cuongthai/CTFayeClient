package com.chatwing.whitelabel.events;


import com.chatwing.whitelabel.pojos.Message;

/**
 * Created by steve on 06/01/2015.
 */
public class MessageEditEvent {
    private final Message[] mMessages;

    public MessageEditEvent(Message[] messages) {
        mMessages = messages;
    }

    public Message[] getMessages() {
        return mMessages;
    }
}
