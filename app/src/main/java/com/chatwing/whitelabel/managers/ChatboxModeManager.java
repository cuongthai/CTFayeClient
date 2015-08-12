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
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.Toast;

import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.activities.CommunicationActivity;
import com.chatwing.whitelabel.contentproviders.ChatWingContentProvider;
import com.chatwing.whitelabel.events.CurrentChatBoxEvent;
import com.chatwing.whitelabel.events.LoadCurrentChatBoxFailedEvent;
import com.chatwing.whitelabel.events.UpdateSubscriptionEvent;
import com.chatwing.whitelabel.events.UserSelectedChatBoxEvent;
import com.chatwing.whitelabel.fragments.NotificationFragment;
import com.chatwing.whitelabel.pojos.ChatBox;
import com.chatwing.whitelabel.pojos.Event;
import com.chatwing.whitelabel.pojos.Message;
import com.chatwing.whitelabel.services.AckChatboxIntentService;
import com.chatwing.whitelabel.tables.MessageTable;
import com.chatwing.whitelabel.tables.NotificationMessagesTable;
import com.chatwing.whitelabel.utils.LogUtils;
import com.chatwing.whitelabel.validators.ChatBoxIdValidator;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

/**
 * Created by cuongthai on 17/08/2014.
 */
public class ChatboxModeManager extends CommunicationModeManager {
    public static final int DRAWER_GRAVITY_CHAT_BOXES = Gravity.LEFT;

    protected final CurrentChatBoxManager mCurrentChatBoxManager;
    protected final ApiManager mApiManager;
    private final ChatBoxIdValidator mChatBoxIdValidator;
    protected int mRequestedChatboxId;
    private final CharSequence mChatBoxesTitle;


    public ChatboxModeManager(Bus bus,
                              Delegate delegate,
                              UserManager userManager,
                              ApiManager apiManager,
                              CurrentChatBoxManager currentChatBoxManager,
                              ChatBoxIdValidator chatBoxIdValidator,
                              CommunicationActivityManager communicationActivityManager) {
        super(bus, delegate, userManager, communicationActivityManager);
        mApiManager = apiManager;
        mCurrentChatBoxManager = currentChatBoxManager;
        mChatBoxesTitle = getString(R.string.title_chat_boxes);
        mChatBoxIdValidator = chatBoxIdValidator;

    }

    @Override
    public void deactivate() {
        super.deactivate();
        mCurrentChatBoxManager.removeCurrentChatBox();
        mCurrentChatBoxManager.resetLastKnownChatBox();
    }

    @Override
    public void onResume() {
        super.onResume();
        mCurrentChatBoxManager.onResume();
    }

    @Override
    public boolean isInCurrentCommunicationBox(Message message) {
        ChatBox currentChatBox = mCurrentChatBoxManager.getCurrentChatBox();
        return currentChatBox != null && currentChatBox.getId() == message.getChatBoxId();
    }

