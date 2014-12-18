package com.chatwing.whitelabel.events;

import com.chatwingsdk.events.internal.ExceptionEvent;

/**
 * Created by nguyenthanhhuy on 1/8/14.
 */
public class LoadOnlineUsersFailedEvent extends ExceptionEvent {
    public LoadOnlineUsersFailedEvent(Exception exception) {
        super(exception);
    }
}
