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

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

import com.chatwing.whitelabel.Constants;
import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.activities.BaseABFragmentActivity;
import com.chatwing.whitelabel.contentproviders.ChatWingContentProvider;
import com.chatwing.whitelabel.events.ChatBoxUnreadCountChangedEvent;
import com.chatwing.whitelabel.events.CurrentCommunicationEvent;
import com.chatwing.whitelabel.fragments.CommunicationMessagesFragment;
import com.chatwing.whitelabel.fragments.NotificationFragment;
import com.chatwing.whitelabel.pojos.Message;
import com.chatwing.whitelabel.services.SyncCommunicationBoxesIntentService;
import com.chatwing.whitelabel.tables.ChatBoxTable;
import com.chatwing.whitelabel.tables.ConversationTable;
import com.chatwing.whitelabel.utils.JsonConstantsProvider;
import com.chatwing.whitelabel.utils.LogUtils;
import com.google.gson.Gson;
import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by cuongthai on 14/04/2014.
 */
public abstract class CommunicationModeManager {
    private static final String MANAGE_NOTIFICATION_TAG = "MANAGE_NOTIFICATION_TAG";

    private static final String EXTRA_OPENING_VIDEO_URL = "opening_video_url";

    private final Bus mBus;
    protected final Delegate mActivityDelegate;
    protected final UserManager mUserManager;
    protected final CommunicationActivityManager mCommunicationActivityManager;
    protected CharSequence mOriginalTitle;
    protected CharSequence mTitle;
    private String mOpeningVideoUrl;
    private boolean mIsRegisteredToBus;
    protected boolean mIsActive;

    public CommunicationModeManager(Bus bus,
                                    Delegate delegate,
                                    UserManager userManager,
                                    CommunicationActivityManager communicationActivityManager) {
        mBus = bus;
        mActivityDelegate = delegate;
        mUserManager = userManager;
        mIsRegisteredToBus = false;
        mIsActive = false;
        mCommunicationActivityManager = communicationActivityManager;
    }


    protected void onCurrentCommunicationChanged(CurrentCommunicationEvent event) {
        LogUtils.v("Test Chatbox not display, onCurrentCommunicationChanged: " + event.getStatus());

        switch (event.getStatus()) {
            case REMOVED:
            case LOADING:
                mActivityDelegate.setContentShown(false);
                break;
            case UI_LOADED:
                mActivityDelegate.setContentShown(true);
                break;
        }
    }

    public abstract void logout();

    public abstract void reloadCurrentBox();

    public abstract ActionBarDrawerToggle getDrawerToggleListener();

    public abstract int getResourceStringNoCommunicationBox();

    public boolean isCommunicationBoxDrawerOpening() {
        return mActivityDelegate.getDrawerLayout().isDrawerOpen(getCommunicationBoxDrawerGravity());
    }

