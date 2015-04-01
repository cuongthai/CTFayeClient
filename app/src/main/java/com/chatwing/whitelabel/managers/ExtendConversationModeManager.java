package com.chatwing.whitelabel.managers;

import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.Menu;

import com.chatwing.whitelabel.R;
import com.chatwingsdk.events.internal.UpdateSubscriptionEvent;
import com.chatwingsdk.managers.CommunicationActivityManager;
import com.chatwingsdk.managers.ConversationModeManager;
import com.chatwingsdk.managers.CurrentConversationManager;
import com.chatwingsdk.managers.UserManager;
import com.chatwingsdk.validators.ConversationIdValidator;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

/**
 * Created by steve on 16/12/2014.
 */
public class ExtendConversationModeManager extends ConversationModeManager {
    public static final int DRAWER_GRAVITY_UNUSED_SECONDARY = Gravity.RIGHT;

    public ExtendConversationModeManager(Bus bus,
                                         Delegate delegate,
                                         UserManager userManager,
                                         CurrentConversationManager currentConversationManager,
                                         ConversationIdValidator conversationIdValidator,
                                         CommunicationActivityManager communicationActivityManager) {
        super(bus, delegate, userManager, currentConversationManager, conversationIdValidator, communicationActivityManager);
    }

    @Override
    public void activate() {
        super.activate();
        setTitle(mTitle.toString());
        mActivityDelegate.getDrawerLayout().setDrawerLockMode(
                DrawerLayout.LOCK_MODE_LOCKED_CLOSED, DRAWER_GRAVITY_UNUSED_SECONDARY);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        ActionBarActivity activity = mActivityDelegate.getActivity();
        activity.getMenuInflater().inflate(R.menu.conversation_menu, menu);
        return true;
    }


    @Override
    public void deactivate() {
        super.deactivate();
        mCurrentConversationManager.removeCurrentConversation();
        mActivityDelegate.getDrawerLayout().setDrawerLockMode(
                DrawerLayout.LOCK_MODE_UNLOCKED, DRAWER_GRAVITY_UNUSED_SECONDARY);
    }

    @com.squareup.otto.Subscribe
    public void onUserSelectedConversationEvent(com.chatwingsdk.events.internal.UserSelectedConversationEvent event) {
        super.onUserSelectedConversationEvent(event);
    }

    @Subscribe
    public void onUpdateSubscriptionEvent(UpdateSubscriptionEvent event) {
        super.onUpdateSubscriptionEvent(event);
    }

    @com.squareup.otto.Subscribe
    public void onCurrentConversationChanged(com.chatwingsdk.events.internal.CurrentConversationEvent event) {
        super.onCurrentConversationChanged(event);
    }

    @com.squareup.otto.Subscribe
    public void onPostAuthentication(com.chatwingsdk.events.internal.PostAuthenticationEvent event) {
        super.onPostAuthentication(event);
    }
}
