package com.chatwing.whitelabel.events;


import com.chatwing.whitelabel.pojos.responses.IgnoreUserResponse;

/**
 * Created by steve on 25/07/2014.
 */
public class IgnoreUserEvent {
    private Exception exception;
    private IgnoreUserResponse response;

    public IgnoreUserEvent(IgnoreUserResponse response){
        this.response = response;
    }

    public IgnoreUserEvent(Exception exception){
        this.exception = exception;
    }

    public Exception getException() {
        return exception;
    }
}
