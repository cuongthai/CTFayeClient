package com.chatwing.whitelabel.events;


import com.chatwing.whitelabel.pojos.User;

/**
 * Created by steve on 07/07/2014.
 */
public class AccountSwitchEvent {
    private final User mSelectedUser;

    public AccountSwitchEvent(User selectedUser) {
        mSelectedUser = selectedUser;
    }

    public User getSelectedUser() {
        return mSelectedUser;
    }
}
