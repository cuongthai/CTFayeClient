package com.chatwing.whitelabel.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.contentproviders.ChatWingContentProvider;
import com.chatwing.whitelabel.events.AccountSwitchEvent;
import com.chatwing.whitelabel.events.AllSyncsCompletedEvent;
import com.chatwing.whitelabel.events.BlockedEvent;
import com.chatwing.whitelabel.events.DeleteBookmarkEvent;
import com.chatwing.whitelabel.events.SyncCommunicationBoxEvent;
import com.chatwing.whitelabel.events.SyncUnreadEvent;
import com.chatwing.whitelabel.events.UpdateUserEvent;
import com.chatwing.whitelabel.events.faye.ChannelSubscriptionChangedEvent;
import com.chatwing.whitelabel.events.faye.FayePublishEvent;
import com.chatwing.whitelabel.events.faye.MessageReceivedEvent;
import com.chatwing.whitelabel.events.faye.ServerConnectionChangedEvent;
import com.chatwing.whitelabel.fragments.AccountDialogFragment;
import com.chatwing.whitelabel.fragments.BlockUserDialogFragment;
import com.chatwing.whitelabel.fragments.BookmarkedChatBoxesDrawerFragment;
import com.chatwing.whitelabel.fragments.CommunicationMessagesFragment;
import com.chatwing.whitelabel.fragments.ExtendChatMessagesFragment;
import com.chatwing.whitelabel.fragments.ExtendCommunicationDrawerFragment;
import com.chatwing.whitelabel.fragments.FeedDrawerFragment;
import com.chatwing.whitelabel.fragments.FeedFragment;
import com.chatwing.whitelabel.fragments.MusicDrawerFragment;
import com.chatwing.whitelabel.fragments.MusicFragment;
import com.chatwing.whitelabel.fragments.OnlineUsersFragment;
import com.chatwing.whitelabel.fragments.PhotoPickerDialogFragment;
import com.chatwing.whitelabel.fragments.SettingsFragment;
import com.chatwing.whitelabel.interfaces.MediaControlInterface;
import com.chatwing.whitelabel.managers.ApiManager;
import com.chatwing.whitelabel.managers.BuildManager;
import com.chatwing.whitelabel.managers.ChatboxUnreadDownloadManager;
import com.chatwing.whitelabel.managers.ExtendChatBoxModeManager;
import com.chatwing.whitelabel.managers.ExtendCommunicationModeManager;
import com.chatwing.whitelabel.managers.FeedModeManager;
import com.chatwing.whitelabel.managers.MusicModeManager;
import com.chatwing.whitelabel.managers.UserManager;
import com.chatwing.whitelabel.modules.CommunicationActivityModule;
import com.chatwing.whitelabel.modules.ExtendCommunicationActivityModule;
import com.chatwing.whitelabel.pojos.Message;
import com.chatwing.whitelabel.pojos.Song;
import com.chatwing.whitelabel.pojos.errors.ChatWingError;
import com.chatwing.whitelabel.pojos.params.CreateConversationParams;
import com.chatwing.whitelabel.pojos.responses.ChatBoxDetailsResponse;
import com.chatwing.whitelabel.pojos.responses.DeleteBookmarkResponse;
import com.chatwing.whitelabel.services.DownloadUserDetailIntentService;
import com.chatwing.whitelabel.services.MusicService;
import com.chatwing.whitelabel.services.SyncBookmarkIntentService;
import com.chatwing.whitelabel.services.SyncCommunicationBoxesIntentService;
import com.chatwing.whitelabel.services.UpdateAvatarIntentService;
import com.chatwing.whitelabel.utils.LogUtils;
import com.flurry.android.FlurryAgent;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.gson.Gson;
import com.soundcloud.android.crop.Crop;
import com.squareup.otto.Subscribe;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by steve on 10/12/2014.
 */
