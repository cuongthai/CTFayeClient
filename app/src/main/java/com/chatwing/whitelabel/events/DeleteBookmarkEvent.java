package com.chatwing.whitelabel.events;


import com.chatwing.whitelabel.pojos.responses.DeleteBookmarkResponse;

/**
 * Author: steve
 * Date: 6/23/14
 * Time: 11:32 AM
 */
public class DeleteBookmarkEvent {
    private Exception exception;
    private DeleteBookmarkResponse response;

    public DeleteBookmarkEvent(DeleteBookmarkResponse response) {
        this.response = response;
    }

    public DeleteBookmarkEvent(Exception exception) {
        this.exception = exception;
    }

    public DeleteBookmarkResponse getResponse() {
        return response;
    }

    public Exception getException() {
        return exception;
    }
}
