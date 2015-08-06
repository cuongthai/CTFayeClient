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


import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.chatwing.whitelabel.ChatWing;
import com.chatwing.whitelabel.Constants;
import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.contentproviders.ChatWingContentProvider;
import com.chatwing.whitelabel.events.AllSyncsCompletedEvent;
import com.chatwing.whitelabel.events.SyncCommunicationBoxEvent;
import com.chatwing.whitelabel.events.UserUnauthenticatedEvent;
import com.chatwing.whitelabel.events.faye.ChannelSubscriptionChangedEvent;
import com.chatwing.whitelabel.events.faye.FayePublishEvent;
import com.chatwing.whitelabel.events.faye.MessageReceivedEvent;
import com.chatwing.whitelabel.events.faye.ServerConnectionChangedEvent;
import com.chatwing.whitelabel.fragments.AdminListFragment;
import com.chatwing.whitelabel.fragments.CategoriesFragment;
import com.chatwing.whitelabel.fragments.ChatMessagesFragment;
import com.chatwing.whitelabel.fragments.ColorPickerDialogFragment;
import com.chatwing.whitelabel.fragments.CommunicationDrawerFragment;
import com.chatwing.whitelabel.fragments.CommunicationMessagesFragment;
import com.chatwing.whitelabel.fragments.ConversationMessagesFragment;
import com.chatwing.whitelabel.fragments.ConversationsFragment;
import com.chatwing.whitelabel.fragments.GooglePlusDialogFragment;
import com.chatwing.whitelabel.fragments.InjectableFragmentDelegate;
import com.chatwing.whitelabel.fragments.NavigatableFragmentListener;
import com.chatwing.whitelabel.fragments.NewContentFragment;
import com.chatwing.whitelabel.fragments.PasswordDialogFragment;
import com.chatwing.whitelabel.fragments.ProfileFragment;
import com.chatwing.whitelabel.interfaces.ChatWingJSInterface;
import com.chatwing.whitelabel.managers.ApiManager;
import com.chatwing.whitelabel.managers.BuildManager;
import com.chatwing.whitelabel.managers.ChatboxModeManager;
import com.chatwing.whitelabel.managers.CommunicationActivityManager;
import com.chatwing.whitelabel.managers.CommunicationModeManager;
import com.chatwing.whitelabel.managers.ConversationModeManager;
import com.chatwing.whitelabel.managers.CurrentChatBoxManager;
import com.chatwing.whitelabel.managers.CurrentConversationManager;
import com.chatwing.whitelabel.managers.GcmManager;
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
import com.chatwing.whitelabel.pojos.User;
import com.chatwing.whitelabel.pojos.params.CreateConversationParams;
import com.chatwing.whitelabel.services.AckChatboxIntentService;
import com.chatwing.whitelabel.services.AckConversationIntentService;
import com.chatwing.whitelabel.services.CreateConversationIntentService;
import com.chatwing.whitelabel.services.OfflineIntentService;
import com.chatwing.whitelabel.services.SyncCommunicationBoxesIntentService;
import com.chatwing.whitelabel.services.UpdateGcmIntentService;
import com.chatwing.whitelabel.tables.MessageTable;
import com.chatwing.whitelabel.utils.LogUtils;
import com.chatwing.whitelabel.views.BBCodeEditText;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
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
        NavigatableFragmentListener,
        ProfileFragment.Listener {
    public static final String PASSWORD_DIALOG_FRAGMENT_TAG = "PasswordDialogFragmentTAG";
    private static final String EXTRA_CURRENT_MODE = "current_mode";
    private static final String ATTACHMENT_CONTENT_FRAGMENT_TAG = "NewContentFragment";
    private static final int MODE_CHAT_BOX = 0;
    private static final int MODE_NONE = -1;
    private static final int MODE_CONVERSATION = 1;
    public static final int REQUEST_CODE_AUTHENTICATION = 10000;
    private static final int REQUEST_CODE_GET_GOOGLE_PLAY_SERVICES = 9000;
    public static final String CHATBOX_ID = "CHATBOX_ID";
    public static final String ACTION_OPEN_CHATBOX = "ACTION_OPEN_CHATBOX";
    public static final String ACTION_OPEN_CONVERSATION = "ACTION_OPEN_CONVERSATION";
    public static final String CONVERSATION_ID = "CONVERSATION_ID";

    protected WebView mWebView;
    protected boolean mNotSubscribeToChannels;
    @Inject
    protected GcmManager mGcmManager;
    @Inject
    ChatWingJSInterface mFayeJsInterface;
    @Inject
    protected ChatboxModeManager mChatboxModeManager;
    @Inject
    BuildManager mBuildManager;
    @Inject
    ConversationModeManager mConversationModeManager;
    @Inject
    Bus mBus;
    @Inject
    UserManager mUserManager;
    @Inject
    ApiManager mApiManager;
    @Inject
    EventParser mEventParser;
    @Inject
    protected SyncManager mSyncManager;
    @Inject
    protected CurrentConversationManager mCurrentConversationManager;
    @Inject
    protected CurrentChatBoxManager mCurrentChatboxManager;
    @Inject
    CommunicationActivityManager mCommunicationActivityManager;

    protected CommunicationModeManager mCurrentCommunicationMode;

    private View mContentView;
    private View mProgressView;
    private ProgressBar mProgressBar;
    private TextView mProgressText;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    // Flag to determined onCreate is called. It is used onResume to decide
    // whether a sync operation should be triggered or not and reset right
    // after that.
    private boolean mIsCreated;
    private ProgressBar mLoadingView;

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

        mChatboxModeManager.onCreate(savedInstanceState);
        mConversationModeManager.onCreate(savedInstanceState);

        stopRefreshAnimation();

        //This mode is priority due to user action requesting open
        int actionMode = getActionMode();
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
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
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
    }

    protected void deployGCM() {
        // Check GCM and register if needed
        if (checkPlayServices()) {
            String regId = mGcmManager.getRegistrationId();
            if (TextUtils.isEmpty(regId)) {
                updateGcm(ApiManager.GCM_ACTION_ADD, false);
            }
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtils.v("On Destroy Activity");
        //Faye connection will stop receiving msg when the is destroyed
        //Either by user(backpress) or system destroy
        mBus.unregister(this);

        destroyWebview();

        mCurrentCommunicationMode.onDestroy();
        mCurrentCommunicationMode = null;
    }

    private void destroyWebview() {
        mNotSubscribeToChannels = true;
        if (mWebView != null) {
            mWebView.destroy();
            mWebView = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT >= 11) {
            if (mWebView != null) {
                mWebView.onResume();
            }
        }
        ensureWebViewAndSubscribeToChannels();
        syncRefreshAnimationState();

        mChatboxModeManager.onResume();
        mConversationModeManager.onResume();
        invalidateOptionsMenu();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mWebView != null) {
            if (Build.VERSION.SDK_INT >= 11) {
                mWebView.onPause();
            }
        }
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
    public final void onConfirmColor(Serializable code, int color) {
        BBCodePair pair = new BBCodePair((BBCodeParser.BBCode) code, color);
        getCommunicationMessagesFragment().appendBBCode(pair);
    }

    @Override
    public final void inject(Fragment fragment) {
        super.inject(fragment);
    }

    @Override
    public void handle(Exception exception, int errorMessageResId) {
        LogUtils.v("Handle error " + exception);
        if (exception instanceof ApiManager.UserUnauthenticatedException) {
            onUserUnauthenticated();
        } else if (exception instanceof ApiManager.InvalidAccessTokenException) {
            onAccessTokenExpired();
        } else if (exception instanceof ApiManager.NotVerifiedEmailException) {
            mErrorMessageView.show(errorMessageResId);
        } else if (exception instanceof ApiManager.OtherApplicationException) {
            mErrorMessageView.show(((ApiManager.OtherApplicationException) exception).getError().getMessage());
            logout();
        } else {
            mErrorMessageView.show(exception, getString(R.string.error_unknown));
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
        LogUtils.v("Test Chatbox not display, shown: " + shown);
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

    @Override
    public WebView getFayeWebView() {
        return mWebView;
    }

    /**
     * Calls this in subclass to use it as Conversation Mode
     */
    protected void setupConversationMode() {
        setupMode(mConversationModeManager, ConversationMessagesFragment.newInstance());
    }

    /**
     * Calls this in subclass to use it as Public ChatBox Mode
     */
    protected void setupChatboxMode() {
        setupMode(mChatboxModeManager, ChatMessagesFragment.newInstance());
    }

    /**
     * Requires to be overriden by subclass to be called by otto
     *
     * @param event
     */
    protected void onUserUnAuthenticatedEvent(UserUnauthenticatedEvent event) {
        mQuickMessageView.show(R.string.message_need_login);
    }

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

    @Override
    public void showAdminList() {
        if (mBuildManager.canShowAdminList()) {
            addToLeftDrawer(new AdminListFragment());
        } else {
            mErrorMessageView.show(R.string.error_empty_message);
        }
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

    @Override
    public WebView getWebView() {
        return mWebView;
    }

    @Override
    public void showConversation(CreateConversationParams.SimpleUser simpleUser) {
        initConversationMenu();
        addToLeftDrawer(new ConversationsFragment(), false);

        Intent createConversation = new Intent(getActivity(), CreateConversationIntentService.class);
        createConversation.putExtra(CreateConversationIntentService.EXTRA_USER, simpleUser);
        startService(createConversation);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void ensureWebViewAndSubscribeToChannels() {
        if (mCurrentCommunicationMode == null) return;
        // Check whether the web view is available or not.
        // If not, init it and load faye client. When loading finished,
        // this method will be recursively called. At that point,
        // the actual subscribe code will be executed.
        if (mWebView == null) {
            mNotSubscribeToChannels = true;
            mWebView = new WebView(this);

            WebSettings webSettings = mWebView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setDomStorageEnabled(true);

            mWebView.addJavascriptInterface(
                    mFayeJsInterface,
                    ChatWingJSInterface.CHATWING_JS_NAME);

            mWebView.setWebChromeClient(new WebChromeClient() {
                @Override
                public boolean onConsoleMessage(ConsoleMessage consoleMessage) {

                    LogUtils.v(consoleMessage.message()
                            + " -- level " + consoleMessage.messageLevel()
                            + " -- From line " + consoleMessage.lineNumber()
                            + " of " + consoleMessage.sourceId());

                    //This workaround tries to fix issue webview is not subscribe successfully
                    //when the screen is off, we cant listen for otto event since it's dead before that
                    //this likely happens on development or very rare case in production
                    if (consoleMessage.messageLevel().equals(ConsoleMessage.MessageLevel.ERROR)) {
                        destroyWebview();
                    }
                    return true;
                }
            });

            mWebView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);

                    if (url.equals(Constants.FAYE_CLIENT_URL)) {
                        // Recursively call this method,
                        // to execute subscribe code.
                        ensureWebViewAndSubscribeToChannels();
                    }
                }
            });

            mWebView.loadUrl(Constants.FAYE_CLIENT_URL);
            return;
        }
        if (mNotSubscribeToChannels) {
            mNotSubscribeToChannels = false;
            mCurrentCommunicationMode.subscribeToChannels(mWebView);
        }
    }


    //////////////////////////////////////////
    ///     Otto
    //////////////////////////////////////////

    @Subscribe
    public void onAllSyncsCompleted(AllSyncsCompletedEvent event) {
        LogUtils.v("All Sync Completed");
        destroyWebview();
        ensureWebViewAndSubscribeToChannels();


        if (event.needReload() &&
                mCurrentCommunicationMode != null) {
            mCurrentCommunicationMode.reloadCurrentBox();
        }
    }

    @Subscribe
    public void onChannelSubscriptionChanged(ChannelSubscriptionChangedEvent event) {
        //Faye
        if (event.getStatus() == ChannelSubscriptionChangedEvent.Status.SUCCEED) {
            if (Constants.DEBUG) {
                mQuickMessageView.show(getString(R.string.message_subscribed_to_channel) + event.getChannel());
            }
            LogUtils.v("Subscribed to channel: " + event.getChannel());
            mNotSubscribeToChannels = false;
        } else {
            // Failed
            mErrorMessageView.show(getString(R.string.message_error) + event.getError());
            LogUtils.e(event.getError());
            //We resubscribe next time
            destroyWebview();
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
            if (Constants.DEBUG) {
//                mQuickMessageView.show(getString(R.string.message_event) + e.getName());
//                LogUtils.v("Event: " + e.getName());
            }
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
        } else {
            // Disconnected
            if (Constants.DEBUG) {
                mQuickMessageView.show(R.string.message_disconnected_from_server);
            }
            LogUtils.v("Disconnected from server.");
        }
    }

    @Subscribe
    public void onSyncCommunicationBoxEvent(SyncCommunicationBoxEvent event) {
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

                ensureWebViewAndSubscribeToChannels();
                break;
            case FAILED:
                handle(event.getException(), R.string.error_failed_to_sync_data);
                break;
        }
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
                    .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left,
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
                dialog = ProgressDialog.show(CommunicationActivity.this, "",
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

                Intent i = new Intent(CommunicationActivity.this, getEntranceActivityClass());
                startActivity(i);
                finish();
            }
        }.execute();
    }


    protected Class<? extends BaseABFragmentActivity> getEntranceActivityClass() {
        return CommunicationActivity.class;
    }

    protected boolean startSyncingCommunications(boolean needReload) {
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


        return true;
    }

    private int getActionMode() {
        if (ACTION_OPEN_CONVERSATION.equals(getIntent().getAction())) {
            return MODE_CONVERSATION;
        }
        if (ACTION_OPEN_CHATBOX.equals(getIntent().getAction())) {
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
            fragment.show(getSupportFragmentManager(), "google_plus_dialog");
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

    protected boolean syncingInProcess() {
        return SyncCommunicationBoxesIntentService.isInProgress();
    }

    private void onAccessTokenExpired() {
        mErrorMessageView.show(R.string.error_invalid_access_token);
        logout();
    }

}
