package com.chatwing.whitelabel.events;

public class EditChatMessageEvent {
    /**
     * Position in data set of the message to be edited.
     */
    private int position;

    public EditChatMessageEvent(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }
}
