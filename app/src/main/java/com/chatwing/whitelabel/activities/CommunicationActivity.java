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

package com.chatwing.whitelabel.activities;


import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.SoundPool;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.chatwing.whitelabel.ChatWing;
import com.chatwing.whitelabel.Constants;
import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.contentproviders.ChatWingContentProvider;
import com.chatwing.whitelabel.events.AccountSwitchEvent;
import com.chatwing.whitelabel.events.AllSyncsCompletedEvent;
import com.chatwing.whitelabel.events.BlockedEvent;
import com.chatwing.whitelabel.events.ChatServiceEvent;
import com.chatwing.whitelabel.events.DeleteBookmarkEvent;
import com.chatwing.whitelabel.events.NetworkStatusEvent;
import com.chatwing.whitelabel.events.SyncCommunicationBoxEvent;
import com.chatwing.whitelabel.events.SyncUnreadEvent;
import com.chatwing.whitelabel.events.UpdateUserEvent;
import com.chatwing.whitelabel.events.UserSelectedDefaultUsersEvent;
import com.chatwing.whitelabel.events.UserUnauthenticatedEvent;
import com.chatwing.whitelabel.events.faye.ChannelSubscriptionChangedEvent;
import com.chatwing.whitelabel.events.faye.FayeFailedEvent;
import com.chatwing.whitelabel.events.faye.FayePublishEvent;
import com.chatwing.whitelabel.events.faye.MessageReceivedEvent;
import com.chatwing.whitelabel.events.faye.ServerConnectionChangedEvent;
import com.chatwing.whitelabel.fragments.AccountDialogFragment;
import com.chatwing.whitelabel.fragments.AdminListFragment;
import com.chatwing.whitelabel.fragments.BlockUserDialogFragment;
import com.chatwing.whitelabel.fragments.BookmarkedChatBoxesDrawerFragment;
import com.chatwing.whitelabel.fragments.CategoriesFragment;
import com.chatwing.whitelabel.fragments.ChatMessagesFragment;
import com.chatwing.whitelabel.fragments.ColorPickerDialogFragment;
import com.chatwing.whitelabel.fragments.CommunicationDrawerFragment;
import com.chatwing.whitelabel.fragments.CommunicationMessagesFragment;
import com.chatwing.whitelabel.fragments.ConversationMessagesFragment;
import com.chatwing.whitelabel.fragments.ConversationsFragment;
import com.chatwing.whitelabel.fragments.FeedDrawerFragment;
import com.chatwing.whitelabel.fragments.FeedFragment;
import com.chatwing.whitelabel.fragments.GooglePlusDialogFragment;
import com.chatwing.whitelabel.fragments.InjectableFragmentDelegate;
import com.chatwing.whitelabel.fragments.MusicDrawerFragment;
import com.chatwing.whitelabel.fragments.MusicFragment;
import com.chatwing.whitelabel.fragments.NavigatableFragmentListener;
import com.chatwing.whitelabel.fragments.NewContentFragment;
import com.chatwing.whitelabel.fragments.OnlineUsersFragment;
import com.chatwing.whitelabel.fragments.PasswordDialogFragment;
import com.chatwing.whitelabel.fragments.PhotoPickerDialogFragment;
import com.chatwing.whitelabel.fragments.ProfileFragment;
import com.chatwing.whitelabel.interfaces.MediaControlInterface;
import com.chatwing.whitelabel.managers.ApiManager;
import com.chatwing.whitelabel.managers.BuildManager;
import com.chatwing.whitelabel.managers.ChatboxModeManager;
import com.chatwing.whitelabel.managers.ChatboxUnreadDownloadManager;
import com.chatwing.whitelabel.managers.CommunicationActivityManager;
import com.chatwing.whitelabel.managers.CommunicationModeManager;
import com.chatwing.whitelabel.managers.ConversationModeManager;
import com.chatwing.whitelabel.managers.CurrentChatBoxManager;
import com.chatwing.whitelabel.managers.CurrentConversationManager;
import com.chatwing.whitelabel.managers.FeedModeManager;
import com.chatwing.whitelabel.managers.GcmManager;
import com.chatwing.whitelabel.managers.MusicModeManager;
import com.chatwing.whitelabel.managers.SyncManager;
import com.chatwing.whitelabel.managers.UserManager;
import com.chatwing.whitelabel.modules.CommunicationActivityModule;
import com.chatwing.whitelabel.parsers.BBCodePair;
import com.chatwing.whitelabel.parsers.BBCodeParser;
import com.chatwing.whitelabel.parsers.EventParser;
import com.chatwing.whitelabel.pojos.ChatBox;
import com.chatwing.whitelabel.pojos.Conversation;
import com.chatwing.whitelabel.pojos.Event;
import com.chatwing.whitelabel.pojos.Message;
import com.chatwing.whitelabel.pojos.Song;
import com.chatwing.whitelabel.pojos.User;
import com.chatwing.whitelabel.pojos.errors.ChatWingError;
import com.chatwing.whitelabel.pojos.params.CreateConversationParams;
import com.chatwing.whitelabel.pojos.responses.ChatBoxDetailsResponse;
import com.chatwing.whitelabel.pojos.responses.DeleteBookmarkResponse;
import com.chatwing.whitelabel.services.AckChatboxIntentService;
import com.chatwing.whitelabel.services.AckConversationIntentService;
import com.chatwing.whitelabel.services.ChatWingChatService;
import com.chatwing.whitelabel.services.CreateConversationIntentService;
import com.chatwing.whitelabel.services.DownloadUserDetailIntentService;
import com.chatwing.whitelabel.services.MusicService;
import com.chatwing.whitelabel.services.OfflineIntentService;
import com.chatwing.whitelabel.services.SyncBookmarkIntentService;
import com.chatwing.whitelabel.services.SyncCommunicationBoxesIntentService;
import com.chatwing.whitelabel.services.UpdateAvatarIntentService;
import com.chatwing.whitelabel.services.UpdateGcmIntentService;
import com.chatwing.whitelabel.tables.MessageTable;
import com.chatwing.whitelabel.utils.LogUtils;
import com.chatwing.whitelabel.views.BBCodeEditText;
import com.flurry.android.FlurryAgent;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.gson.Gson;
import com.soundcloud.android.crop.Crop;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.json.JSONException;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

/**
 * This activity is the base where {@link com.chatwing.whitelabel.fragments.CommunicationMessagesFragment}
 * and drawers are attached. Depend on which mode the activity is in,
 * appropriate {@link com.chatwing.whitelabel.managers.CommunicationModeManager} is activated
 * <p/>
 * <p>
 * Due to otto limitation, subclasses must override
 * </p>
 *
 * @author cuongthai
 */