    public boolean onCreateOptionsMenu(Menu menu){
        ActionBarActivity activity = mActivityDelegate.getActivity();
        activity.getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    public boolean onPrepareOptionsMenu(Menu menu){
        return false;
    }

    public boolean onOptionsItemSelected(MenuItem item){
        if (item.getItemId() == R.id.manage_notification) {
            manageNotification();
            return true;
        }
        return false;
    }

    public abstract boolean isSecondaryDrawerOpening();

    public abstract boolean isInCurrentCommunicationBox(Message message);

    public boolean processMessageNotInCurrentCommunicationBox(Message message) {
        Uri uri;
        String unreadColumn;
        if (message.isPrivate()) {
            uri = ChatWingContentProvider.getConversationWithIdUri(message.getConversationID());
            unreadColumn = ConversationTable.UNREAD_COUNT;
        } else {
            uri = ChatWingContentProvider.getChatBoxWithIdUri(message.getChatBoxId());
            unreadColumn = ChatBoxTable.UNREAD_COUNT;
        }

        ActionBarActivity activity = mActivityDelegate.getActivity();
        ContentResolver contentResolver = activity.getContentResolver();

        // Get unread count of the correct chat box.
        Cursor cursor = null;
        int unreadCount = -1;
        try {
            cursor = contentResolver.query(
                    uri,
                    new String[]{unreadColumn},
                    null, null, null);
            if (cursor.getCount() > 0 && cursor.moveToFirst()) {
                unreadCount = cursor.getInt(cursor.getColumnIndex(unreadColumn));
            }
            // else {
            // The chat box is not available in the DB.
            // TODO: may create the chat box or trigger a sync operation.
            // }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        if (unreadCount == -1) {
            // 1. Something was wrong (the chat box id is invalid
            // or can't query the chat box from DB).
            // Let's stop.
            // 2. Conversation not found. There might be a case client received
            // a msg but there is not conversation created beforehand. We can
            // just ignore that and wait for "remote_unread"
            if (message.isPrivate()) {
                Intent intent = new Intent(activity, SyncCommunicationBoxesIntentService.class);
                intent.putExtra(SyncCommunicationBoxesIntentService.UPDATE_CATEGORIES_FLAG, false);
                intent.putExtra(SyncCommunicationBoxesIntentService.UPDATE_CONVERSATION_FLAG, true);
                activity.startService(intent);
            }
            return false;
        }

        // Increase unread count in DB
        if (message.isPrivate()) {
            return updateConversationUnreadCount(message.getConversationID(), ++unreadCount);
        } else {
            return updateChatBoxUnreadCountInDB(message.getChatBoxId(), ++unreadCount);
        }
    }

    protected boolean updateChatBoxUnreadCountInDB(int chatBoxId, int unreadCount) {
        Uri uri = ChatWingContentProvider.getChatBoxWithIdUri(chatBoxId);
        ContentValues contentValues = new ContentValues();
        contentValues.put(ChatBoxTable.UNREAD_COUNT, unreadCount);

        ContentResolver contentResolver = mActivityDelegate.getActivity().getContentResolver();
        int updated = contentResolver.update(uri, contentValues, null, null);
        if (updated == 1) {
            mBus.post(new ChatBoxUnreadCountChangedEvent(chatBoxId));

            contentResolver.notifyChange(ChatWingContentProvider.getSyncedBookmarksUri(), null);
            contentResolver.notifyChange(ChatWingContentProvider.getAggregatedCategoriesUri(), null);
            contentResolver.notifyChange(ChatWingContentProvider.getCategorizedChatBoxesUri(), null);
            return true;
        } else {
            LogUtils.e("Failed to update unread count.");
            return false;
        }
    }

    protected boolean updateConversationUnreadCount(String conversationId, int unreadCount) {
        Uri uri = ChatWingContentProvider.getConversationWithIdUri(conversationId);
        ContentValues contentValues = new ContentValues();
        LogUtils.v("updateConversationUnreadCount "+unreadCount);
        contentValues.put(ConversationTable.UNREAD_COUNT, unreadCount);
        contentValues.put(ConversationTable.DATE_UPDATED, System.currentTimeMillis());

        int updated = mActivityDelegate.getActivity().getContentResolver()
                .update(uri, contentValues, null, null);
        if (updated == 1) {
            return true;
        } else {
            LogUtils.e("Failed to update unread count.");
            return false;
        }
    }

    public void onNewIntent(Intent intent) {

    }

    public abstract void processMessageInCurrentCommunicationBox(Message message);

    public static interface Delegate {
        public static final String EXTRA_CHAT_BOX_ID = "chat_box_id";

        public static final int REQUEST_SEARCH_CHAT_BOX = 2;
        public static final int REQUEST_CREATE_CHAT_BOX = 3;

        public void handle(Exception exception, int errorMessageResId);

        public BaseABFragmentActivity getActivity();

        public CommunicationMessagesFragment getCommunicationMessagesFragment();

        public void setProgressText(int resId, boolean showProgressBar);

        public void setContentShown(boolean show);

        public DrawerLayout getDrawerLayout();

        public void ensureWebViewAndSubscribeToChannels();



    }

    /**
     * Required methods for {@link com.chatwing.whitelabel.activities.CommunicationActivity}
     * we try to have separate handlers for ChatMode or ConversationMode.
     */
    public abstract void onPostResume();

    public abstract void onDestroy();

    protected abstract NotificationFragment getNotificationSettingFragment();

    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(EXTRA_OPENING_VIDEO_URL)) {
                mOpeningVideoUrl = savedInstanceState.getString(EXTRA_OPENING_VIDEO_URL);
            }
        }
    }

    protected void invalidateOptionsMenu() {
        mActivityDelegate.getActivity().invalidateOptionsMenu();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!mIsActive) {
            return;
        }
    }

    public void onResume() {
    }

    public void onPause() {
    }

    public void onSaveInstanceState(Bundle outState) {
        if (!TextUtils.isEmpty(mOpeningVideoUrl)) {
            outState.putString(EXTRA_OPENING_VIDEO_URL, mOpeningVideoUrl);
        }
    }

    public void activate() {
        mIsActive = true;
        registerToBus();
        mOriginalTitle = mTitle = mActivityDelegate.getActivity().getTitle();
        mActivityDelegate.setProgressText(getResourceStringNoCommunicationBox(), false);
        setSubTitle(null);
        openCommunicationBoxDrawer();
        invalidateOptionsMenu();
    }

    public void deactivate() {
        mIsActive = false;
        unregisterToBus();
    }

    protected String getString(int id) {
        return mActivityDelegate.getActivity().getString(id);
    }

    protected void setTitle(String title) {
        mTitle = title;
        mActivityDelegate.getActivity().getSupportActionBar().setTitle(title);
    }

    protected void setSubTitle(String subTitle) {
        if(Constants.SHOW_CHAT_BOX_URL) {
            mActivityDelegate.getActivity().getSupportActionBar().setSubtitle(subTitle);
        }else{
            mActivityDelegate.getActivity().getSupportActionBar().setSubtitle(null);
        }
    }

    public Map<String, String> getEmoticons() {
        return new Gson().fromJson(JsonConstantsProvider.DEFAULT_COMMUNICATION_EMOTION_JSON, HashMap.class);
    }

    public void openCommunicationBoxDrawer() {
        mActivityDelegate.getDrawerLayout().openDrawer(getCommunicationBoxDrawerGravity());
        mActivityDelegate.getActivity().invalidateOptionsMenu();
    }

    protected abstract int getCommunicationBoxDrawerGravity();

    private void registerToBus() {
        if (mIsActive && !mIsRegisteredToBus) {
            mBus.register(this);
            mIsRegisteredToBus = true;
        }
    }

    private void unregisterToBus() {
        if (mIsRegisteredToBus) {
            mBus.unregister(this);
            mIsRegisteredToBus = false;
        }
    }

    public void subscribeToChannels(WebView mWebView) {
        if (mWebView == null) {
            return;
        }
        subscribeToChatBoxChannels(mWebView);
        subscribeToConversationChannels(mWebView);
    }

    private void subscribeToChatBoxChannels(WebView mWebView) {
        // Query for chat box keys
        Uri chatBoxesUri = ChatWingContentProvider.getChatBoxesUri();
        ArrayList<String> fayeChannels = new ArrayList<String>();
        Cursor c = null;
        try {
            c = mActivityDelegate.getActivity().getContentResolver().query(
                    chatBoxesUri,
                    new String[]{ChatBoxTable.FAYE_CHANNEL},
                    null, null, null);
            if (c.getCount() > 0 && c.moveToFirst()) {
                int fayeChannelIndex = c.getColumnIndex(ChatBoxTable.FAYE_CHANNEL);
                do {
                    // We can subscribe to each chat box here,
                    // but it can take a lot of time and holding the Cursor for
                    // that long is not a good idea. So, to be safe,
                    // let's get all chat box keys first and subscribe later.
                    String fayeChannel = c.getString(fayeChannelIndex);
                    fayeChannels.add(fayeChannel);
                } while (c.moveToNext());
            }
        } finally {
            if (c != null && !c.isClosed()) {
                c.close();
            }
        }
        LogUtils.v("Test Duplicate subscribeToChatBoxChannels "+fayeChannels.size()+":"+mWebView);

        // Subscribe to all of them
        for (String fayeChannel : fayeChannels) {
            String js = String.format("javascript:subscribe('%s')", fayeChannel);
            mWebView.loadUrl(js);
            mWebView.loadUrl(js);
        }
    }

    public void unsubscribeToChannels(WebView mWebView) {
        if (mWebView == null) {
            return;
        }
        unsubscribeToChatBoxChannels(mWebView);
        unsubscribeToConversationChannels(mWebView);
    }

    private void unsubscribeToChatBoxChannels(WebView mWebView) {
        // Subscribe to all of them
        ArrayList<String> allFayeChannels = getAllFayeChannels();
        LogUtils.v("Test Duplicate unsubscribeToChatBoxChannels "+allFayeChannels.size());

        for (String fayeChannel : allFayeChannels) {
            String js = String.format("javascript:unsubscribe('%s')", fayeChannel);
            mWebView.loadUrl(js);
        }
    }

    private void unsubscribeToConversationChannels(WebView mWebView) {
        if (mUserManager.getCurrentUser() == null) {
            return;
        }
        LogUtils.v("Test Duplicate unsubscribeToConversationChannels");

        String js = String.format("javascript:unsubscribe('/user/%s')",
                mUserManager.getCurrentUser().getId());
        mWebView.loadUrl(js);
    }

    private void subscribeToConversationChannels(WebView mWebView) {
        if (mUserManager.getCurrentUser() == null) {
            return;
        }
        LogUtils.v("Test Duplicate subscribeToConversationChannels");
        String js = String.format("javascript:subscribe('/user/%s')",
                mUserManager.getCurrentUser().getId());
        mWebView.loadUrl(js);
    }

    private void manageNotification() {
        FragmentManager supportFragmentManager = mActivityDelegate.getActivity().getSupportFragmentManager();
        Fragment notificationFragment = supportFragmentManager.findFragmentByTag(MANAGE_NOTIFICATION_TAG);
        if (notificationFragment != null) {
            supportFragmentManager.beginTransaction().remove(notificationFragment).commit();
        }
        supportFragmentManager.beginTransaction()
                .add(getNotificationSettingFragment(), MANAGE_NOTIFICATION_TAG)
                .addToBackStack(null)
                .commit();
    }

    private ArrayList<String> getAllFayeChannels() {
        // Query for chat box keys
        Uri chatBoxesUri = ChatWingContentProvider.getChatBoxesUri();
        ArrayList<String> fayeChannels = new ArrayList<String>();
        Cursor c = null;
        try {
            c = mActivityDelegate.getActivity().getContentResolver().query(
                    chatBoxesUri,
                    new String[]{ChatBoxTable.FAYE_CHANNEL},
                    null, null, null);
            if (c.getCount() > 0 && c.moveToFirst()) {
                int fayeChannelIndex = c.getColumnIndex(ChatBoxTable.FAYE_CHANNEL);
                do {
                    // We can subscribe to each chat box here,
                    // but it can take a lot of time and holding the Cursor for
                    // that long is not a good idea. So, to be safe,
                    // let's get all chat box keys first and subscribe later.
                    String fayeChannel = c.getString(fayeChannelIndex);
                    fayeChannels.add(fayeChannel);
                } while (c.moveToNext());
            }
        } finally {
            if (c != null && !c.isClosed()) {
                c.close();
            }
        }
        return fayeChannels;
    }
}
