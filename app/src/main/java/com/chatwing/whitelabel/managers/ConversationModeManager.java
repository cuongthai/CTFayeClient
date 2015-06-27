/*
 * Copyright (C) 2014 ChatWing
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chatwing.whitelabel.managers;

import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.activities.CommunicationActivity;
import com.chatwing.whitelabel.contentproviders.ChatWingContentProvider;
import com.chatwing.whitelabel.events.CurrentConversationEvent;
import com.chatwing.whitelabel.events.PostAuthenticationEvent;
import com.chatwing.whitelabel.events.UpdateSubscriptionEvent;
import com.chatwing.whitelabel.events.UserSelectedConversationEvent;
import com.chatwing.whitelabel.fragments.NotificationFragment;
import com.chatwing.whitelabel.pojos.Conversation;
import com.chatwing.whitelabel.pojos.Message;
import com.chatwing.whitelabel.pojos.User;
import com.chatwing.whitelabel.services.AckConversationIntentService;
import com.chatwing.whitelabel.services.UpdateNotificationSettingsService;
import com.chatwing.whitelabel.tables.NotificationMessagesTable;
import com.chatwing.whitelabel.utils.LogUtils;
import com.chatwing.whitelabel.validators.ConversationIdValidator;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

/**
 * Created by cuongthai on 18/08/2014.
 */
public class ConversationModeManager extends CommunicationModeManager {
    public static final int DRAWER_GRAVITY_CONVERSATIONS = Gravity.LEFT;
    private static final String EXTRA_CURRENT_CONVERSATION_ID = "current_conversation_id";

    protected final CurrentConversationManager mCurrentConversationManager;
    private String mRequestedUserHashKey;
    ConversationIdValidator mConversationIdValidator;
    private final String mConversationsTitle;
    private String mRequestedConversationKey;


    public ConversationModeManager(Bus bus,
                                   Delegate delegate,
                                   UserManager userManager,
                                   CurrentConversationManager currentConversationManager,
                                   ConversationIdValidator conversationIdValidator,
                                   CommunicationActivityManager communicationActivityManager) {
        super(bus, delegate, userManager, communicationActivityManager);
        mCurrentConversationManager = currentConversationManager;
        mConversationIdValidator = conversationIdValidator;
        mConversationsTitle = mActivityDelegate.getActivity().getString(
                R.string.title_activity_conversation);
    }

    @Override
    public void activate() {
        super.activate();
        mOriginalTitle = mTitle = mActivityDelegate.getActivity().getTitle();
        LogUtils.v("Title: activate " + mOriginalTitle + ":" + mTitle + ":" + mConversationsTitle);
    }

    @Override
    public void reloadCurrentBox() {
        Conversation currentConversation = mCurrentConversationManager.getCurrentConversation();
        if (currentConversation == null) {
            return;
        }
        mCurrentConversationManager.loadConversation(currentConversation.getId());
    }

    @Override
    public boolean isInCurrentCommunicationBox(Message message) {
        Conversation conversation = mCurrentConversationManager.getCurrentConversation();
        return conversation != null && conversation.getId().equals(message.getConversationID());
    }

    @Override
    public void processMessageInCurrentCommunicationBox(Message message) {

    }

    @Override
    public void logout() {
        mCurrentConversationManager.removeCurrentConversation();
    }

    @Override
    public void deactivate() {
        super.deactivate();
        mCurrentConversationManager.removeCurrentConversation();
    }

    @Override
    public void onResume() {
        super.onResume();
        mCurrentConversationManager.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mCurrentConversationManager.onPause();
        if (mCurrentConversationManager.getCurrentConversation() != null)
            mCommunicationActivityManager.setString(R.string.current_conversation_key, mCurrentConversationManager.getCurrentConversation().getId());
    }

    @Override
    public void onPostResume() {
        if (mRequestedUserHashKey != null) {
            LogUtils.v("Debug request open conversation: onPostResume 1");

            mCurrentConversationManager.loadConversationForUser(mRequestedUserHashKey);
        } else if (mRequestedConversationKey != null) {
            LogUtils.v("Debug request open conversation: onPostResume 2 " + mRequestedConversationKey);

            mActivityDelegate.getDrawerLayout().closeDrawer(DRAWER_GRAVITY_CONVERSATIONS);
            mCurrentConversationManager.loadConversation(mRequestedConversationKey);
            mRequestedConversationKey = null;
        } else if (mCurrentConversationManager.getCurrentConversation() == null) {
            LogUtils.v("Debug request open conversation: onPostResume 3");
            mActivityDelegate.setProgressText(R.string.message_select_conversation, false);
            mActivityDelegate.getDrawerLayout().openDrawer(DRAWER_GRAVITY_CONVERSATIONS);
        }

    }

