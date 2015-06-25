package com.chatwing.whitelabel.events;


/**
 * Created by nguyenthanhhuy on 1/8/14.
 */
public class LoadOnlineUsersFailedEvent extends ExceptionEvent {
    public LoadOnlineUsersFailedEvent(Exception exception) {
        super(exception);
    }
}
