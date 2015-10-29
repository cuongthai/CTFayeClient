package com.chatwing.whitelabel.events;

/**
 * Created by cuongthai on 10/29/15.
 */
public class ChatServiceEvent {
    private Status status;
    private String fayeChannel;

    public enum Status {
        CONNECT,
        DISCONNECT,
        SUBSCRIBE_CHANNEL,
        UNSUBSCRIBE_ALL_CHANNELS
    }

    public static ChatServiceEvent connect() {
        return new ChatServiceEvent(Status.CONNECT);
    }

    public static ChatServiceEvent subscribeChannel(String fayeChannel) {
        ChatServiceEvent chatServiceEvent = new ChatServiceEvent(Status.SUBSCRIBE_CHANNEL);
        chatServiceEvent.fayeChannel = fayeChannel;
        return chatServiceEvent;
    }

    public static ChatServiceEvent unsubscribeAllChannels() {
        ChatServiceEvent chatServiceEvent = new ChatServiceEvent(Status.UNSUBSCRIBE_ALL_CHANNELS);
        return chatServiceEvent;
    }

    public ChatServiceEvent(Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }

    public String getFayeChannel() {
        return fayeChannel;
    }
}