    @Override
    public boolean isSecondaryDrawerOpening() {
        return false;
    }

    @Override
    public int getCommunicationBoxDrawerGravity() {
        return DRAWER_GRAVITY_CONVERSATIONS;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = mActivityDelegate.getActivity().getIntent();
        String action = intent.getAction();

        String recentConversationKey = mCommunicationActivityManager.getString(R.string.current_conversation_key, null);
        if (mConversationIdValidator.isValid(recentConversationKey)) {
            mRequestedConversationKey = recentConversationKey;
            LogUtils.v("Debug request open conversation: Set recent conversation");
        }

        if (CommunicationActivity.ACTION_OPEN_CONVERSATION.equals(action)) {
            mRequestedConversationKey = intent.getStringExtra(CommunicationActivity.CONVERSATION_ID);
            LogUtils.v("Debug request open conversation: intent.getStringExtra(CommunicationActivity.CONVERSATION_ID) " + intent.getStringExtra(CommunicationActivity.CONVERSATION_ID));
            LogUtils.v("Debug request open conversation: Set open conversation " + mRequestedConversationKey);
        }

    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String action = intent.getAction();
        if (CommunicationActivity.ACTION_OPEN_CONVERSATION.equals(action)) {
            LogUtils.v("Debug request open conversation: Set open conversation onNewIntent");
            mRequestedConversationKey = intent.getStringExtra(CommunicationActivity.CONVERSATION_ID);
        }
    }

    @Override
    public void onDestroy() {
        mCurrentConversationManager.onDestroy();
    }

    @Override
    protected NotificationFragment getNotificationSettingFragment() {
        return NotificationFragment.newInstance(mCurrentConversationManager.getCurrentConversation().getId());
    }

