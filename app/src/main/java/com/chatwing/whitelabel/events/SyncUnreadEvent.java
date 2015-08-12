package com.chatwing.whitelabel.events;

import java.util.List;

/**
 * Created by steve on 24/04/2015.
 */
public class SyncUnreadEvent {
    //List of chatboxes that has never been acked
    private List<Integer> unAckChatboxIds;
    public SyncUnreadEvent(List<Integer> unAckChatboxIds) {
        this.unAckChatboxIds = unAckChatboxIds;
    }

    public List<Integer> getUnAckChatboxIds() {
        return unAckChatboxIds;
    }
}