public class ExtendCommunicationActivity
        extends CommunicationActivity
        implements ExtendCommunicationDrawerFragment.Listener,
        OnlineUsersFragment.OnlineUsersFragmentDelegate,
        ExtendChatMessagesFragment.Delegate,
        ExtendCommunicationModeManager.Delegate,
        MediaControlInterface {

    public static final String AVATAR_PICKER_DIALOG_FRAGMENT_TAG = "AvatarPickerDialogFragment";
    public static final String BLOCK_USER_DIALOG_FRAGMENT_TAG = "BlockUserDialogFragment";
    public static final String ACCOUNT_DIALOG_FRAGMENT_TAG = "AccountDialogFragmentTag";
    public static final String ACTION_STOP_MEDIA = "ACTION_STOP_MEDIA";

    @Inject
    ApiManager mApiManager;
    @Inject
    UserManager mUserManager;
    @Inject
    BuildManager mBuildManager;
    @Inject
    ChatboxUnreadDownloadManager chatboxUnreadDownloadManager;
    @Inject
    protected FeedModeManager mFeedModeManager;
    @Inject
    protected MusicModeManager mMusicModeManager;

    private MusicService musicService;
    private Intent playIntent;
    private boolean musicBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String action = getIntent().getAction();
        if (ACTION_STOP_MEDIA.equals(action)) {
            startService(new Intent(MusicService.ACTION_STOP));
        }

        if (!mBuildManager.isOfficialChatWingApp() && userManager.getCurrentUser() == null) {
            startActivity(new Intent(this, StartActivity.class));
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
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (playIntent == null) {
            playIntent = new Intent(this, MusicService.class);
            startService(playIntent);
        }
        bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);

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
    protected void onResume() {
        super.onResume();
        chatboxUnreadDownloadManager.downloadUnread();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == Crop.REQUEST_CROP && resultCode == RESULT_OK) {
            Uri output = Crop.getOutput(intent);
            startUpdateAvatar(output.getPath());
        }
    }

    @Override
    protected Class<? extends BaseABFragmentActivity> getEntranceActivityClass() {
        return mBuildManager.isOfficialChatWingApp() ? ExtendCommunicationActivity.class : StartActivity.class;
    }

    @Override
    public void updateAvatar() {
        getDrawerLayout().closeDrawers();
        showAvatarPicker();
    }

    @Override
    protected List<Object> getModules() {
        return Arrays.<Object>asList(new CommunicationActivityModule(this),
                new ExtendCommunicationActivityModule(this));
    }

    @Subscribe
    public void onAllSyncsCompleted(AllSyncsCompletedEvent
                                            event) {
        super.onAllSyncsCompleted(event);

        chatboxUnreadDownloadManager.downloadUnread();
        syncRefreshAnimationState();
    }

    @Subscribe
    public void onSyncUnreadEvent(SyncUnreadEvent event) {
        syncRefreshAnimationState();
    }

    @Subscribe
    public void onUpdateUserProfileEvent(UpdateUserEvent event) {
        Exception exception = event.getException();
        if (exception != null) {
            handle(exception, R.string.error_failed_to_update_user_profile);
        }
    }

    @com.squareup.otto.Subscribe
    public void onServerConnectionChangedEvent
            (ServerConnectionChangedEvent event) {
        super.onServerConnectionChangedEvent(event);
    }

    @com.squareup.otto.Subscribe
    public void onSyncCommunicationBoxEvent
            (SyncCommunicationBoxEvent event) {
        super.onSyncCommunicationBoxEvent(event);
        SyncCommunicationBoxEvent.Status status = event.getStatus();

        switch (status) {
            case SUCCEED:
                startSyncingBookmarks();
                startSyncingCurrentUser();
                break;
        }
    }


    @Subscribe
    public void onChannelSubscriptionChanged
            (ChannelSubscriptionChangedEvent event) {
        super.onChannelSubscriptionChanged(event);
    }

    @Subscribe
    public void onFayePublished(FayePublishEvent event) {
        super.onFayePublished(event);
    }

    @Subscribe
    public void onMessageReceived(MessageReceivedEvent event) {
        super.onMessageReceived(event);
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

    @Override
    public void handle(Exception exception, int errorMessageResId) {
        if (exception instanceof ApiManager.InvalidIdentityException) {
            onInvalidAuthentication((ApiManager.InvalidIdentityException) exception);
        } else
            super.handle(exception, errorMessageResId);
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
    public void onBackPressed() {
        if (mCurrentCommunicationMode.isSecondaryDrawerOpening()) {
            ((ExtendChatBoxModeManager) mCurrentCommunicationMode).closeSecondaryDrawer();
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
    public void showSettings() {
        Intent i = new Intent(this, MainPreferenceActivity.class);
        i.putExtra(SettingsFragment.LOAD_LATEST_USER_PROFILE, true);
        startActivity(i);
    }

    @Override
    protected void setupChatboxMode() {
        setupMode(mChatboxModeManager, ExtendChatMessagesFragment.newInstance());
    }

    @Override
    public void createConversation(CreateConversationParams.SimpleUser simpleUser) {
        showConversation(simpleUser);
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
        mCurrentCommunicationMode.unsubscribeToChannels(mWebView);
        mWebView = null;
        mNotSubscribeToChannels = true;
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

    private void startSyncingBookmarks() {
        if (!mBuildManager.isOfficialChatWingApp()) return;

        if (mUserManager.getCurrentUser() == null
                || SyncBookmarkIntentService.isInProgress()) {
            // A sync operation is running. Just wait for it.
            return;
        }
        super.mSyncManager.addToQueue(SyncBookmarkIntentService.class);

        getActivity().startService(new Intent(getActivity(), SyncBookmarkIntentService.class));
    }

    @Override
    protected boolean syncingInProcess() {
        return SyncCommunicationBoxesIntentService.isInProgress() || SyncBookmarkIntentService.isInProgress() || ChatboxUnreadDownloadManager.isRunning();
    }

    @Override
    protected boolean startSyncingCommunications(boolean needReload) {
        boolean result = super.startSyncingCommunications(needReload);
        if (result) {
            super.mSyncManager.addToQueue(DownloadUserDetailIntentService.class);
        }
        return result;
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

    private boolean handleDeleteBookmarkEvent(DeleteBookmarkEvent event) {
        if (event.getException() == null)
            return false;
        mErrorMessageView.show(event.getException());
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
}
