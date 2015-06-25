package com.chatwing.whitelabel.events;

/**
 * Author: Huy Nguyen
 * Date: 8/6/13
 * Time: 4:28 AM
 */
public class UpdateUserEvent extends ExceptionEvent {
    private STATE mState;

    public UpdateUserEvent(STATE state) {
        mState = state;
    }

    public enum STATE {
        CANCELLED,
        STARTED,
        SUCCESS,
        ERROR
    }

    public static UpdateUserEvent started() {
        return new UpdateUserEvent(STATE.STARTED);
    }

    public static UpdateUserEvent success() {
        return new UpdateUserEvent(STATE.SUCCESS);
    }

    public UpdateUserEvent(Exception exception) {
        super(exception);
        this.mState = STATE.ERROR;
    }

    public STATE getState() {
        return mState;
    }
}