    @Override
    public ActionBarDrawerToggle getDrawerToggleListener() {
        final AppCompatActivity activity = mActivityDelegate.getActivity();
        final DrawerLayout drawerLayout = mActivityDelegate.getDrawerLayout();
        final ActionBar actionBar = activity.getSupportActionBar();
        return new ActionBarDrawerToggle(activity,
                drawerLayout,
                R.drawable.ic_drawer,
                R.string.message_drawer_open,
                R.string.message_drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                LogUtils.v("Title: onDrawerClosed " + mOriginalTitle + ":" + mTitle + ":" + mConversationsTitle);
                actionBar.setTitle(mTitle);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                LogUtils.v("Title: onDrawerOpened " + mOriginalTitle + ":" + mTitle + ":" + mConversationsTitle);
                actionBar.setTitle(mConversationsTitle);
                invalidateOptionsMenu();
            }
        };
    }

    @Override
    public int getResourceStringNoCommunicationBox() {
        return R.string.message_select_conversation;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem notificationItem = menu.findItem(R.id.manage_notification);
        if (notificationItem != null) {
            if (mCurrentConversationManager.getCurrentConversation() == null
                    || !mCurrentConversationManager.getCurrentConversation().allowShowNotification(mUserManager.getCurrentUser())
                    || mUserManager.getCurrentUser() == null) {
                notificationItem.setVisible(false);
            } else {
                notificationItem.setVisible(true);
            }
        }

        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Conversation conversation = mCurrentConversationManager.getCurrentConversation();
        if (conversation != null) {
            outState.putString(EXTRA_CURRENT_CONVERSATION_ID, conversation.getId());
        }
    }

    ///////////////////////////////////////////////////
    // Conversation events
    ///////////////////////////////////////////////////
    @Subscribe
    public void onUserSelectedConversationEvent(UserSelectedConversationEvent event) {
        LogUtils.v("Test ACK onUserSelectedConversationEvent");
        mActivityDelegate.getDrawerLayout().closeDrawers();
        String conversationId = event.getConversationId();
        loadConversation(conversationId);
    }

    @Subscribe
    public void onCurrentConversationChanged(CurrentConversationEvent event) {
        super.onCurrentCommunicationChanged(event);
        switch (event.getStatus()) {
            case REMOVED:
                LogUtils.v("Title: Conversation REMOVED " + mOriginalTitle + ":" + mTitle + ":" + mConversationsTitle);
                setTitle(mOriginalTitle.toString());
                mActivityDelegate.setProgressText(R.string.message_select_conversation, false);
                invalidateOptionsMenu();
                break;
            case LOADING:
                mActivityDelegate.setProgressText(R.string.message_loading_conversation, true);
                mActivityDelegate.getDrawerLayout().closeDrawers();
                break;
            case LOADED:
                Conversation conversation = event.getConversation();
                LogUtils.v("Title: Conversation LOADED " + conversation.getConversationAlias(mUserManager.getCurrentUser().getId()));
                setTitle(conversation.getConversationAlias(mUserManager.getCurrentUser().getId()));
                mRequestedUserHashKey = null;
                markNotificationRead(conversation.getId());
                updateConversationUnreadCount(conversation.getId(), 0);
                //When open conversation, this means user read all unread messages
                AckConversationIntentService.ack(mActivityDelegate.getActivity(), conversation.getId());
                invalidateOptionsMenu();
                cancelNotification();
                break;
            case UPDATED:
                invalidateOptionsMenu();
                break;
        }
    }

    @Subscribe
    public void onUpdateSubscriptionEvent(UpdateSubscriptionEvent event) {
        if (event.getStatus() == UpdateSubscriptionEvent.Status.SUCCEED) {
            boolean set = UpdateNotificationSettingsService.ACTION_SUBSCRIBE.equals(event.getAction()) ? true : false;
            if (event.getException() == null) {

                Conversation currentConversation = mCurrentConversationManager.getCurrentConversation();
                if (currentConversation == null) return;
                User me = currentConversation.getMe(mUserManager.getCurrentUser());
                if (me != null) {
                    if (UpdateNotificationSettingsService.TARGET_EMAIL.equals(event.getSubscriptionResponse().getData().get("type"))) {
                        me.setConversationEmailNotification(set);
                    } else {
                        me.setConversationPushNotification(set);
                    }
                }

            }
            invalidateOptionsMenu();

        } else if (event.getStatus() == UpdateSubscriptionEvent.Status.FAILED) {
            if (event.getException() != null &&
                    event.getException() instanceof ApiManager.NotVerifiedEmailException) {
                Toast.makeText(mActivityDelegate.getActivity(), getString(R.string.error_email_verify), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Subscribe
    public void onPostAuthentication(PostAuthenticationEvent event) {
        if (event.getStatus() == PostAuthenticationEvent.Status.SUCCEED) {
            invalidateOptionsMenu();
            reloadCurrentBox();
        } else {
            mActivityDelegate.getActivity().finish();
        }
    }

    private void loadConversation(String conversationId) {
        mCurrentConversationManager.loadConversation(conversationId);
    }

    private void markNotificationRead(String conversationID) {
        Uri uri = ChatWingContentProvider.getNotificationMessagesUri();

        ContentResolver contentResolver = mActivityDelegate.getActivity().getContentResolver();
        contentResolver.delete(uri, NotificationMessagesTable.CONVERSATION_ID + "==\"" + conversationID + "\"", null);
    }

    private void cancelNotification() {
        if (mCurrentConversationManager.getCurrentConversation() == null) return;
        NotificationManager notificationManager = (NotificationManager) mActivityDelegate.getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(mCurrentConversationManager.getCurrentConversation().getId().hashCode());
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        Intent intent = mActivityDelegate.getActivity().getIntent();
        String action = intent.getAction();

        if (!CommunicationActivity.ACTION_OPEN_CONVERSATION.equals(action) && //Make sure this wont override open action
                savedInstanceState != null
                && savedInstanceState.containsKey(EXTRA_CURRENT_CONVERSATION_ID)) {
            LogUtils.v("Debug request open conversation: set mRequestedConversationKey onRestoreInstanceState");

            mRequestedConversationKey = savedInstanceState.getString(EXTRA_CURRENT_CONVERSATION_ID);
        }
    }
}