public class CommunicationActivity
        extends BaseABFragmentActivity
        implements NewContentFragment.Listener,
        ColorPickerDialogFragment.Listener,
        CommunicationMessagesFragment.Delegate,
        CommunicationModeManager.Delegate,
        InjectableFragmentDelegate,
        CommunicationDrawerFragment.Listener,
        OnlineUsersFragment.OnlineUsersFragmentDelegate,
        NavigatableFragmentListener,
        ProfileFragment.Listener,
        MediaControlInterface {
    public static final String AVATAR_PICKER_DIALOG_FRAGMENT_TAG = "AvatarPickerDialogFragment";
    public static final String BLOCK_USER_DIALOG_FRAGMENT_TAG = "BlockUserDialogFragment";
    public static final String ACCOUNT_DIALOG_FRAGMENT_TAG = "AccountDialogFragmentTag";
    public static final String ACTION_STOP_MEDIA = "ACTION_STOP_MEDIA";
    public static final String PASSWORD_DIALOG_FRAGMENT_TAG = "PasswordDialogFragmentTAG";
    public static final String CHATBOX_ID = "CHATBOX_ID";
    public static final String ACTION_OPEN_CHATBOX = "ACTION_OPEN_CHATBOX";
    public static final String ACTION_OPEN_CONVERSATION = "ACTION_OPEN_CONVERSATION";
    public static final String CONVERSATION_ID = "CONVERSATION_ID";

    private static final String EXTRA_CURRENT_MODE = "current_mode";
    private static final String ATTACHMENT_CONTENT_FRAGMENT_TAG = "NewContentFragment";
    private static final int MODE_CHAT_BOX = 0;
    private static final int MODE_NONE = -1;
    private static final int MODE_CONVERSATION = 1;
    public static final int REQUEST_CODE_AUTHENTICATION = 10000;
    private static final int REQUEST_CODE_GET_GOOGLE_PLAY_SERVICES = 9000;


    @Inject
    protected GcmManager mGcmManager;
    @Inject
    protected ChatboxModeManager mChatboxModeManager;
    @Inject
    protected BuildManager mBuildManager;
    @Inject
    protected ConversationModeManager mConversationModeManager;
    @Inject
    protected Bus mBus;
    @Inject
    protected EventParser mEventParser;
    @Inject
    protected SyncManager mSyncManager;
    @Inject
    protected CurrentConversationManager mCurrentConversationManager;
    @Inject
    protected CurrentChatBoxManager mCurrentChatboxManager;
    @Inject
    protected CommunicationActivityManager mCommunicationActivityManager;
    @Inject
    protected ApiManager mApiManager;
    @Inject
    protected UserManager mUserManager;
    @Inject
    protected ChatboxUnreadDownloadManager chatboxUnreadDownloadManager;
    @Inject
    protected FeedModeManager mFeedModeManager;
    @Inject
    protected MusicModeManager mMusicModeManager;
    @Inject
    protected SoundPool mSoundEffectsPool;


    protected CommunicationModeManager mCurrentCommunicationMode;

    private View mContentView;
    private View mProgressView;
    private ProgressBar mProgressBar;
    private TextView mProgressText;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    // Flag to determined onCreate is called. It is used onStart to decide
    // whether a sync operation should be triggered or not and reset right
    // after that.
    private boolean mIsCreated;
    private ProgressBar mLoadingView;
    private MusicService musicService;
    private Intent playIntent;
    private boolean musicBound = false;
    private int mNewMessageSoundId;
    private Snackbar snackbar;

    private View.OnClickListener snackbarRetryAction = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ensureFayeIsConnected();
        }
    };


    /**
     * This class makes the ad request and loads the ad.
     */
    public static class AdFragment extends Fragment {

        private AdView mAdView;

        public AdFragment() {
        }

        @Override
        public void onActivityCreated(Bundle bundle) {
            super.onActivityCreated(bundle);

            // Gets the ad view defined in layout/ad_fragment.xml with ad unit ID set in
            // values/strings.xml.
            mAdView = (AdView) getView().findViewById(R.id.adView);

            // Create an ad request. Check logcat output for the hashed device ID to
            // get test ads on a physical device. e.g.
            // "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .addTestDevice("8852195CD9EACCCF36E4DEBF3288370B")
                    .build();

            // Start loading the ad in the background.
            mAdView.loadAd(adRequest);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_ad, container, false);
        }

        /**
         * Called when leaving the activity
         */
        @Override
        public void onPause() {
            if (mAdView != null) {
                mAdView.pause();
            }
            super.onPause();
        }

        /**
         * Called when returning to the activity
         */
        @Override
        public void onResume() {
            super.onResume();
            if (mAdView != null) {
                mAdView.resume();
            }
        }

        /**
         * Called before the activity is destroyed
         */
        @Override
        public void onDestroy() {
            if (mAdView != null) {
                mAdView.destroy();
            }
            super.onDestroy();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ChatWing.instance(getApplicationContext()).getChatwingGraph().plus();

        setContentView(R.layout.activity_communication);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);

        mBus.register(this);

        mContentView = findViewById(R.id.fragment_container);
        mProgressView = findViewById(R.id.progress_container);
        mProgressBar = (ProgressBar) mProgressView.findViewById(R.id.loading_view);
        mProgressText = (TextView) mProgressView.findViewById(R.id.progress_text);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mLoadingView = (ProgressBar) findViewById(R.id.progress_bar);

        mNewMessageSoundId = getSoundNewMessageId();

        mChatboxModeManager.onCreate(savedInstanceState);
        mConversationModeManager.onCreate(savedInstanceState);

        stopRefreshAnimation();

        //This mode is priority due to user action requesting open
        int actionMode = getActionMode(getIntent());
        LogUtils.v("Intent to use actionMode mode " + actionMode);

        int pauseSavedMode = mCommunicationActivityManager.getInt(R.string.current_mode_state, 0);
        int currentMode = MODE_CHAT_BOX;  //Default mode is chatbox

        if (pauseSavedMode != 0) {
            currentMode = pauseSavedMode;
        }

        if (savedInstanceState != null
                && savedInstanceState.containsKey(EXTRA_CURRENT_MODE)) {
            currentMode = savedInstanceState.getInt(EXTRA_CURRENT_MODE);
        }

        //Override current mode by priority mode
        if (actionMode != MODE_NONE) {
            currentMode = actionMode;
        }


        if (currentMode == MODE_CHAT_BOX) {
            setupChatboxMode();
        } else {
            setupConversationMode();
        }

        mIsCreated = true;

        String action = getIntent().getAction();
        if (ACTION_STOP_MEDIA.equals(action)) {
            startService(new Intent(MusicService.ACTION_STOP));
        }

        if (!mBuildManager.isOfficialChatWingApp() && userManager.getCurrentUser() == null) {
            startActivity(new Intent(this, WhiteLabelCoverActivity.class));
            finish();
            return;
        }

        String onlineFragmentTag = getString(R.string.fragment_tag_online_user);
        if (getSupportFragmentManager().findFragmentByTag(onlineFragmentTag) == null) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.right_drawer_container, new OnlineUsersFragment(), onlineFragmentTag);
            fragmentTransaction.commit();
        }

        String adsFragmentTag = getString(R.string.fragment_tag_ads);
        if (mBuildManager.isSupportedAds()
                && getSupportFragmentManager().findFragmentByTag(adsFragmentTag) == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.ads_container, new AdFragment(), adsFragmentTag)
                    .commit();
        }

        //We start our lovely ChatService so that it listen to faye server
        startService(new Intent(this, ChatWingChatService.class));
    }

    private void setupSnackbar(View contentView) {
        snackbar = Snackbar
                .make(contentView, "", Snackbar.LENGTH_SHORT);

        styleInfoSnackbar();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        int actionMode = getActionMode(intent);
        LogUtils.v("Action Mode "+actionMode);
        if (actionMode == MODE_CHAT_BOX) {
            setupChatboxMode();
        } else if (actionMode == MODE_CONVERSATION){
            setupConversationMode();
        }

        if (mCurrentCommunicationMode != null) {
            mCurrentCommunicationMode.onNewIntent(intent);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        deployGCM();

        // Determine whether a sync categories operation should be
        // performed right now or not.
        // It should start if the activity is newly created (onCreate is called).
        // Thus, we don't refresh when the activity has been in background for a
        // short period of time.
        if (mIsCreated) {
            startSyncingCommunications(false);
            mIsCreated = false;
        }

        startAndBindMusicService();

        FlurryAgent.onStartSession(this, getString(R.string.flurry_api_key));
    }

    @Override
    protected void onStop() {
        super.onStop();
        FlurryAgent.onEndSession(this);
        if (isBindMediaService()) {
            unbindService(musicConnection);
        }
    }

    @Override
    public void onBackPressed() {
        if (mCurrentCommunicationMode.isSecondaryDrawerOpening()) {
            ((ChatboxModeManager) mCurrentCommunicationMode).closeSecondaryDrawer();
        } else if (!mCurrentCommunicationMode.isCommunicationBoxDrawerOpening()) {
            // Both online users and chat boxes/conversation lists are closed.
            // Open chat boxes/conversation list now.
            mCurrentCommunicationMode.openCommunicationBoxDrawer();
        } else {
            // Online users list is closed, chat boxes list is opened.
            // User probably is trying to quit the app.
            FragmentManager fragmentManager = getSupportFragmentManager();
            int stackSize = fragmentManager.getBackStackEntryCount();
            if (stackSize == 0) {
                finish();
            } else {
                String fragmentTag = fragmentManager.getBackStackEntryAt(stackSize - 1).getName();
                fragmentManager.popBackStack(fragmentTag, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        }
    }

    @Override
    public void onContextMenuClosed(Menu menu) {
        super.onContextMenuClosed(menu);
        String fragmentTag = getString(R.string.tag_communication_messages);
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment currentFragment = fragmentManager.findFragmentByTag(fragmentTag);
        if (currentFragment instanceof CommunicationMessagesFragment) {
            ((CommunicationMessagesFragment) currentFragment).onContextMenuClosed(menu);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mChatboxModeManager.onRestoreInstanceState(savedInstanceState);
        mConversationModeManager.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("WORKAROUND_FOR_BUG_19917_KEY", "WORKAROUND_FOR_BUG_19917_VALUE");
        super.onSaveInstanceState(outState);
        LogUtils.v("onSaveInstanceState save mode");
        mChatboxModeManager.onSaveInstanceState(outState);
        mConversationModeManager.onSaveInstanceState(outState);
        if (isInChatBoxMode()) {
            outState.putInt(EXTRA_CURRENT_MODE, MODE_CHAT_BOX);
        } else if (isInConversationMode()) {
            outState.putInt(EXTRA_CURRENT_MODE, MODE_CONVERSATION);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        LogUtils.v("onActivityResult " + requestCode + ":" + resultCode);
        mCurrentCommunicationMode.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == REQUEST_CODE_AUTHENTICATION && resultCode == RESULT_OK) {
            startSyncingCommunications(true);
        }

        if (requestCode == Crop.REQUEST_CROP && resultCode == RESULT_OK) {
            Uri output = Crop.getOutput(intent);
            startUpdateAvatar(output.getPath());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtils.v("On Destroy Activity");
        //Faye connection will stop receiving msg when the is destroyed
        //Either by user(backpress) or system destroy
        mBus.unregister(this);

        mCurrentCommunicationMode.onDestroy();
        mCurrentCommunicationMode = null;
    }

    @Override
    protected void onResume() {
        super.onResume();

        ensureFayeIsConnected();
        syncRefreshAnimationState();

        mChatboxModeManager.onResume();
        mConversationModeManager.onResume();
        invalidateOptionsMenu();
        chatboxUnreadDownloadManager.downloadUnread();
    }

    @Override
    protected void onPause() {
        super.onPause();

        mChatboxModeManager.onPause();
        mConversationModeManager.onPause();

        if (isInChatBoxMode()) {
            mCommunicationActivityManager.setInt(R.string.current_mode_state, MODE_CHAT_BOX);
        } else if (isInConversationMode()) {
            mCommunicationActivityManager.setInt(R.string.current_mode_state, MODE_CONVERSATION);
        }


        //Notify server that user does not attention to the app
        //Go to background, sleep, etc... and start receiving push notification
        //Although msg is still coming in the background to prevent reload in onResume
        startService(new Intent(this, OfflineIntentService.class));
    }


    @Override
    protected void onPostResume() {
        super.onPostResume();
        mCurrentCommunicationMode.onPostResume();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mCurrentCommunicationMode == null) return false;

        return mCurrentCommunicationMode.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return mCurrentCommunicationMode.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            if (getDrawerLayout().isDrawerOpen(Gravity.LEFT)) {
                getDrawerLayout().closeDrawer(Gravity.LEFT);
            } else {
                getDrawerLayout().openDrawer(Gravity.LEFT);
            }
            return true;
        }
        if (i == R.id.refresh) {
            startSyncingCommunications(true);
            return true;
        } else {
            return mCurrentCommunicationMode.onOptionsItemSelected(item);
        }
    }

    @Override
    protected List<Object> getModules() {
        return Arrays.<Object>asList(new CommunicationActivityModule(this));
    }

    @Override
    public final void onItemClicked(NewContentFragment.Item item) {
        switch (item) {
            case BBCODES:
                getCommunicationMessagesFragment().showBBCodeControls();
                break;
        }
    }

    //////////////////////////////////////////////////////////////////
    //          CommunicationMessagesFragment.Delegate
    /////////////////////////////////////////////////////////////////

    @Override
    public final void showColorPickerDialogFragment(BBCodeParser.BBCode code) {
        if (code == null) {
            throw new IllegalArgumentException("Code is required.");
        }
        ColorPickerDialogFragment fragment
                = ColorPickerDialogFragment.newInstance(code);
        fragment.show(getSupportFragmentManager(), code.toString());
    }

    @Override
    public final void showNewContentFragment() {
        boolean hasBBCodes = !getCommunicationMessagesFragment().isShowingBBControls();
        NewContentFragment fragment = NewContentFragment.newInstance(hasBBCodes);
        fragment.show(getSupportFragmentManager(), ATTACHMENT_CONTENT_FRAGMENT_TAG);
    }

    @Override
    public void showPasswordDialogFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        PasswordDialogFragment oldFragment = (PasswordDialogFragment)
                fragmentManager.findFragmentByTag(PASSWORD_DIALOG_FRAGMENT_TAG);
        if (oldFragment == null || oldFragment.isDismissingByUser()) {
            PasswordDialogFragment newFragment = new PasswordDialogFragment();
            newFragment.show(getSupportFragmentManager(), PASSWORD_DIALOG_FRAGMENT_TAG);
        }
    }

    @Override
    public final void inject(BBCodeEditText mCommunicationBoxEditText) {
        super.inject(mCommunicationBoxEditText);
    }

    @Override
    public void showConversation(CreateConversationParams.SimpleUser simpleUser) {
        initConversationMenu();
        if (!isInConversationMode()) {
            setupConversationMode();
        }
        addToLeftDrawer(new ConversationsFragment(), false);

        Intent createConversation = new Intent(getActivity(), CreateConversationIntentService.class);
        createConversation.putExtra(CreateConversationIntentService.EXTRA_USER, simpleUser);
        startService(createConversation);
    }

    @Override
    public void showBlockUserDialogFragment(Message message) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        BlockUserDialogFragment oldFragment = (BlockUserDialogFragment)
                fragmentManager.findFragmentByTag(BLOCK_USER_DIALOG_FRAGMENT_TAG);
        if (oldFragment == null || oldFragment.isDismissingByUser()) {
            BlockUserDialogFragment newFragment = BlockUserDialogFragment.newInstance(message);
            newFragment.show(getSupportFragmentManager(), BLOCK_USER_DIALOG_FRAGMENT_TAG);
        }
    }


    @Override
    public final void onConfirmColor(Serializable code, int color) {
        BBCodePair pair = new BBCodePair((BBCodeParser.BBCode) code, color);
        getCommunicationMessagesFragment().appendBBCode(pair);
    }

    @Override
    public final void inject(Fragment fragment) {
        super.inject(fragment);
    }

    ////////////////////////////////////////////////////
    //          CommunicationModeManager.Delegate
    ///////////////////////////////////////////////////

    @Override
    public void handle(Exception exception, int generalErrorMessageResId) {
        if (exception instanceof ApiManager.InvalidIdentityException) {
            onInvalidAuthentication((ApiManager.InvalidIdentityException) exception);
        } else if (exception instanceof ApiManager.UserUnauthenticatedException) {
            onUserUnauthenticated();
        } else if (exception instanceof ApiManager.InvalidAccessTokenException) {
            onAccessTokenExpired();
        } else if (exception instanceof ApiManager.NotVerifiedEmailException) {
            mErrorMessageView.show(R.string.error_email_not_verified);
        } else if (exception instanceof ApiManager.OtherApplicationException) {
            mErrorMessageView.show(((ApiManager.OtherApplicationException) exception).getError().getMessage());
            logout();
        }
        if (exception instanceof ApiManager.RequiredPermissionException) {
            mErrorMessageView.show(R.string.error_no_permission);
        } else { // General Error
            if (generalErrorMessageResId != 0) {
                mErrorMessageView.show(exception, getString(generalErrorMessageResId));
            } else {
                mErrorMessageView.show(exception, getString(R.string.error_unknown));
            }
        }
    }

    @Override
    public void dismissAuthenticationDialog() {
        Fragment authenticationDialog = getSupportFragmentManager().findFragmentByTag(ACCOUNT_DIALOG_FRAGMENT_TAG);
        if (authenticationDialog != null)
            ((DialogFragment) authenticationDialog).dismiss();
    }

    @Subscribe
    @Override
    public void onAccountSwitch(AccountSwitchEvent accountSwitchEvent) {
        getDrawerLayout().closeDrawers();
        if (isInConversationMode()) {
            mCurrentConversationManager.removeCurrentConversation();
        }
        mBus.post(ChatServiceEvent.unsubscribeAllChannels());
        //clean up irrelevant data
        try {
            mGcmManager.clearRegistrationId();

            getContentResolver().applyBatch(ChatWingContentProvider.AUTHORITY,
                    ChatWingContentProvider.getClearAllDataBatch());
            startSyncingCommunications(true);

            deployGCM();
            invalidateOptionsMenu();
        } catch (Exception e) {
            LogUtils.e(e);
        }
    }

    @Subscribe
    public void onNetworkStatusEvent(NetworkStatusEvent event) {
        if (event.getStatus()== NetworkStatusEvent.Status.ON){
            chatboxUnreadDownloadManager.downloadUnread();
        }
    }

    @Override
    public final BaseABFragmentActivity getActivity() {
        return this;
    }

    @Override
    public final CommunicationMessagesFragment getCommunicationMessagesFragment() {
        String tag = getString(R.string.tag_communication_messages);
        return (CommunicationMessagesFragment) getSupportFragmentManager()
                .findFragmentByTag(tag);
    }

    @Override
    public final void setProgressText(int resId, boolean showProgressBar) {
        setContentShown(false);
        mProgressBar.setVisibility(showProgressBar ? View.VISIBLE : View.GONE);
        mProgressText.setText(resId);
    }

    @Override
    public final void setContentShown(boolean shown) {
        if (shown) {
            mContentView.setVisibility(View.VISIBLE);
            mProgressView.setVisibility(View.GONE);
        } else {
            mContentView.setVisibility(View.GONE);
            mProgressView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public final DrawerLayout getDrawerLayout() {
        return mDrawerLayout;
    }

    public void ensureFayeIsConnected() {
        if (snackbar != null) {
            snackbar.dismiss();
        }
        mBus.post(ChatServiceEvent.connect());
    }

    @Override
    public void showConversations() {
        if (mUserManager.userCanLoadConversations()) {
            setTitle(getActivity().getString(R.string.title_activity_conversation));
            invalidateOptionsMenu();
            if (isInChatBoxMode()) {
                setupConversationMode();
            }
            addToLeftDrawer(new ConversationsFragment());
        } else {
            mErrorMessageView.show(R.string.error_required_login_except_guest);
        }
    }


    /**
     * Calls this in subclass to use it as Conversation Mode
     */
    private void setupConversationMode() {
        setupMode(mConversationModeManager, ConversationMessagesFragment.newInstance());
    }

    /**
     * Calls this in subclass to use it as Public ChatBox Mode
     */
    private void setupChatboxMode() {
        setupMode(mChatboxModeManager, ChatMessagesFragment.newInstance());
    }

    /////////////////////////////////////////////////
    //     CommunicationDrawerFragment.Listener
    //////////////////////////////////////////////////

    @Override
    public void showCategories() {
        setTitle(getActivity().getString(R.string.title_chat_boxes));
        invalidateOptionsMenu();
        if (!isInChatBoxMode()) {
            setupChatboxMode();
        }
        addToLeftDrawer(getChatBoxesFragment());
    }

    @Override
    public void showAdminList() {
        if (mBuildManager.canShowAdminList()) {
            addToLeftDrawer(new AdminListFragment());
        } else {
            mErrorMessageView.show(R.string.error_empty_message);
        }
    }

    @Override
    public void showSettings() {
        Intent i = new Intent(this, MainPreferenceActivity.class);
        startActivity(i);
    }

    @Override
    public void updateAvatar() {
        getDrawerLayout().closeDrawers();
        showAvatarPicker();
    }

    @Override
    public void searchChatBox() {
        setTitle(getActivity().getString(R.string.title_chat_boxes));
        invalidateOptionsMenu();
        if (isInConversationMode()) {
            setupChatboxMode();
        }

        Intent i = new Intent(this, SearchChatBoxActivity.class);
        startActivityForResult(i, REQUEST_SEARCH_CHAT_BOX);
    }

    @Override
    public void createChatBox() {
        if (mUserManager.userCanCreateChatBox()) {
            setTitle(getActivity().getString(R.string.title_chat_boxes));
            invalidateOptionsMenu();
            if (isInConversationMode()) {
                setupChatboxMode();
            }

            Intent i = new Intent(this, CreateChatBoxActivity.class);
            startActivityForResult(i, REQUEST_CREATE_CHAT_BOX);
        } else {
            mErrorMessageView.show(R.string.error_required_chat_wing_login_to_create_chat_boxes);
        }
    }

    @Override
    public void showBookmarks() {
        if (mUserManager.userCanBookmark()) {
            setTitle(getActivity().getString(R.string.title_chat_boxes));
            invalidateOptionsMenu();
            if (isInConversationMode()) {
                setupChatboxMode();
            }
            addToLeftDrawer(new BookmarkedChatBoxesDrawerFragment());
        } else {
            mErrorMessageView.show(R.string.error_required_chat_wing_login);
        }
    }

    @Override
    public void openAccountPicker() {
        if (!mBuildManager.isOfficialChatWingApp()) return;
        getDrawerLayout().closeDrawers();
        showAccountPicker(null);
    }

    @Override
    public void showFeedsSources() {
        setTitle(getActivity().getString(R.string.title_feeds));
        invalidateOptionsMenu();
        if (!isInFeedMode()) {
            setupFeedMode();
        }
        addToLeftDrawer(new FeedDrawerFragment());
    }

    @Override
    public void showMusicBox() {
        setTitle(getActivity().getString(R.string.title_music_box));
        invalidateOptionsMenu();
        if (!isInMusicBoxMode()) {
            setupMusicBoxMode();
        }
        addToLeftDrawer(new MusicDrawerFragment());
    }

    @Override
    public void logout() {
        if (mUserManager.getCurrentUser() == null) {
            return;
        }

        new AsyncTask<Void, Void, Void>() {
            public ProgressDialog dialog;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                dialog = ProgressDialog.show(getDialogContext(), "",
                        getString(R.string.logging_out), true, false);
            }

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    String regId = mGcmManager.getRegistrationId();
                    mApiManager.updateGcm(mUserManager.getCurrentUser(), regId, ApiManager.GCM_ACTION_REMOVE);
                } catch (Exception e) {
                    LogUtils.e(e);
                }
                mGcmManager.clearRegistrationId();


                try {
                    getContentResolver().applyBatch(ChatWingContentProvider.AUTHORITY,
                            ChatWingContentProvider.getClearAllDataBatch());
                } catch (Exception e) {
                    LogUtils.e(e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                mCurrentCommunicationMode.logout();
                mUserManager.removeUsers();
                mBus.post(ChatServiceEvent.unsubscribeAllChannels());
                mCurrentCommunicationMode.deactivate();
                Intent i = new Intent(CommunicationActivity.this, getEntranceActivityClass());
                startActivity(i);
                finish();
            }
        }.execute();
    }

    @Override
    public void back(Fragment from) {
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(0, R.anim.slide_out_left)
                .remove(from)
                .commit();
        invalidateOptionsMenu();
    }

    @Override
    public boolean onSwipe() {
        return startSyncingCommunications(true);
    }


    //////////////////////////////////////////
    ///     Otto
    //////////////////////////////////////////
    @Subscribe
    public void onAllSyncsCompleted(AllSyncsCompletedEvent
                                            event) {
        ensureFayeIsConnected();


        if (event.needReload() &&
                mCurrentCommunicationMode != null) {
            mCurrentCommunicationMode.reloadCurrentBox();
        }

        chatboxUnreadDownloadManager.downloadUnread();
        syncRefreshAnimationState();
    }

    @Subscribe
    public void onSyncUnreadEvent(SyncUnreadEvent event) {
        syncRefreshAnimationState();

        AckChatboxIntentService.ack(this, event.getUnAckChatboxIds());
    }

    @Subscribe
    public void onUpdateUserProfileEvent(UpdateUserEvent event) {
        Exception exception = event.getException();
        if (exception != null) {
            handle(exception, R.string.error_failed_to_update_user_profile);
        }
    }

    @Subscribe
    public void onSyncCommunicationBoxEvent
            (SyncCommunicationBoxEvent event) {
        SyncCommunicationBoxEvent.Status status = event.getStatus();
        syncRefreshAnimationState();
        switch (status) {
            case STARTED:
                break;
            case SUCCEED:
                /*
                 * TODO when succeed, may need to check if the current chat
                 * box is still valid.
                 * And un-subscribe to all invalid chat boxes.
                 */

                ensureFayeIsConnected();
                startSyncingBookmarks();
                startSyncingCurrentUser();
                break;
            case FAILED:
                handle(event.getException(), R.string.error_failed_to_sync_data);
                break;
        }
    }

    /**
     * After deleting bookmark on server, we delete on client
     *
     * @param event
     */
    @Subscribe
    public void onDeletedBookmarkEvent(DeleteBookmarkEvent event) {
        if (handleDeleteBookmarkEvent(event)) {
            return;
        }

        DeleteBookmarkResponse.DeletedBookmark deletedBookmark = event.getResponse().getData();
        if (deletedBookmark == null) {
            LogUtils.e("Hmm... No data again.." + event.getResponse());
            return;
        }
        Uri syncedBookmarkWithChatBoxIdUri = ChatWingContentProvider
                .getSyncedBookmarkWithChatBoxIdUri(deletedBookmark.getChatBoxId());

        int delete = getContentResolver()
                .delete(syncedBookmarkWithChatBoxIdUri,
                        null,
                        null);
        if (delete != 1) {
            //Weird thing happen
            LogUtils.e("After deleting on server, bookmark removal on client side has broken chatbox_id" +
                    deletedBookmark.getChatBoxId());
        }
    }

    @Subscribe
    public void onBlockedUser(BlockedEvent event) {
        if (event.getException() == null) {
            Fragment fragmentByTag = getSupportFragmentManager().findFragmentByTag(BLOCK_USER_DIALOG_FRAGMENT_TAG);
            if (fragmentByTag != null) {
                ((DialogFragment) fragmentByTag).dismiss();
            }
            mErrorMessageView.show(R.string.message_blocked);
            return;
        }
        if (event.getException() instanceof ApiManager.ValidationException) {
            //Ignore this exception since it handle on BlockFragmentDialog
            return;
        }
        if (event.getException() instanceof ApiManager.RequiredPermissionException) {
            mErrorMessageView.show(event.getException(),
                    getString(R.string.error_require_permission_to_view_ip));
            return;
        }

        handle(event.getException(), R.string.error_while_blocking_user);
    }

    @Subscribe
    public void onUserUnAuthenticatedEvent(UserUnauthenticatedEvent event) {
        mQuickMessageView.show(R.string.message_need_login);
    }

    @Subscribe
    public void onChannelSubscriptionChanged(ChannelSubscriptionChangedEvent event) {
        //Faye
        if (event.getStatus() == ChannelSubscriptionChangedEvent.Status.SUCCEED) {
            if (Constants.DEBUG) {
                mQuickMessageView.show(getString(R.string.message_subscribed_to_channel) + event.getChannel());
            }
            LogUtils.v("Subscribed to channel: " + event.getChannel());
        } else {
            // Failed
            mErrorMessageView.show(getString(R.string.message_error) + event.getError());
            LogUtils.e(event.getError());
        }
    }

    @Subscribe
    public void onFayePublished(FayePublishEvent event) {
        //Faye
        if (event.getStatus() == FayePublishEvent.Status.SUCCEED) {
            if (Constants.DEBUG) {
                mQuickMessageView.show(R.string.message_published);
            }
            LogUtils.v("Published: " + event.toString());
        } else {
            // Failed
            mErrorMessageView.show(getString(R.string.message_error) + event.getError());
            LogUtils.e(event.getError());
        }
    }

    @Subscribe
    public void onMessageReceived(MessageReceivedEvent event) {
        //Faye
        try {
            Event e = mEventParser.parse(event.getMessage());
            processEvent(e);
        } catch (JSONException ex) {
            handle(ex, R.string.message_error);
        }
    }

    @Subscribe
    public void onServerConnectionChangedEvent(ServerConnectionChangedEvent event) {
        //Faye
        if (event.getStatus() == ServerConnectionChangedEvent.Status.CONNECTED) {
            if (Constants.DEBUG) {
                mQuickMessageView.show(R.string.message_connected_to_server);
            }
            LogUtils.v("Connected to server.");

            setupSnackbar(mContentView);
            styleInfoSnackbar();
            snackbar.setText(R.string.message_connected_to_server);
            snackbar.setDuration(Snackbar.LENGTH_SHORT);
            snackbar.show();
        } else {
            // Disconnected
            if (Constants.DEBUG) {
                mQuickMessageView.show(R.string.message_disconnected_from_server);
            }
            LogUtils.v("Disconnected from server.");
            setupSnackbar(mContentView);
            styleErrorSnackbar();
            snackbar.setAction("RETRY", snackbarRetryAction);
            snackbar.setText(R.string.error_faye_closed);
            snackbar.setDuration(Snackbar.LENGTH_INDEFINITE);
            snackbar.show();
        }
    }

    @Subscribe
    public void onFayeFailedEvent(FayeFailedEvent event) {
        mErrorMessageView.show(R.string.error_faye_fail_event);
    }

    @Subscribe
    public void onUserSelectedDefaultUsersEvent(UserSelectedDefaultUsersEvent event) {
        showConversation(event.getSimpleUser());
    }

    private void processEvent(Event event) {
        String name = event.getName();
        LogUtils.v("event " + event.getParams());
        if (name.equals(EventParser.EVENT_DELETE_MESSAGE)) {
            mCurrentCommunicationMode.processDeleteMessageEvent(event);
        } else if (name.equals(EventParser.EVENT_DELETE_MESSAGE_BY_SOCIAL)) {
            mCurrentCommunicationMode.processDeleteMessagesBySocialAccountEvent(event);
        } else if (name.equals(EventParser.EVENT_DELETE_MESSAGE_BY_IP)) {
            mCurrentCommunicationMode.processDeleteMessagesByIPEvent(event);
        } else if (name.equals(EventParser.EVENT_NEW_MESSAGE) || name.equals(EventParser.EVENT_NETWORK_NEW_MESSAGE)) {
            Message message = (Message) event.getParams();
            message.setStatus(Message.Status.PUBLISHED);

            if (!isFromMe(message)) {
                // New message, insert it into DB.
                Uri uri = ChatWingContentProvider.getMessagesUri();
                ContentValues contentValues = MessageTable.getContentValues(message);
                Uri result = getContentResolver().insert(uri, contentValues);
                if ("-1".equals(result.getLastPathSegment())) {
                    // Failed to insert the message. Stop here.
                    LogUtils.e("Insert message failed ");
                    return;
                }
            }

            boolean added;
            boolean isInCurrentCommunicationBox = mCurrentCommunicationMode.isInCurrentCommunicationBox(message);

            if (!isInCurrentCommunicationBox) {
                added = mCurrentCommunicationMode.processMessageNotInCurrentCommunicationBox(message);
            } else {
                CommunicationMessagesFragment fragment = getCommunicationMessagesFragment();
                added = (fragment != null && fragment.addNewMessage(message));
                mCurrentCommunicationMode.processMessageInCurrentCommunicationBox(message);
            }

            //Handle sound when app is visible
            //When app not visible chatservice will handle it
            if (mUserManager.isSoundEnabled()
                    &&
                    (!appIsNotVisible() && !isInCurrentCommunicationBox)) {
                mSoundEffectsPool.play(mNewMessageSoundId, 1.0f, 1.0f, 0, 0, 1);
            }
        }
    }

    //////////////////////////
    // Instance methods
    //////////////////////////
    private boolean isFromMe(Message message) {
        User currentUser = mUserManager.getCurrentUser();
        if (currentUser == null) {
            return false;
        }
        return mUserManager.isCurrentUser(message.getUserIdentifier());
    }

    private void initConversationMenu() {
        setTitle(getActivity().getString(R.string.title_activity_conversation));

        if (!isInConversationMode()) {
            setupConversationMode();
        }
        invalidateOptionsMenu();
    }

    private void onUserUnauthenticated() {
        //Right now we only show this lovely message, future we will show other login options
        mErrorMessageView.show(R.string.error_auth_required);
    }

    protected boolean isInChatBoxMode() {
        return mCurrentCommunicationMode == null || mCurrentCommunicationMode instanceof ChatboxModeManager;
    }

    protected boolean isInConversationMode() {
        return mCurrentCommunicationMode == null || mCurrentCommunicationMode instanceof ConversationModeManager;
    }

    protected void addToLeftDrawer(Fragment fragment) {
        addToLeftDrawer(fragment, true);
    }

    private void addToLeftDrawer(Fragment fragment, boolean animate) {
        if (animate) {
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_right,
                            R.anim.slide_out_left,
                            R.anim.slide_in_right, R.anim.slide_out_left)
                    .replace(R.id.left_drawer_container, fragment)
                    .addToBackStack(null)
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.left_drawer_container, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private Fragment getChatBoxesFragment() {
        return new CategoriesFragment();
    }

    protected void setupMode(CommunicationModeManager newMode,
                             Fragment newFragment) {
        /*
         * Update fragments
         */
        String fragmentTag = getString(R.string.tag_communication_messages);
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment oldFragment = fragmentManager.findFragmentByTag(fragmentTag);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if (oldFragment != null) {
            fragmentTransaction.remove(oldFragment);
        }
        fragmentTransaction.add(R.id.fragment_container, newFragment, fragmentTag);
        fragmentTransaction.commit();

        /*
         * Deactivate old mode and activate the new one
         */
        if (mCurrentCommunicationMode != null) {
            mCurrentCommunicationMode.deactivate();
        }
        mCurrentCommunicationMode = newMode;
        mCurrentCommunicationMode.activate();

        /*
         * Setup drawer layout
         */
        // Set custom shadows that overlay the main content when a drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow_left, Gravity.LEFT);
        mDrawerToggle = mCurrentCommunicationMode.getDrawerToggleListener();
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        // Don't allow the drawer layout to catch back button and close itself
        // on back key is pressed. This activity will handle it.
        mDrawerLayout.setFocusableInTouchMode(false);

        invalidateOptionsMenu();
    }

    private Context getDialogContext() {
        Context context;
        if (getParent() != null) context = getParent();
        else context = this;
        return context;
    }

    private Class<? extends BaseABFragmentActivity> getEntranceActivityClass() {
        return mBuildManager.isOfficialChatWingApp() ? CommunicationActivity.class : WhiteLabelCoverActivity.class;
    }

    private boolean startSyncingCommunications(boolean needReload) {
        if (SyncCommunicationBoxesIntentService.isInProgress()) {
            // A sync operation is running. Just wait for it.
            return false;
        }
        mSyncManager.resetQueue();
        mSyncManager.setNeedReload(needReload);
        mSyncManager.addToQueue(SyncCommunicationBoxesIntentService.class);
        Conversation conversation = mCurrentConversationManager.getCurrentConversation();
        ChatBox chatBox = mCurrentChatboxManager.getCurrentChatBox();
        Intent intent = new Intent(this, SyncCommunicationBoxesIntentService.class);
        if (conversation != null) {
            intent.putExtra(SyncCommunicationBoxesIntentService.EXTRA_CONVERSATION_ID, conversation.getId());
        }

        startService(intent);

        //Send ack, we reset unread_count for current opening conversation
        if (conversation != null) {
            LogUtils.v("Test ACK startSyncingCommunications");
            AckConversationIntentService.ack(this, conversation.getId());
        }

        if (chatBox != null) {
            AckChatboxIntentService.ack(this, chatBox.getId());
        }

        mSyncManager.addToQueue(DownloadUserDetailIntentService.class);
        return true;
    }

    private int getActionMode(Intent intent) {
        if (ACTION_OPEN_CONVERSATION.equals(intent.getAction())) {
            return MODE_CONVERSATION;
        }
        if (ACTION_OPEN_CHATBOX.equals(intent.getAction())) {
            return MODE_CHAT_BOX;
        }
        return MODE_NONE;
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            showGooglePlusDialogFragment(resultCode, REQUEST_CODE_GET_GOOGLE_PLAY_SERVICES);
            return false;
        }
        return true;
    }

    private void showGooglePlusDialogFragment(int errorCode, int requestCode) {
        GooglePlusDialogFragment fragment = GooglePlusDialogFragment.newInstance(
                errorCode, requestCode);
        if (fragment != null) {
            fragment.show(getSupportFragmentManager(), GooglePlusDialogFragment.DIALOG_TAG);
        }
    }

    private void updateGcm(String action, boolean shouldSupplyUser) {
        User user = mUserManager.getCurrentUser();
        if (user == null) {
            // User is required by the server, so do nothing if it is unavailable.
            return;
        }
        Intent i = new Intent(this, UpdateGcmIntentService.class);
        i.setAction(action);
        if (shouldSupplyUser) {
            i.putExtra(UpdateGcmIntentService.EXTRA_USER, user);
        }
        startService(i);
    }

    protected void syncRefreshAnimationState() {
        LogUtils.v("syncRefreshAnimationState " + syncingInProcess());
        // Start refresh animation if the chat boxes drawer is opened and a
        // sync operation is running.
        // Stop the animation if that drawer is closed.
        if (syncingInProcess()) {
            startRefreshAnimation();
        } else {
            stopRefreshAnimation();
        }
    }

    protected void stopRefreshAnimation() {
        mLoadingView.setVisibility(View.GONE);
    }

    protected void startRefreshAnimation() {
        mLoadingView.setVisibility(View.VISIBLE);
    }

    private boolean syncingInProcess() {
        return SyncCommunicationBoxesIntentService.isInProgress()
                || SyncBookmarkIntentService.isInProgress()
                || ChatboxUnreadDownloadManager.isRunning();
    }

    private void onAccessTokenExpired() {
        mErrorMessageView.show(R.string.error_invalid_access_token);
        logout();
    }

    ///////////////////////////////////////////////////////
    ///            OnlineUsersFragmentDelegate
    ////////////////////////////////////////////////////////////
    @Override
    public void createConversation(CreateConversationParams.SimpleUser simpleUser) {
        showConversation(simpleUser);
    }

    /////////////////////////////////////////////////////////
    //              MediaControlInterface
    /////////////////////////////////////////////////////////

    @Override
    public MusicService.STATUS getMediaStatus() {
        if (musicBound) {
            return musicService.getStatus();
        }
        return null;
    }

    @Override
    public MusicService getMediaService() {
        return musicService;
    }

    @Override
    public void playLastMediaIfStopping() {
        Intent service = new Intent(MusicService.ACTION_PLAY_LAST_MEDIA_IF_STOPPING);
        startService(service);
    }

    @Override
    public boolean isBindMediaService() {
        return musicBound;
    }

    @Override
    public void enqueue(Song song) {
        Intent i = new Intent(MusicService.ACTION_URL);
        i.putExtra(MusicService.SONG_EXTRA, song);
        startService(i);
    }

    @Override
    public void updateUIForPlayerPreparing(boolean preparing) {
        if (preparing) {
            startRefreshAnimation();
        } else {
            stopRefreshAnimation();
            syncRefreshAnimationState(); // Make sure if sync is in progress, continue show
        }
    }


    private void startAndBindMusicService() {
        if (mBuildManager.isSupportedMusicBox()) {
            if (playIntent == null) {
                playIntent = new Intent(this, MusicService.class);
                startService(playIntent);
            }
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
        }
    }

    private synchronized void showAccountPicker(String message) {
        Fragment oldFragment = getSupportFragmentManager().findFragmentByTag(
                ACCOUNT_DIALOG_FRAGMENT_TAG);
        if (oldFragment == null
                && isActive()) { // To prevent showdialog when activity is paused.
            // Better Workaround http://stackoverflow.com/questions/8040280/how-to-handle-handler-messages-when-activity-fragment-is-paused
            AccountDialogFragment accountDialogFragment = AccountDialogFragment.newInstance(message);
            accountDialogFragment.show(getSupportFragmentManager(),
                    ACCOUNT_DIALOG_FRAGMENT_TAG);
            //This to prevent duplication dialog. This should be used together with findFragmentByTag
            getSupportFragmentManager().executePendingTransactions();
        }
    }

    private void showAvatarPicker() {
        Fragment oldFragment = getSupportFragmentManager().findFragmentByTag(
                AVATAR_PICKER_DIALOG_FRAGMENT_TAG);
        if (oldFragment == null) {
            PhotoPickerDialogFragment accountDialogFragment = PhotoPickerDialogFragment.newInstance();
            accountDialogFragment.show(getSupportFragmentManager(),
                    AVATAR_PICKER_DIALOG_FRAGMENT_TAG);
            //This to prevent duplication dialog. This should be used together with findFragmentByTag
            getSupportFragmentManager().executePendingTransactions();
        }
    }

    private boolean isInFeedMode() {
        return mCurrentCommunicationMode == null || mCurrentCommunicationMode instanceof FeedModeManager;
    }


    private boolean isInMusicBoxMode() {
        return mCurrentCommunicationMode == null || mCurrentCommunicationMode instanceof MusicModeManager;
    }

    private void setupFeedMode() {
        setupMode(mFeedModeManager, FeedFragment.newInstance());
        setContentShown(true);
    }

    private void setupMusicBoxMode() {
        setupMode(mMusicModeManager, MusicFragment.newInstance());
        setContentShown(true);
    }

    private void startUpdateAvatar(String filePath) {
        if (UpdateAvatarIntentService.isInProgress()) {
            return;
        }

        Intent startIntent = new Intent(this, UpdateAvatarIntentService.class);
        startIntent.putExtra(UpdateAvatarIntentService.EXTRA_AVATAR_PATH, filePath);
        startService(startIntent);
    }

    private boolean onInvalidAuthentication(ApiManager.InvalidIdentityException invalidIdentityException) {
        ChatWingError error = invalidIdentityException.getError();
        ChatBoxDetailsResponse.ChatBoxDetailErrorParams chatBoxDetailErrorParams =
                new Gson().fromJson(error.getParams(), ChatBoxDetailsResponse.ChatBoxDetailErrorParams.class);

        if (chatBoxDetailErrorParams == null) {
            return true;
        }
        if (chatBoxDetailErrorParams.isForceLogin()
                && mUserManager.getCurrentUser() == null) {
            denyAccessCurrentManager();
            showAccountPicker(getString(R.string.message_need_login));
            return true;
        }

        if (!mUserManager.acceptAccessChatbox(mUserManager.getCurrentUser(),
                chatBoxDetailErrorParams)) {
            denyAccessCurrentManager();
            showAccountPicker(getString(R.string.message_need_switch_account, chatBoxDetailErrorParams.getAuthenticationMethodString()));
            return true;
        }

        //No one can access this chatbox except admin, etc...
        return false;
    }

    /**
     * User has been denied to access chatbox, they should be kicked out
     */
    private void denyAccessCurrentManager() {
        if (isInChatBoxMode()) {
            mCurrentChatboxManager.removeCurrentChatBox();
        } else {
            mCurrentConversationManager.removeCurrentConversation();
        }
    }

    private void startSyncingCurrentUser() {
        if (mUserManager.getCurrentUser() == null
                || DownloadUserDetailIntentService.isInProgress()) {
            // A sync operation is running. Just wait for it.
            return;
        }

        getActivity().startService(new Intent(getActivity(), DownloadUserDetailIntentService.class));

    }

    private boolean handleDeleteBookmarkEvent(DeleteBookmarkEvent event) {
        if (event.getException() == null)
            return false;
        handle(event.getException(), R.string.error_while_deleting_bookmark);
        return true;
    }

    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            //get service
            musicService = binder.getService();
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    private void startSyncingBookmarks() {
        if (!mBuildManager.isOfficialChatWingApp()) return;

        if (mUserManager.getCurrentUser() == null
                || SyncBookmarkIntentService.isInProgress()) {
            // A sync operation is running. Just wait for it.
            return;
        }
        mSyncManager.addToQueue(SyncBookmarkIntentService.class);

        getActivity().startService(new Intent(getActivity(), SyncBookmarkIntentService.class));
    }


    private void deployGCM() {
        // Check GCM and register if needed
        if (checkPlayServices()) {
            String regId = mGcmManager.getRegistrationId();
            if (TextUtils.isEmpty(regId)) {
                updateGcm(ApiManager.GCM_ACTION_ADD, false);
            }
        }
    }

    private int getSoundNewMessageId() {
        return mSoundEffectsPool.load(this, R.raw.new_message, 1);
    }

    private boolean appIsNotVisible() {
        return !isVisible();
    }

    private void styleInfoSnackbar() {
        ViewGroup group = (ViewGroup) snackbar.getView();
        group.setBackgroundColor(getResources().getColor(R.color.primary));
        snackbar.setActionTextColor(getResources().getColor(R.color.accent));
        TextView tv = (TextView) group.findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextColor(getResources().getColor(R.color.text_on_primary));
    }

    private void styleErrorSnackbar() {
        ViewGroup group = (ViewGroup) snackbar.getView();
        group.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
        snackbar.setActionTextColor(getResources().getColor(R.color.white));
        TextView tv = (TextView) group.findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextColor(getResources().getColor(R.color.white));
    }
}
