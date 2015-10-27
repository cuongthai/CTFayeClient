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

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.OperationApplicationException;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.text.format.DateUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.chatwing.whitelabel.Constants;
import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.activities.AuthenticateActivity;
import com.chatwing.whitelabel.activities.BaseABFragmentActivity;
import com.chatwing.whitelabel.activities.CommunicationActivity;
import com.chatwing.whitelabel.activities.CreateChatBoxActivity;
import com.chatwing.whitelabel.activities.NoMenuWebViewActivity;
import com.chatwing.whitelabel.activities.SearchChatBoxActivity;
import com.chatwing.whitelabel.contentproviders.ChatWingContentProvider;
import com.chatwing.whitelabel.events.AccountSwitchEvent;
import com.chatwing.whitelabel.events.CreateBookmarkEvent;
import com.chatwing.whitelabel.events.CurrentChatBoxEvent;
import com.chatwing.whitelabel.events.LoadCurrentChatBoxFailedEvent;
import com.chatwing.whitelabel.events.LoadOnlineUsersSuccessEvent;
import com.chatwing.whitelabel.events.UpdateSubscriptionEvent;
import com.chatwing.whitelabel.events.UserSelectedChatBoxEvent;
import com.chatwing.whitelabel.events.UserSelectedSongEvent;
import com.chatwing.whitelabel.fragments.AccountDialogFragment;
import com.chatwing.whitelabel.fragments.NotificationFragment;
import com.chatwing.whitelabel.interfaces.FayeReceiver;
import com.chatwing.whitelabel.interfaces.MediaControlInterface;
import com.chatwing.whitelabel.pojos.ChatBox;
import com.chatwing.whitelabel.pojos.Event;
import com.chatwing.whitelabel.pojos.LightWeightChatBox;
import com.chatwing.whitelabel.pojos.Message;
import com.chatwing.whitelabel.pojos.Song;
import com.chatwing.whitelabel.pojos.SyncedBookmark;
import com.chatwing.whitelabel.pojos.User;
import com.chatwing.whitelabel.services.AckChatboxIntentService;
import com.chatwing.whitelabel.services.CreateBookmarkIntentService;
import com.chatwing.whitelabel.services.MusicService;
import com.chatwing.whitelabel.tables.ChatBoxTable;
import com.chatwing.whitelabel.tables.MessageTable;
import com.chatwing.whitelabel.tables.NotificationMessagesTable;
import com.chatwing.whitelabel.tables.SyncedBookmarkTable;
import com.chatwing.whitelabel.utils.LogUtils;
import com.chatwing.whitelabel.validators.ChatBoxIdValidator;
import com.chatwing.whitelabel.validators.PermissionsValidator;
import com.readystatesoftware.viewbadger.BadgeView;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

/**
 * Created by cuongthai on 17/08/2014.
 */
public class ChatboxModeManager extends CommunicationModeManager {
    public static final int DRAWER_GRAVITY_ONLINE_USER = Gravity.RIGHT;
    public static final int DRAWER_GRAVITY_CHAT_BOXES = Gravity.LEFT;
    public static final int MSG_GET_ONLINE_USERS = 0;

    private static final long REFRESH_ONLINE_USERS_INTERVAL = 20 * DateUtils.SECOND_IN_MILLIS;

    private final MediaControlInterface mMediaControlInterface;
    private final CurrentChatBoxManager mCurrentChatBoxManager;
    private final ApiManager mApiManager;
    private final ChatBoxIdValidator mChatBoxIdValidator;
    private final CharSequence mChatBoxesTitle;

    private int mNumOfOnlineUser;
    private int mRequestedChatboxId;

    private BadgeView mOnlineUsersBadgeView;
    private Handler mRefreshOnlineUsersHandler;
    private CommunicationModeManager.Delegate mActivityDelegate;
    private BuildManager mBuildManager;
    private MenuItem mediaAddItem;

