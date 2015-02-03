package com.chatwing.whitelabel.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;

import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.events.BlockedEvent;
import com.chatwing.whitelabel.fragments.BlockUserDialogFragment;
import com.chatwing.whitelabel.fragments.ExtendChatMessagesFragment;
import com.chatwing.whitelabel.fragments.ExtendCommunicationDrawerFragment;
import com.chatwing.whitelabel.fragments.OnlineUsersFragment;
import com.chatwing.whitelabel.fragments.PhotoPickerDialogFragment;
import com.chatwing.whitelabel.managers.ApiManager;
import com.chatwing.whitelabel.managers.ExtendChatBoxModeManager;
import com.chatwing.whitelabel.modules.ExtendCommunicationActivityModule;
import com.chatwing.whitelabel.services.UpdateAvatarIntentService;
import com.chatwingsdk.activities.BaseABFragmentActivity;
import com.chatwingsdk.activities.CommunicationActivity;
import com.chatwingsdk.events.internal.ViewProfileEvent;
import com.chatwingsdk.fragments.ChatMessagesFragment;
import com.chatwingsdk.fragments.CommunicationMessagesFragment;
import com.chatwingsdk.modules.CommunicationActivityModule;
import com.chatwingsdk.pojos.Message;
import com.chatwingsdk.pojos.jspojos.UserResponse;
import com.chatwingsdk.pojos.params.CreateConversationParams;
import com.chatwingsdk.services.CreateConversationIntentService;
import com.chatwingsdk.utils.LogUtils;
import com.soundcloud.android.crop.Crop;
import com.squareup.otto.Bus;
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
        ExtendChatMessagesFragment.Delegate {

    public static final String AVATAR_PICKER_DIALOG_FRAGMENT_TAG = "AvatarPickerDialogFragment";
    public static final String BLOCK_USER_DIALOG_FRAGMENT_TAG = "BlockUserDialogFragment";

    @Inject
    com.chatwingsdk.managers.ApiManager mApiManager;
    @Inject
    com.chatwingsdk.managers.UserManager mUserManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String onlineFragmentTag = getString(R.string.fragment_tag_online_user);
        if (getSupportFragmentManager().findFragmentByTag(onlineFragmentTag) == null) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.right_drawer_container, new OnlineUsersFragment(), onlineFragmentTag);
            fragmentTransaction.commit();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == Crop.REQUEST_CROP && resultCode == RESULT_OK) {
            Uri output = Crop.getOutput(intent);
            startUpdateAvatar(output.getPath());
        } else if (requestCode == REQUEST_CODE_AUTHENTICATION && resultCode == RESULT_OK) {
            //Populate fake mods
//            CreateConversationParams.SimpleUser simpleUser = new CreateConversationParams.SimpleUser("238405", "chatwing");
//            Intent service = new Intent(this, CreateConversationIntentService.class);
//            service.putExtra(CreateConversationIntentService.SILENT, true);
//            service.putExtra(CreateConversationIntentService.EXTRA_USER, simpleUser);
//            startService(service);
        }
    }

    @Override
    protected Class<? extends BaseABFragmentActivity> getEntranceActivityClass() {
        return StartActivity.class;
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
        UserResponse user = event.getUser();
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

    @com.squareup.otto.Subscribe
    public void onServerConnectionChangedEvent
            (com.chatwingsdk.events.faye.ServerConnectionChangedEvent event) {
        super.onServerConnectionChangedEvent(event);
    }

    @com.squareup.otto.Subscribe
    public void onSyncCommunicationBoxEvent
            (com.chatwingsdk.events.internal.SyncCommunicationBoxEvent event) {
        super.onSyncCommunicationBoxEvent(event);
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
    public void showSettings() {
        Intent i = new Intent(this, MainPreferenceActivity.class);
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
}
