package com.chatwing.whitelabel.events;

/**
 * Author: Huy Nguyen
 * Date: 5/31/13
 * Time: 2:01 PM
 */
public class SyncBookmarkEvent {
    public enum Status {
        STARTED,
        SUCCEED,
        FAILED
    }

    public static SyncBookmarkEvent startedEvent() {
        return new SyncBookmarkEvent(Status.STARTED);
    }

    public static SyncBookmarkEvent succeedEvent() {
        return new SyncBookmarkEvent(Status.SUCCEED);
    }

    public static SyncBookmarkEvent failedEvent(Exception exception) {
        SyncBookmarkEvent event = new SyncBookmarkEvent(Status.FAILED);
        event.exception = exception;
        return event;
    }

    private Status status;
    private Exception exception;

    private SyncBookmarkEvent(Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }

    public Exception getException() {
        return exception;
    }
}
