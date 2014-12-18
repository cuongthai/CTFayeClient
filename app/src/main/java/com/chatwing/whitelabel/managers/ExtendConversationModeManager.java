package com.chatwing.whitelabel.managers;

import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;

import com.chatwingsdk.managers.ConversationModeManager;
import com.chatwingsdk.managers.CurrentConversationManager;
import com.chatwingsdk.managers.UserManager;
import com.chatwingsdk.validators.ConversationIdValidator;
import com.squareup.otto.Bus;

/**
 * Created by steve on 16/12/2014.
 */
public class ExtendConversationModeManager extends ConversationModeManager {
    public static final int DRAWER_GRAVITY_UNUSED_SECONDARY = Gravity.RIGHT;

    public ExtendConversationModeManager(Bus bus,
                                         Delegate delegate,
                                         UserManager userManager,
                                         CurrentConversationManager currentConversationManager,
                                         ConversationIdValidator conversationIdValidator) {
        super(bus, delegate, userManager, currentConversationManager, conversationIdValidator);
    }

    @Override
    public void activate() {
        super.activate();
        mActivityDelegate.getDrawerLayout().setDrawerLockMode(
                DrawerLayout.LOCK_MODE_LOCKED_CLOSED, DRAWER_GRAVITY_UNUSED_SECONDARY);
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

    @com.squareup.otto.Subscribe
    public void onCurrentConversationChanged(com.chatwingsdk.events.internal.CurrentConversationEvent event) {
        super.onCurrentConversationChanged(event);
    }

    @com.squareup.otto.Subscribe
    public void onPostAuthentication(com.chatwingsdk.events.internal.PostAuthenticationEvent event) {
        super.onPostAuthentication(event);
    }
}
