package com.chatwing.whitelabel.events;


import com.chatwingsdk.pojos.Message;

/**
 * Created by stevethai
 */
public class RequestBlockEvent {

    private final Message message;

    private boolean clearMessage;
    private String reason;
    private long duration;

    public RequestBlockEvent(Message message,
                             boolean clearMessage,
                             String reason,
                             long duration) {
        this.message = message;
        this.clearMessage = clearMessage;
        this.reason = reason;
        this.duration = duration;
    }

    public Message getMessage() {
        return message;
    }

    public long getDuration() {
        return duration;
    }

    public String getReason() {
        return reason;
    }

    public boolean isClearMessage() {
        return clearMessage;
    }
}