    //Handle broadcast event from MusicService
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ChatBox currentChatBox = mCurrentChatBoxManager.getCurrentChatBox();
            if (currentChatBox == null) return;
            updateControlUI(currentChatBox);
        }
    };

    public ChatboxModeManager(Bus bus,
                              Delegate delegate,
                              MediaControlInterface mediaControlInterface,
                              UserManager userManager,
                              ApiManager apiManager,
                              CurrentChatBoxManager currentChatBoxManager,
                              CommunicationActivityManager communicationActivityManager,
                              BuildManager buildManager,
                              ChatBoxIdValidator chatBoxIdValidator) {
        super(bus, delegate, userManager, communicationActivityManager);
        mActivityDelegate = delegate;
        mApiManager = apiManager;
        mCurrentChatBoxManager = currentChatBoxManager;
        mChatBoxesTitle = getString(R.string.title_chat_boxes);
        mChatBoxIdValidator = chatBoxIdValidator;
        mMediaControlInterface = mediaControlInterface;
        mBuildManager = buildManager;
    }

    @Override
    public void activate() {
        super.activate();
        setTitle(getString(R.string.title_chat_boxes));
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

        // Register mMessageReceiver to receive messages.
        LocalBroadcastManager.getInstance(mActivityDelegate.getActivity())
                .registerReceiver(mMessageReceiver,
                        new IntentFilter(MusicService.EVENT_CONTROL_CHANGED));
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

        MenuItem shareChatBoxItem = menu.findItem(R.id.share_chat_box);
        MenuItem copyAliasItem = menu.findItem(R.id.copy_alias);
        MenuItem manageBlackListItem = menu.findItem(R.id.manage_blacklist);
        MenuItem bookmarkChatBoxItem = menu.findItem(R.id.bookmark_chat_box);
        MenuItem onlineUsersItem = menu.findItem(R.id.bookmark_chat_box);

        // Invalidate all menu related objects
        onlineUsersItem.setVisible(false);
        mOnlineUsersBadgeView.hide();
        shareChatBoxItem.setVisible(false);
        copyAliasItem.setVisible(false);
        manageBlackListItem.setVisible(false);
        bookmarkChatBoxItem.setVisible(false);
        mediaAddItem.setVisible(false);

        // Now config them
        if (mCurrentChatBoxManager.getCurrentChatBox() != null) {
            // When main view or online users drawer is opened
            // and current chat box is available.
            onlineUsersItem.setVisible(true);
            shareChatBoxItem.setVisible(true && Constants.ALLOW_SHARE_CHATBOX);
            if (mNumOfOnlineUser > 0) {
                mOnlineUsersBadgeView.setText(Integer.toString(mNumOfOnlineUser));
                mOnlineUsersBadgeView.show();
            }

            // Config share intent for share chat box item
            AppCompatActivity activity = mActivityDelegate.getActivity();
            ChatBox chatBox = mCurrentChatBoxManager.getCurrentChatBox();
            String shareText = mApiManager.getChatBoxUrl(chatBox.getKey());
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.message_share_chat_box_subject));
            intent.putExtra(Intent.EXTRA_TEXT, shareText);
            ShareActionProvider shareChatBoxActionProvider = (ShareActionProvider)
                    MenuItemCompat.getActionProvider(shareChatBoxItem);
            shareChatBoxActionProvider.setShareIntent(intent);


            if (mBuildManager.isOfficialChatWingApp() &&
                    mUserManager.userCanBookmark() &&
                    !ChatWingContentProvider.hasSyncedBookmarkInDB(
                            activity.getContentResolver(),
                            chatBox.getId())) {
                bookmarkChatBoxItem.setVisible(true);
            }

            if (mBuildManager.isSupportedMusicBox()) {
                mediaAddItem.setVisible(true);
            }

            if (chatBox.getAlias() != null && Constants.ALLOW_SHARE_CHATBOX) {
                copyAliasItem.setVisible(true);
            }
            if (mUserManager.userHasPermission(chatBox, PermissionsValidator.Permission.UNBLOCK_USER)) {
                manageBlackListItem.setVisible(true);
            }

            updateControlUI(chatBox);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.bookmark_chat_box:
                bookmarkCurrentChatBox();
                return true;
            case R.id.copy_alias:
                copyAliasCurrentChatBox();
                return true;
            case R.id.manage_blacklist:
                manageBlackList();
                return true;
            case R.id.audio_add:
                ChatBox currentChatBox = mCurrentChatBoxManager.getCurrentChatBox();
                if (currentChatBox != null) {
                    mMediaControlInterface.enqueue(new Song(currentChatBox.getAudioUrl(),
                            currentChatBox.getAudioName(),
                            currentChatBox.getName()));
                    LogUtils.v("mMediaControlInterface.getMediaStatus() " +
                            mMediaControlInterface.getMediaStatus());
                    mMediaControlInterface.playLastMediaIfStopping();

                    invalidateOptionsMenu();
                    setMediaControlVisible(false);
                }

                return true;
            default:
                return false;
        }
    }

    @Override
    public void logout() {
        mCurrentChatBoxManager.removeCurrentChatBox();
    }

    @Override
    public void onPostResume() {

        //TODO Check this
        if (!mIsActive) {
            return;
        }

        if (mRequestedChatboxId != 0) {
            LogUtils.v("Duplicate load onPostResume");
            mCurrentChatBoxManager.loadCurrentChatBox(mRequestedChatboxId);
            mRequestedChatboxId = 0;
            mActivityDelegate.getDrawerLayout().closeDrawer(DRAWER_GRAVITY_CHAT_BOXES);
        } else if (mCurrentChatBoxManager.getCurrentChatBox() == null) {
            mActivityDelegate.getDrawerLayout().openDrawer(DRAWER_GRAVITY_CHAT_BOXES);
        }

        if (mCurrentChatBoxManager.getCurrentChatBox() != null) {
            mCurrentChatBoxManager.loadOnlineUsers();
        }
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(mActivityDelegate.getActivity())
                .unregisterReceiver(mMessageReceiver);
        super.onPause();
        mCurrentChatBoxManager.onPause();
        ChatBox chatBox = mCurrentChatBoxManager.getCurrentChatBox();
        if (chatBox != null)
            mCommunicationActivityManager.setInt(R.string.current_chatbox_id,
                    chatBox.getId());

        if (mRefreshOnlineUsersHandler != null) {
            mRefreshOnlineUsersHandler.removeMessages(MSG_GET_ONLINE_USERS);
        }
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
        String action = intent.getAction();
        LogUtils.v("GCM onNewIntent action" + action);

        if (CommunicationActivity.ACTION_OPEN_CHATBOX.equals(action)) {
            mRequestedChatboxId = intent.getIntExtra(CommunicationActivity.CHATBOX_ID, 0);
            LogUtils.v("GCM onNewIntent action mRequestedChatboxId " + mRequestedChatboxId);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!mIsActive) {
            return;
        }
        switch (requestCode) {
            case Delegate.REQUEST_SEARCH_CHAT_BOX:
                if (resultCode == Activity.RESULT_OK) {
                    LightWeightChatBox searchResult = (LightWeightChatBox) data
                            .getSerializableExtra(SearchChatBoxActivity.EXTRA_RESULT_CHAT_BOX);
                    onChatBoxResult(searchResult);
                }
                break;
            case Delegate.REQUEST_CREATE_CHAT_BOX:
                if (resultCode == Activity.RESULT_OK) {
                    LightWeightChatBox newChatBox = (LightWeightChatBox) data
                            .getSerializableExtra(CreateChatBoxActivity.EXTRA_RESULT_CHAT_BOX);
                    onChatBoxResult(newChatBox);
                } else if (resultCode == BaseABFragmentActivity.RESULT_EXCEPTION) {
                    Exception exception = (Exception) data
                            .getSerializableExtra(CreateChatBoxActivity.EXTRA_RESULT_EXCEPTION);
                    mActivityDelegate.handle(exception, R.string.error_while_creating_chatbox);
                }
                break;
            case AccountDialogFragment.REQUEST_ADD_NEW_AUTHENTICATION:
                if (resultCode == Activity.RESULT_OK) {
                    mActivityDelegate.dismissAuthenticationDialog();
                    User user = (User) data.getSerializableExtra(AuthenticateActivity.INTENT_USER);
                    mActivityDelegate.onAccountSwitch(new AccountSwitchEvent(user));
                    Toast.makeText(mActivityDelegate.getActivity(),
                            getString(R.string.message_account_added),
                            Toast.LENGTH_LONG).show();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        AppCompatActivity activity = mActivityDelegate.getActivity();
        final DrawerLayout drawerLayout = mActivityDelegate.getDrawerLayout();
        activity.getMenuInflater().inflate(R.menu.chatbox_menu, menu);
        MenuItem onlineUsersItem = menu.findItem(R.id.online_users);
        mediaAddItem = menu.findItem(R.id.audio_add);

        /**
         * Create badge view for online user item
         */
        ImageButton iconView = new ImageButton(activity, null,
                R.style.Widget_AppCompat_ActionButton);
        iconView.setImageDrawable(onlineUsersItem.getIcon());
        iconView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawerLayout.isDrawerOpen(DRAWER_GRAVITY_ONLINE_USER)) {
                    drawerLayout.closeDrawer(DRAWER_GRAVITY_ONLINE_USER);
                } else {
                    drawerLayout.openDrawer(DRAWER_GRAVITY_ONLINE_USER);
                }
            }
        });

        // The badge view requires target view (iconView in this case)
        // to have a ViewGroup parent
        LinearLayout container = new LinearLayout(activity);
        container.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        container.addView(iconView);

        Resources res = activity.getResources();
        mOnlineUsersBadgeView = new BadgeView(activity, iconView);
        mOnlineUsersBadgeView.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
        mOnlineUsersBadgeView.setTextSize(TypedValue.COMPLEX_UNIT_SP,
                res.getDimension(R.dimen.badge_view_text_size));
        mOnlineUsersBadgeView.setBadgeMargin(res.getDimensionPixelSize(
                R.dimen.default_margin));
        onlineUsersItem.setActionView(container);
        mOnlineUsersBadgeView.setBadgeBackgroundColor(
                mActivityDelegate.getActivity().getResources().getColor(R.color.accent));

        return true;
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
        return mActivityDelegate.getDrawerLayout().isDrawerOpen(DRAWER_GRAVITY_ONLINE_USER);
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
        mActivityDelegate.getActivity().getContentResolver().delete(
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
    //        Event Bus
    ///////////////////////////////////////////////////
    @Subscribe
    public void onUpdateSubscriptionEvent(UpdateSubscriptionEvent event) {
        if (event.getStatus() == UpdateSubscriptionEvent.Status.FAILED) {
            if (event.getException() != null &&
                    event.getException() instanceof ApiManager.NotVerifiedEmailException) {
                Toast.makeText(mActivityDelegate.getActivity(),
                        getString(R.string.error_email_verify),
                        Toast.LENGTH_LONG).show();
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
                if (mRefreshOnlineUsersHandler != null) {
                    mRefreshOnlineUsersHandler.removeMessages(MSG_GET_ONLINE_USERS);
                }
                mNumOfOnlineUser = 0;
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
                ChatBox chatbox = event.getChatbox();
                setTitle(chatbox.getName());
                if (chatbox.getAlias() != null) setSubTitle(constructAliasLink(chatbox.getAlias()));
                mCurrentChatBoxManager.loadOnlineUsers();

                invalidateOptionsMenu();
                subscribeToCurrentChatBox();
                markNotificationRead(chatbox.getId());
                updateChatBoxUnreadCountInDB(chatbox.getId(), 0);
                //When open conversation, this means user read all unread messages
                AckChatboxIntentService.ack(mActivityDelegate.getActivity(), chatbox.getId());
                cancelNotification();
                break;
            case UPDATED:
                invalidateOptionsMenu();
                break;
        }
    }

    @Subscribe
    public void onUserSelectedSongEvent(UserSelectedSongEvent event) {
        mActivityDelegate.getDrawerLayout().closeDrawers();
    }

    @Subscribe
    public void onLoadOnlineUsersSuccess(LoadOnlineUsersSuccessEvent event) {
        // Reload options menu to update number of onlines users
        mNumOfOnlineUser = event.getCount();
        invalidateOptionsMenu();
        // Recursively get online users.
        // Also, check and only post new message to the handler if
        // its queue is empty. That ensures that the update interval
        // is correct.
        if (mRefreshOnlineUsersHandler == null) {
            mRefreshOnlineUsersHandler = new Handler() {
                @Override
                public void handleMessage(android.os.Message msg) {
                    mCurrentChatBoxManager.loadOnlineUsers();
                }
            };
        }
        if (!mRefreshOnlineUsersHandler.hasMessages(MSG_GET_ONLINE_USERS)) {
            android.os.Message msg = android.os.Message.obtain(
                    mRefreshOnlineUsersHandler, MSG_GET_ONLINE_USERS);
            mRefreshOnlineUsersHandler.sendMessageDelayed(msg,
                    REFRESH_ONLINE_USERS_INTERVAL);
        }
    }

    @Subscribe
    public void onCreateBookmarkEvent(CreateBookmarkEvent event) {
        if (handleCreateBookmarkException(event)) {
            return;
        }
        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
        SyncedBookmark syncedBookmark = event.getResponse().getData();
        ChatBox chatBox = syncedBookmark.getChatBox();
        //Update local chatbox
        batch.add(ContentProviderOperation
                .newUpdate(ChatWingContentProvider.getChatBoxWithIdUri(chatBox.getId()))
                .withValues(ChatBoxTable.getContentValues(chatBox, null))
                .build());

        //Update local bookmarks
        int chatBoxId = chatBox.getId();
        syncedBookmark.setIsSynced(true);
        if (ChatWingContentProvider.hasSyncedBookmarkInDB(
                mActivityDelegate.getActivity().getContentResolver(),
                chatBoxId)) {
            //Update existing bookmark
            Uri syncedBookmarkWithChatBoxIdUri = ChatWingContentProvider
                    .getSyncedBookmarkWithChatBoxIdUri(chatBoxId);
            batch.add(ContentProviderOperation
                    .newUpdate(syncedBookmarkWithChatBoxIdUri)
                    .withValues(SyncedBookmarkTable.getContentValues(syncedBookmark))
                    .build());
        } else {
            //Somehow local bookmark is deleted, it should be add back
            Uri syncedBookmarksUri = ChatWingContentProvider.getSyncedBookmarksUri();
            batch.add(ContentProviderOperation
                    .newInsert(syncedBookmarksUri)
                    .withValues(SyncedBookmarkTable.getContentValues(syncedBookmark))
                    .build());
        }

        try {
            mActivityDelegate.getActivity()
                    .getContentResolver()
                    .applyBatch(ChatWingContentProvider.AUTHORITY, batch);
        } catch (RemoteException e) {
            mActivityDelegate.handle(e, R.string.error_failed_to_save_bookmark);
        } catch (OperationApplicationException e) {
            mActivityDelegate.handle(e, R.string.error_failed_to_save_bookmark);
        }
    }

    private void cancelNotification() {
        if (mCurrentChatBoxManager.getCurrentChatBox() == null) return;
        NotificationManager notificationManager =
                (NotificationManager) mActivityDelegate.getActivity()
                        .getSystemService(Context.NOTIFICATION_SERVICE);
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

    public void closeSecondaryDrawer() {
        mActivityDelegate.getDrawerLayout().closeDrawer(DRAWER_GRAVITY_ONLINE_USER);
        mActivityDelegate.getActivity().invalidateOptionsMenu();
    }

    private void subscribeToCurrentChatBox() {
        FayeReceiver fayeReceiver = mActivityDelegate.getFayeReceiver();
        ChatBox currentChatBox = mCurrentChatBoxManager.getCurrentChatBox();
        if (currentChatBox != null) {
            fayeReceiver.subscribeToChannel(String.format("/%s",
                    currentChatBox.getFayeChannel()));
        }
    }

    private void markNotificationRead(int chatBoxId) {
        Uri uri = ChatWingContentProvider.getNotificationMessagesUri();

        ContentResolver contentResolver = mActivityDelegate.getActivity().getContentResolver();
        contentResolver.delete(uri, NotificationMessagesTable.CHAT_BOX_ID + "==" + chatBoxId, null);
    }

    private String constructAliasLink(String alias) {
        return Constants.CHATWING_BASE_URL + "/" + alias;
    }

    private void setMediaControlVisible(boolean visible) {
        mediaAddItem.setVisible(visible);
    }

    private void manageBlackList() {
        AppCompatActivity activity = mActivityDelegate.getActivity();
        Intent i = new Intent(activity, NoMenuWebViewActivity.class);
        i.putExtra(NoMenuWebViewActivity.EXTRA_URL, String.format(ApiManager.MANAGE_BLACKLIST_URL,
                mCurrentChatBoxManager.getCurrentChatBox().getKey(),
                mUserManager.getCurrentUser().getAccessToken()));
        activity.startActivity(i);
    }

    private void bookmarkCurrentChatBox() {
        ChatBox chatBox = mCurrentChatBoxManager.getCurrentChatBox();
        if (chatBox == null) {
            return;
        }
        if (mUserManager.getCurrentUser() == null) {
            Toast.makeText(mActivityDelegate.getActivity(),
                    mActivityDelegate.getActivity().getString(R.string.error_failed_to_save_bookmark),
                    Toast.LENGTH_LONG)
                    .show();
            return;
        }
        CreateBookmarkIntentService.start(mActivityDelegate.getActivity(),
                LightWeightChatBox.copyFromChatbox(chatBox));
    }

    private void onChatBoxResult(LightWeightChatBox result) {
        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
        // Add chat box to DB before setting it as our current chat box,
        // so that we get notify when this chat box is changed (#131).
        // FIX ME: right now category is required for a chat box.
        // And that requirement is incorrect. So this will be fixed after
        // the DB Scheme is changed, probably (#142.)
        ChatBox chatBox = new ChatBox(result.getId(),
                result.getKey(),
                result.getName(),
                result.getFayeChannel(),
                result.getAlias());


        ContentValues chatBoxContentValues = ChatBoxTable.getContentValues(
                chatBox, " ");
        batch.add(ContentProviderOperation
                .newInsert(ChatWingContentProvider.getChatBoxesUri())
                .withValues(chatBoxContentValues)
                .build());

        try {
            mActivityDelegate.getActivity()
                    .getContentResolver()
                    .applyBatch(ChatWingContentProvider.AUTHORITY, batch);
            mActivityDelegate.getDrawerLayout().closeDrawers();
            // Don't directly set chat box using mCurrentChatBoxManager here
            // because components are not ready and won't receive posted events.
            mRequestedChatboxId = result.getId();
        } catch (RemoteException e) {
            mActivityDelegate.handle(e, R.string.error_failed_to_save_chat_box);
        } catch (OperationApplicationException e) {
            mActivityDelegate.handle(e, R.string.error_failed_to_save_chat_box);
        }
        // Add bookmark to SyncedBookmarkTable
        CreateBookmarkIntentService.start(mActivityDelegate.getActivity(), result);
        mActivityDelegate.getDrawerLayout().closeDrawers();
        // Don't directly set chat box using mCurrentChatBoxManager here
        // because components are not ready and won't receive posted events.
        mRequestedChatboxId = result.getId();
    }

    @TargetApi(11)
    private void copyAliasCurrentChatBox() {
        ChatBox chatBox = mCurrentChatBoxManager.getCurrentChatBox();
        if (chatBox == null || chatBox.getAlias() == null) {
            return;
        }
        AppCompatActivity activity = mActivityDelegate.getActivity();

        if (Build.VERSION.SDK_INT >= 11) {
            ClipboardManager clipboard = (ClipboardManager)
                    activity.getSystemService(activity.CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(ClipData.newPlainText("alias_copied",
                    constructAliasLink(chatBox.getAlias())));
        } else {
            android.text.ClipboardManager oldClipboard = (android.text.ClipboardManager)
                    activity.getSystemService(activity.CLIPBOARD_SERVICE);
            oldClipboard.setText(constructAliasLink(chatBox.getAlias()));
        }

        Toast.makeText(activity, R.string.message_current_chat_box_alias_copied,
                Toast.LENGTH_SHORT)
                .show();
    }

    private boolean handleCreateBookmarkException(CreateBookmarkEvent event) {
        Exception exception = event.getException();
        if (exception == null) {
            return false;
        }
        if (exception instanceof OperationApplicationException
                || exception instanceof RemoteException) {
            mActivityDelegate.handle(exception, R.string.error_failed_to_save_chat_box);
            return true;
        }
        if (exception instanceof ApiManager.NotVerifiedEmailException) {
            mActivityDelegate.handle(exception, R.string.error_email_verify);
            return true;
        }
        mActivityDelegate.handle(exception, R.string.error_failed_to_save_bookmark);
        return true;
    }

    private void updateControlUI(ChatBox chatbox) {
        if (!mBuildManager.isSupportedMusicBox()) {
            return;
        }
        String audioUrl = chatbox.getAudioUrl();
        boolean isBindMediaService = mMediaControlInterface.isBindMediaService();
        MusicService.STATUS status = mMediaControlInterface.getMediaStatus();

        LogUtils.v("Audio URL " + audioUrl +
                " isBindMediaService=" + isBindMediaService + " status " + status);

        if (audioUrl == null
                || (isBindMediaService && status == MusicService.STATUS.PREPARING)
                || isBindMediaService && mMediaControlInterface.getMediaService()
                .containsSong(new Song(
                        chatbox.getAudioUrl(),
                        chatbox.getAudioName(),
                        chatbox.getName()))) {
            setMediaControlVisible(false);
        } else {
            setMediaControlVisible(true);
        }

        if (status == MusicService.STATUS.PREPARING) {
            mMediaControlInterface.updateUIForPlayerPreparing(true);
        } else {
            mMediaControlInterface.updateUIForPlayerPreparing(false);
        }
    }
}
