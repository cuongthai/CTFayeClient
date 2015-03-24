package com.chatwing.whitelabel.activities;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.events.AccountSwitchEvent;
import com.chatwing.whitelabel.events.BlockedEvent;
import com.chatwing.whitelabel.events.DeleteBookmarkEvent;
import com.chatwing.whitelabel.fragments.AccountDialogFragment;
import com.chatwing.whitelabel.fragments.BlockUserDialogFragment;
import com.chatwing.whitelabel.fragments.BookmarkedChatBoxesDrawerFragment;
import com.chatwing.whitelabel.fragments.ExtendChatMessagesFragment;
import com.chatwing.whitelabel.fragments.ExtendCommunicationDrawerFragment;
import com.chatwing.whitelabel.fragments.OnlineUsersFragment;
import com.chatwing.whitelabel.fragments.PhotoPickerDialogFragment;
import com.chatwing.whitelabel.fragments.SettingsFragment;
import com.chatwing.whitelabel.managers.ApiManager;
import com.chatwing.whitelabel.managers.BuildManager;
import com.chatwing.whitelabel.managers.ExtendChatBoxModeManager;
import com.chatwing.whitelabel.managers.ExtendCommunicationModeManager;
import com.chatwing.whitelabel.modules.ExtendCommunicationActivityModule;
import com.chatwing.whitelabel.pojos.responses.DeleteBookmarkResponse;
import com.chatwing.whitelabel.services.DownloadUserDetailIntentService;
import com.chatwing.whitelabel.services.SyncBookmarkIntentService;
import com.chatwing.whitelabel.services.UpdateAvatarIntentService;
import com.chatwingsdk.activities.BaseABFragmentActivity;
import com.chatwingsdk.activities.CommunicationActivity;
import com.chatwingsdk.contentproviders.ChatWingContentProvider;
import com.chatwingsdk.events.internal.SyncCommunicationBoxEvent;
import com.chatwingsdk.events.internal.UpdateUserEvent;
import com.chatwingsdk.events.internal.ViewProfileEvent;
import com.chatwingsdk.fragments.CommunicationMessagesFragment;
import com.chatwingsdk.modules.CommunicationActivityModule;
import com.chatwingsdk.pojos.Message;
import com.chatwingsdk.pojos.errors.ChatWingError;
import com.chatwingsdk.pojos.jspojos.JSUserResponse;
import com.chatwingsdk.pojos.params.CreateConversationParams;
import com.chatwingsdk.pojos.responses.ChatBoxDetailsResponse;
import com.chatwingsdk.services.SyncCommunicationBoxesIntentService;
import com.chatwingsdk.utils.LogUtils;
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
        ExtendCommunicationModeManager.Delegate {

    public static final String AVATAR_PICKER_DIALOG_FRAGMENT_TAG = "AvatarPickerDialogFragment";
    public static final String BLOCK_USER_DIALOG_FRAGMENT_TAG = "BlockUserDialogFragment";
    public static final String ACCOUNT_DIALOG_FRAGMENT_TAG = "AccountDialogFragmentTag";

    @Inject
    com.chatwingsdk.managers.ApiManager mApiManager;
    @Inject
    com.chatwingsdk.managers.UserManager mUserManager;
    @Inject
    BuildManager mBuildManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
    public void onAllSyncsCompleted(com.chatwingsdk.events.internal.AllSyncsCompletedEvent
                                            event) {
        super.onAllSyncsCompleted(event);
    }

    @Subscribe
    public void onTouchUserInfoEvent(com.chatwingsdk.events.internal.TouchUserInfoEvent event) {
        JSUserResponse user = event.getUser();
        String loginType = user.getLoginType();
        String loginId = user.getLoginId();
        String userAvatar = user.getUserAvatar();
        String userProfileUrl = mApiManager.getUserProfileUrl(loginType, loginId);

        ViewProfileEvent viewProfileEvent = new ViewProfileEvent(
                userProfileUrl,
                mApiManager.getAvatarUrl(loginType, loginId, userAvatar),
                user.getUserName(),
                loginType,
                loginId,
                mUserManager.getCurrentUser() == null || user.equals(mUserManager.getCurrentUser()));
        if (!viewProfileEvent.isDenyReply()) {
            showConversation(new CreateConversationParams.SimpleUser(viewProfileEvent.getLoginId(), viewProfileEvent.getUserType()));
        }
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
            (com.chatwingsdk.events.faye.ServerConnectionChangedEvent event) {
        super.onServerConnectionChangedEvent(event);
    }

    @com.squareup.otto.Subscribe
    public void onSyncCommunicationBoxEvent
            (com.chatwingsdk.events.internal.SyncCommunicationBoxEvent event) {
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
            (com.chatwingsdk.events.faye.ChannelSubscriptionChangedEvent event) {
        super.onChannelSubscriptionChanged(event);
    }

    @Subscribe
    public void onFayePublished(com.chatwingsdk.events.faye.FayePublishEvent event) {
        super.onFayePublished(event);
    }

    @Subscribe
    public void onMessageReceived(com.chatwingsdk.events.faye.MessageReceivedEvent event) {
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
        if (oldFragment == null) {
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
            getContentResolver().applyBatch(ChatWingContentProvider.AUTHORITY,
                    ChatWingContentProvider.getClearAllDataBatch());
            startSyncingCommunications();
            invalidateOptionsMenu();
        } catch (Exception e) {
            LogUtils.e(e);
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

        getActivity().startService(new Intent(getActivity(), SyncBookmarkIntentService.class));
    }

    @Override
    protected boolean syncingInProcess() {
        return SyncCommunicationBoxesIntentService.isInProgress() || SyncBookmarkIntentService.isInProgress();
    }

    @Override
    protected boolean startSyncingCommunications() {
        boolean result = super.startSyncingCommunications();
        super.mSyncManager.addToQueue(SyncBookmarkIntentService.class);
        super.mSyncManager.addToQueue(DownloadUserDetailIntentService.class);
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
}
