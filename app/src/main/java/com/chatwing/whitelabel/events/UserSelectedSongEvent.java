package com.chatwing.whitelabel.events;

/**
 * Created by steve on 26/05/2015.
 */
public class UserSelectedSongEvent {
    private final int position;

    public UserSelectedSongEvent(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }
}
