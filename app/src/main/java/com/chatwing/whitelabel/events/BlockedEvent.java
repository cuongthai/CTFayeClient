package com.chatwing.whitelabel.events;


import com.chatwing.whitelabel.pojos.responses.BlackListResponse;

/**
 * Created by steve on 01/07/2014.
 */
public class BlockedEvent {
    private Exception exception;
    private BlackListResponse response;

    public BlockedEvent(BlackListResponse response){
        this.response = response;
    }

    public BlockedEvent(Exception exception){
        this.exception = exception;
    }

    public BlackListResponse getResponse() {
        return response;
    }

    public Exception getException() {
        return exception;
    }
}
