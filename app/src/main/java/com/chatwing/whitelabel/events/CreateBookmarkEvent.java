package com.chatwing.whitelabel.events;


import com.chatwing.whitelabel.pojos.responses.CreateBookmarkResponse;

/**
 * Author: steve
 * Date: 6/23/14
 * Time: 11:32 AM
 */
public class CreateBookmarkEvent {
    private Exception exception;
    private CreateBookmarkResponse response;

    public CreateBookmarkEvent(CreateBookmarkResponse response) {
        this.response = response;
    }

    public CreateBookmarkEvent(Exception exception) {
        this.exception = exception;
    }

    public CreateBookmarkResponse getResponse() {
        return response;
    }

    public Exception getException() {
        return exception;
    }
}
