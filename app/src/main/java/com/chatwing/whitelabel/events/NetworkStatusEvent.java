package com.chatwing.whitelabel.events;

/**
 * Created by cuongthai on 10/27/15.
 */
public class NetworkStatusEvent {
    public enum Status {
        ON,
        OFF
    }

    private Status status;

    public NetworkStatusEvent(Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }
}
