package com.chatwing.whitelabel.events;

/**
 * Created by nguyenthanhhuy on 6/18/14.
 */
public class PasswordEnteredEvent {
    private String password;
    private boolean shouldRemember;

    public PasswordEnteredEvent(String password, boolean shouldRemember) {
        this.password = password;
        this.shouldRemember = shouldRemember;
    }

    public String getPassword() {
        return password;
    }

    public boolean shouldRemember() {
        return shouldRemember;
    }
}
