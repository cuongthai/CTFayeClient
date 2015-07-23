package com.chatwing.whitelabel.managers;

import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.Menu;

import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.events.CurrentConversationEvent;
import com.chatwing.whitelabel.events.PostAuthenticationEvent;
import com.chatwing.whitelabel.events.UpdateSubscriptionEvent;
import com.chatwing.whitelabel.events.UserSelectedConversationEvent;
import com.chatwing.whitelabel.events.UserSelectedDefaultUsersEvent;
import com.chatwing.whitelabel.validators.ConversationIdValidator;
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
        AppCompatActivity activity = mActivityDelegate.getActivity();
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
    public void onUserSelectedConversationEvent(UserSelectedConversationEvent event) {
        super.onUserSelectedConversationEvent(event);
    }

    @com.squareup.otto.Subscribe
    public void onUserSelectedDefaultUsersEvent(UserSelectedDefaultUsersEvent event) {
        super.onUserSelectedDefaultUsersEvent(event);
    }

    @Subscribe
    public void onUpdateSubscriptionEvent(UpdateSubscriptionEvent event) {
        super.onUpdateSubscriptionEvent(event);
    }

    @com.squareup.otto.Subscribe
    public void onCurrentConversationChanged(CurrentConversationEvent event) {
        super.onCurrentConversationChanged(event);
    }

    @com.squareup.otto.Subscribe
    public void onPostAuthentication(PostAuthenticationEvent event) {
        super.onPostAuthentication(event);
    }
}
