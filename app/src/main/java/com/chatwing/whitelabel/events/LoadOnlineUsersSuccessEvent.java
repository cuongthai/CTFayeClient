package com.chatwing.whitelabel.events;


import com.chatwing.whitelabel.pojos.OnlineUser;

import java.util.List;

/**
 * Created by nguyenthanhhuy on 1/8/14.
 */
public class LoadOnlineUsersSuccessEvent {
    private int count;
    private List<OnlineUser> onlineUsers;

    public LoadOnlineUsersSuccessEvent(int count, List<OnlineUser> onlineUsers) {
        this.count = count;
        this.onlineUsers = onlineUsers;
    }

    public int getCount() {
        return count;
    }

    public List<OnlineUser> getOnlineUsers() {
        return onlineUsers;
    }
}
