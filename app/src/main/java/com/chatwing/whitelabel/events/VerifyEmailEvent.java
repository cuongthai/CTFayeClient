package com.chatwing.whitelabel.events;

/**
 * Author: Huy Nguyen
 * Date: 8/6/13
 * Time: 4:28 AM
 */
public class VerifyEmailEvent extends ExceptionEvent {
    private STATE mState;

    public VerifyEmailEvent(STATE state) {
        mState = state;
    }

    public enum STATE {
        STARTED,
        SUCCESS,
        ERROR
    }

    public static VerifyEmailEvent started() {
        return new VerifyEmailEvent(STATE.STARTED);
    }

    public static VerifyEmailEvent success() {
        return new VerifyEmailEvent(STATE.SUCCESS);
    }

    public VerifyEmailEvent(Exception exception) {
        super(exception);
        this.mState = STATE.ERROR;
    }

    public STATE getState() {
        return mState;
    }
}
