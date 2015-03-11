package com.chatwing.whitelabel.events;


import com.chatwing.whitelabel.pojos.responses.CreateBookmarkResponse;

/**
 * Author: steve
 * Date: 6/23/14
 * Time: 11:32 AM
 */
public class CreateBookmarkEvent {
    private boolean isUpgrading; //Compatibility: #remove-bookmark-table
    private Exception exception;
    private CreateBookmarkResponse response;

    public CreateBookmarkEvent(CreateBookmarkResponse response, boolean isUpgrading) {
        this.response = response;
        this.isUpgrading = isUpgrading;
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

    public boolean isUpgrading() {
        return isUpgrading;
    }
}
