package com.chatwing.whitelabel.managers;

import com.chatwing.whitelabel.events.AccountSwitchEvent;

/**
 * Created by steve on 11/03/2015.
 */
public class ExtendCommunicationModeManager {
    public static interface Delegate extends CommunicationModeManager.Delegate {

        void dismissAuthenticationDialog();

        void onAccountSwitch(AccountSwitchEvent accountSwitchEvent);
    }
}
