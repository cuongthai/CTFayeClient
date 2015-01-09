package com.chatwing.whitelabel.events;


import com.chatwingsdk.pojos.Message;

/**
 * Created by stevethai
 */
public class RequestBlockIPEvent extends RequestBlockEvent {

    public RequestBlockIPEvent(Message message,
                               boolean clearMessage,
                               String reason,
                               long duration) {
        super(message, clearMessage, reason, duration);
    }

}
