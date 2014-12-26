package com.chatwing.whitelabel.events;


import com.chatwing.whitelabel.pojos.OnlineUser;

import java.util.List;
import java.util.Set;

/**
 * Created by nguyenthanhhuy on 1/8/14.
 */
public class LoadOnlineUsersSuccessEvent {
    private int count;
    private Set<OnlineUser> onlineUsers;

    public LoadOnlineUsersSuccessEvent(int count, Set<OnlineUser> onlineUsers) {
        this.count = count;
        this.onlineUsers = onlineUsers;
    }

    public int getCount() {
        return count;
    }

    public Set<OnlineUser> getOnlineUsers() {
        return onlineUsers;
    }
}
