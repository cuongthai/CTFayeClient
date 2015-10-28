package com.chatwing.whitelabel.events;


import com.chatwing.whitelabel.pojos.Message;

import java.util.List;

/**
 * Author: Huy Nguyen
 * Date: 7/4/13
 * Time: 2:43 PM
 */
public class GotMessagesEvent extends MessageEvent {
    private List<Message> messagesFromServer;
    private boolean loadMore;

    public GotMessagesEvent(int chatBoxId,
                            List<Message> messagesFromServer,
                            boolean loadMore) {
        super(chatBoxId);
        this.messagesFromServer = messagesFromServer;
        this.loadMore = loadMore;
    }

    public GotMessagesEvent(String conversationId,
                            List<Message> messagesFromServer,
                            boolean loadMore) {
        super(conversationId);
        this.messagesFromServer = messagesFromServer;
        this.loadMore = loadMore;
    }

    public GotMessagesEvent(int chatBoxId, Exception exception) {
        super(chatBoxId, exception);
    }

    public GotMessagesEvent(String conversationId, Exception exception) {
        super(conversationId, exception);
    }

    public List<Message> getMessagesFromServer() {
        return messagesFromServer;
    }

    public boolean isLoadMore() {
        return loadMore;
    }
}
