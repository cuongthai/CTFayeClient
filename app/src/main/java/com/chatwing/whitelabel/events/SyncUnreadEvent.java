package com.chatwing.whitelabel.events;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by steve on 24/04/2015.
 */
public class SyncUnreadEvent {
    //List of chatboxes that has never been acked
    private ArrayList<Integer> unAckChatboxIds;
    public SyncUnreadEvent(ArrayList<Integer> unAckChatboxIds) {
        this.unAckChatboxIds = unAckChatboxIds;
    }

    public ArrayList<Integer> getUnAckChatboxIds() {
        return unAckChatboxIds;
    }
}
