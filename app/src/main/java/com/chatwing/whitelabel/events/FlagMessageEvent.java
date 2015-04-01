package com.chatwing.whitelabel.events;


import com.chatwing.whitelabel.pojos.responses.FlagMessageResponse;

/**
 * Created by steve on 25/07/2014.
 */
public class FlagMessageEvent {
    private Exception exception;
    private FlagMessageResponse response;

    public FlagMessageEvent(FlagMessageResponse response){
        this.response = response;
    }

    public FlagMessageEvent(Exception exception){
        this.exception = exception;
    }

    public Exception getException() {
        return exception;
    }
}