    @Override
    public void reloadCurrentBox() {
        if (mCurrentChatBoxManager.getCurrentChatBox() != null) {
            // We will recieve onSyncCompletedEvent and reload current chatbox there
            mActivityDelegate.setProgressText(R.string.progress_loading_chat_box_detail, true);
        }
        ChatBox chatbox = mCurrentChatBoxManager.getLastKnownGoodChatBox();
        if (chatbox == null) return;
        LogUtils.v("Duplicate load reloadCurrentBox");

        //We should use loadById since LightWeightChatbox version is stored in db to keep it consistent with
        //onSearchResult
        mCurrentChatBoxManager.loadCurrentChatBox(
                chatbox.getId());
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem notificationItem = menu.findItem(R.id.manage_notification);
        if (notificationItem != null) {
            if (mCurrentChatBoxManager.getCurrentChatBox() == null
                    || mCurrentChatBoxManager.getCurrentChatBox().getNotificationStatus() == null
                    || mUserManager.getCurrentUser() == null) {
                notificationItem.setVisible(false);
            } else {
                notificationItem.setVisible(true);
            }
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void logout() {
        mCurrentChatBoxManager.removeCurrentChatBox();
    }

    @Override
    public void onPostResume() {
        if (mRequestedChatboxId != 0) {
            LogUtils.v("Duplicate load onPostResume");
            mCurrentChatBoxManager.loadCurrentChatBox(mRequestedChatboxId);
            mRequestedChatboxId = 0;
            mActivityDelegate.getDrawerLayout().closeDrawer(DRAWER_GRAVITY_CHAT_BOXES);
        } else if (mCurrentChatBoxManager.getCurrentChatBox() == null) {
            mActivityDelegate.getDrawerLayout().openDrawer(DRAWER_GRAVITY_CHAT_BOXES);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mCurrentChatBoxManager.onPause();
        if (mCurrentChatBoxManager.getCurrentChatBox() != null)
            mCommunicationActivityManager.setInt(R.string.current_chatbox_id, mCurrentChatBoxManager.getCurrentChatBox().getId());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = mActivityDelegate.getActivity().getIntent();
        String action = intent.getAction();

        int recentChatboxID = mCommunicationActivityManager.getInt(R.string.current_chatbox_id, 0);
        if (mChatBoxIdValidator.isValid(recentChatboxID)) {
            mRequestedChatboxId = recentChatboxID;
        }

        if (CommunicationActivity.ACTION_OPEN_CHATBOX.equals(action)) {
            mRequestedChatboxId = intent.getIntExtra(CommunicationActivity.CHATBOX_ID, 0);
        }


    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ChatBox chatBox = mCurrentChatBoxManager.getCurrentChatBox();
        if (chatBox != null) {
            outState.getInt(Delegate.EXTRA_CHAT_BOX_ID, chatBox.getId());
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String action = intent.getAction();
        LogUtils.v("GCM onNewIntent action" + action);

        if (CommunicationActivity.ACTION_OPEN_CHATBOX.equals(action)) {
            mRequestedChatboxId = intent.getIntExtra(CommunicationActivity.CHATBOX_ID, 0);
            LogUtils.v("GCM onNewIntent action mRequestedChatboxId " + mRequestedChatboxId);
        }
    }

    @Override
    public void processMessageInCurrentCommunicationBox(Message message) {
    }

    @Override
    public void onDestroy() {
        mCurrentChatBoxManager.onDestroy();
    }

    @Override
    protected NotificationFragment getNotificationSettingFragment() {
        return NotificationFragment.newInstance(mCurrentChatBoxManager.getCurrentChatBox().getId());
    }

    @Override
    public ActionBarDrawerToggle getDrawerToggleListener() {
        final AppCompatActivity activity = mActivityDelegate.getActivity();
        final DrawerLayout drawerLayout = mActivityDelegate.getDrawerLayout();
        return new ActionBarDrawerToggle(activity,
                drawerLayout,
                R.drawable.ic_drawer,
                R.string.message_drawer_open,
                R.string.message_drawer_close) {
            @Override
            public void onDrawerStateChanged(int newState) {
                if (newState == DrawerLayout.STATE_IDLE) {
                    CharSequence title;
                    if (drawerLayout.isDrawerOpen(DRAWER_GRAVITY_CHAT_BOXES)) {
                        title = mChatBoxesTitle;
                    } else {
                        title = mTitle;
                    }
                    activity.getSupportActionBar().setTitle(title);
                    invalidateOptionsMenu();
                }
            }
        };
    }

    @Override
    public boolean isSecondaryDrawerOpening() {
        return false;
    }

    @Override
    public int getResourceStringNoCommunicationBox() {
        return R.string.message_select_chat_box;
    }

    @Override
    public void processDeleteMessageEvent(Event event) {
        Message message = (Message) event.getParams();
        mActivityDelegate.getActivity().getContentResolver().delete(
                ChatWingContentProvider.getMessagesUri(),
                MessageTable._ID + "='" + message.getId() + "'",
                null
        );
        mActivityDelegate.getCommunicationMessagesFragment().deleteMessage(message);
    }

    @Override
    public void processDeleteMessagesBySocialAccountEvent(Event event) {
        Message message = (Message) event.getParams();
        mActivityDelegate.getActivity().getContentResolver().delete(
                ChatWingContentProvider.getMessagesUri(),
                MessageTable.CHAT_BOX_ID + "=" + message.getChatBoxId()
                        + " AND " + MessageTable.LOGIN_TYPE + "='" + message.getUserType() + "'",
                null
        );
        mActivityDelegate.getCommunicationMessagesFragment().deleteMessageBySocialAccount(message);
    }

    @Override
    public void processDeleteMessagesByIPEvent(Event event) {
        Message message = (Message) event.getParams();
        int delete = mActivityDelegate.getActivity().getContentResolver().delete(
                ChatWingContentProvider.getMessagesUri(),
                MessageTable.CHAT_BOX_ID + "=" + message.getChatBoxId()
                        + " AND " + MessageTable.IP + "='" + message.getIp() + "'",
                null
        );
        mActivityDelegate.getCommunicationMessagesFragment().deleteMessageByIp(message);
    }

    @Override
    public int getCommunicationBoxDrawerGravity() {
        return DRAWER_GRAVITY_CHAT_BOXES;
    }


    ///////////////////////////////////////////////////
    // ChatBox events
    ///////////////////////////////////////////////////
    @Subscribe
    public void onUpdateSubscriptionEvent(UpdateSubscriptionEvent event) {
        if (event.getStatus() == UpdateSubscriptionEvent.Status.FAILED) {
            if (event.getException() != null &&
                    event.getException() instanceof ApiManager.NotVerifiedEmailException) {
                Toast.makeText(mActivityDelegate.getActivity(), getString(R.string.error_email_verify), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Subscribe
    public void onUserSelectedChatBox(UserSelectedChatBoxEvent event) {
        mActivityDelegate.getDrawerLayout().closeDrawers();
        int chatBoxId = event.getChatBoxId();
        LogUtils.v("Duplicate load onUserSelectedChatBox");
        mCurrentChatBoxManager.loadCurrentChatBox(chatBoxId);
    }

    private void markNotificationRead(int chatBoxId) {
        Uri uri = ChatWingContentProvider.getNotificationMessagesUri();

        ContentResolver contentResolver = mActivityDelegate.getActivity().getContentResolver();
        contentResolver.delete(uri, NotificationMessagesTable.CHAT_BOX_ID + "==" + chatBoxId, null);
    }

    @Subscribe
    public void onLoadCurrentChatBoxFailed(LoadCurrentChatBoxFailedEvent event) {
        if (event.getException() instanceof ApiManager.NotVerifiedEmailException) {
            mActivityDelegate.setProgressText(R.string.message_select_chat_box, false);
            mActivityDelegate.handle(event.getException(), R.string.error_email_verify);
            return;
        }
        //We remove the loading lock so that cache content may display when there is issue
        mActivityDelegate.setContentShown(true);
        mActivityDelegate.handle(event.getException(), R.string.error_failed_to_load_chat_box_details);
    }

    @Subscribe
    public void onCurrentChatBoxChanged(CurrentChatBoxEvent event) {
        super.onCurrentCommunicationChanged(event);
        switch (event.getStatus()) {
            case REMOVED:

                mActivityDelegate.setProgressText(R.string.message_select_chat_box, false);
                setTitle(mOriginalTitle.toString());
                setSubTitle(null);
                invalidateOptionsMenu();
                break;
            case LOADING:
                mActivityDelegate.setProgressText(R.string.message_loading_chatbox, true);
                mActivityDelegate.getDrawerLayout().closeDrawers();
                break;
            case LOADED:
                invalidateOptionsMenu();
                subscribeToCurrentChatBox();
                markNotificationRead(event.getChatbox().getId());
                updateChatBoxUnreadCountInDB(event.getChatbox().getId(), 0);
                //When open conversation, this means user read all unread messages
                AckChatboxIntentService.ack(mActivityDelegate.getActivity(), event.getChatbox().getId());
                cancelNotification();
                break;
            case UPDATED:
                invalidateOptionsMenu();
                break;
        }
    }

    private void cancelNotification() {
        if (mCurrentChatBoxManager.getCurrentChatBox() == null) return;
        NotificationManager notificationManager = (NotificationManager) mActivityDelegate.getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(mCurrentChatBoxManager.getCurrentChatBox().getId());
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        Intent intent = mActivityDelegate.getActivity().getIntent();
        String action = intent.getAction();

        if (!CommunicationActivity.ACTION_OPEN_CHATBOX.equals(action) && //Make sure this wont override open action
                savedInstanceState != null
                && savedInstanceState.containsKey(Delegate.EXTRA_CHAT_BOX_ID)) {
            mRequestedChatboxId = savedInstanceState.getInt(Delegate.EXTRA_CHAT_BOX_ID);
        }
    }

    private void subscribeToCurrentChatBox() {
        WebView webView = mActivityDelegate.getFayeWebView();
        if (webView == null) {
            mActivityDelegate.ensureWebViewAndSubscribeToChannels();
        } else {
            ChatBox currentChatBox = mCurrentChatBoxManager.getCurrentChatBox();
            if (currentChatBox != null) {
                String js = String.format("javascript:subscribe('%s')", currentChatBox.getFayeChannel());
                webView.loadUrl(js);
            }
        }
    }
}
