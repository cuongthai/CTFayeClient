package com.chatwing.whitelabel.fragments;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.chatwing.whitelabel.Constants;
import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.events.AppendEmoticonEvent;
import com.chatwing.whitelabel.events.CreateMessageEvent;
import com.chatwing.whitelabel.events.CurrentChatBoxEvent;
import com.chatwing.whitelabel.events.EditChatMessageEvent;
import com.chatwing.whitelabel.events.FlagMessageEvent;
import com.chatwing.whitelabel.events.GotMoreMessagesEvent;
import com.chatwing.whitelabel.events.IgnoreUserEvent;
import com.chatwing.whitelabel.events.PasswordEnteredEvent;
import com.chatwing.whitelabel.events.PasswordRefusedEvent;
import com.chatwing.whitelabel.events.RequestBlockEvent;
import com.chatwing.whitelabel.events.RequestBlockIPEvent;
import com.chatwing.whitelabel.events.RequestBlockTypeEvent;
import com.chatwing.whitelabel.events.RequestOpenChatBoxEvent;
import com.chatwing.whitelabel.events.ViewProfileEvent;
import com.chatwing.whitelabel.managers.CurrentChatBoxManager;
import com.chatwing.whitelabel.managers.UserManager;
import com.chatwing.whitelabel.pojos.BaseUser;
import com.chatwing.whitelabel.pojos.Message;
import com.chatwing.whitelabel.pojos.params.CreateConversationParams;
import com.chatwing.whitelabel.services.BlockUserIntentService;
import com.chatwing.whitelabel.services.DeleteMessageIntentService;
import com.chatwing.whitelabel.services.FlagMessageIntentService;
import com.chatwing.whitelabel.services.IgnoreUserIntentService;
import com.chatwing.whitelabel.utils.LogUtils;
import com.chatwing.whitelabel.utils.StatisticTracker;
import com.chatwing.whitelabel.validators.PermissionsValidator;
import com.chatwing.whitelabel.views.ErrorMessageView;
import com.chatwing.whitelabel.views.QuickMessageView;
import com.cocosw.bottomsheet.BottomSheet;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

/**
 * Created by steve on 31/12/2014.
 */
public class ExtendChatMessagesFragment extends ChatMessagesFragment {
    @Inject
    protected CurrentChatBoxManager mCurrentChatBoxManager;
    @Inject
    protected UserManager mUserManager;
    @Inject
    protected ErrorMessageView mErrorMessageView;
    @Inject
    protected QuickMessageView mMessageView;
    private Delegate mDelegate;

    public enum BLOCK {
        IP,
        ACCOUNT_TYPE
    }

    public static CommunicationMessagesFragment newInstance() {
        return new ExtendChatMessagesFragment();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mDelegate = (Delegate) activity;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        StatisticTracker.stopChatBoxEvent();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mDelegate = null;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }


    @Subscribe
    public void onEditChatMessageEvent(final EditChatMessageEvent event) {
        LogUtils.v("On long Click " + event.getPosition());
        final Message message = mAdapter.getItem(event.getPosition());
        LogUtils.v("On long Click " + message);
        if (message == null) return;
        new BottomSheet.Builder(getActivity())
                .title(message.getContent())
                .sheet(R.menu.bottom_sheet_message_item)
                .listener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case R.id.delete:
                                doDeleteMessage(message);
                                break;
                            case R.id.block:
                                blockUser(message);
                                break;
                            case R.id.ignore:
                                ignoreUser(message);
                                break;
                            case R.id.flag:
                                flagMessage(message);
                                break;
                            case R.id.pm:
                                pm(message);
                                break;
                            case R.id.copy:
                                copyMessage(message);
                                break;
                        }
                    }
                }).show();
    }

    private void pm(Message message) {
        String loginType = message.getUserType();
        String loginId = message.getUserId();
        String userAvatar = message.getAvatar();
        String username = message.getUserName();

        String userProfileUrl = mApiManager.getUserProfileUrl(loginType, loginId);

        ViewProfileEvent viewProfileEvent = new ViewProfileEvent(
                userProfileUrl,
                mApiManager.getAvatarUrl(loginType, loginId, userAvatar),
                username,
                loginType,
                loginId,
                mUserManager.getCurrentUser() == null
                        || BaseUser.computeIdentifier(loginId, loginType).equals(mUserManager.getCurrentUser().getIdentifier())
                        || Constants.TYPE_GUEST.equals(loginType)); // Prevent chat to yourself, guest
        if (!viewProfileEvent.isDenyReply()) {
            mDelegate.showConversation(new CreateConversationParams.SimpleUser(viewProfileEvent.getLoginId(), viewProfileEvent.getUserType()));
        }
    }

    private void copyMessage(Message message) {
        ClipboardManager clipboard = (ClipboardManager)
                getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("message content", message.getContent());
        clipboard.setPrimaryClip(clip);
    }

    private void flagMessage(Message message) {
        if (!shouldShowFlagButton(message.getUserId(), message.getUserType())) {
            mErrorMessageView.show(R.string.error_flag_message_no_permission);
            return;
        }
        if (message == null) return;
        Intent intent = new Intent(getActivity(), FlagMessageIntentService.class);
        intent.putExtra(FlagMessageIntentService.EXTRA_MESSAGE_ID, message.getId());
        getActivity().startService(intent);
    }

    private void ignoreUser(Message message) {
        if (!shouldShowIgnoreButton(message.getUserId(), message.getUserType())) {
            mErrorMessageView.show(R.string.error_ignore_message_no_permission);
            return;
        }
        if (message == null) return;
        Intent intent = new Intent(getActivity(), IgnoreUserIntentService.class);
        intent.putExtra(IgnoreUserIntentService.EXTRA_USER_ID, message.getUserId());
        intent.putExtra(IgnoreUserIntentService.EXTRA_USER_TYPE, message.getUserType());
        intent.putExtra(IgnoreUserIntentService.EXTRA_REQUEST_IGNORE, !mUserManager.hasIgnored(message.getUserId(), message.getUserType()));
        getActivity().startService(intent);
    }

    private void doDeleteMessage(Message message) {
        boolean canDeleteMessage = hasPermission(PermissionsValidator.Permission.DELETE_MESSAGE);
        if (!canDeleteMessage) {
            mErrorMessageView.show(R.string.error_delete_message_no_permission);
            return;
        }
        if (message == null) return;

        Intent intent = new Intent(getActivity(), DeleteMessageIntentService.class);
        intent.putExtra(DeleteMessageIntentService.EXTRA_CHAT_BOX_ID, message.getChatBoxId());
        intent.putExtra(DeleteMessageIntentService.EXTRA_MESSAGE_ID, message.getId());
        getActivity().startService(intent);
    }

    private void blockUser(Message message) {
        if (message == null) return;

        // Double check for permission.
        // If user doesn't have the permission, the UI element shouldn't be showed anyway.
        if (!hasPermission(PermissionsValidator.Permission.BLOCK_USER)) {
            mErrorMessageView.show(R.string.error_block_user_no_permission);
            return;
        }

        mDelegate.showBlockUserDialogFragment(message);
    }

    @com.squareup.otto.Subscribe
    public void onAppendEmoticonEvent(AppendEmoticonEvent event) {
        super.onAppendEmoticonEvent(event);
    }

    @Subscribe
    public void onIgnoreUserUpdate(IgnoreUserEvent event){
        mAdapter.notifyDataSetChanged();
    }

    @Subscribe
    public void onRequestOpenChatBoxEvent(final RequestOpenChatBoxEvent event) {
        super.onRequestOpenChatBoxEvent(event);
    }

    @Subscribe
    public void onPasswordEntered(PasswordEnteredEvent event) {
        super.onPasswordEntered(event);
    }

    @Subscribe
    public void onPasswordRefused(PasswordRefusedEvent event) {
        super.onPasswordRefused(event);
    }

    @com.squareup.otto.Subscribe
    public void onCurrentChatBoxChanged(CurrentChatBoxEvent event) {
        CurrentChatBoxEvent.Status status = event.getStatus();
        super.onCurrentChatBoxChanged(event);
        if (status == CurrentChatBoxEvent.Status.LOADED) {
            StatisticTracker.startChatBoxEvent(event.getChatbox());
        }
    }

    @Override
    @Subscribe
    public void onGotMoreMessagesEvent(GotMoreMessagesEvent event) {
        super.onGotMoreMessagesEvent(event);
    }

    @Subscribe
    public void onBlockIpEvent(RequestBlockIPEvent event) {
        doBlockUser(BLOCK.IP, event);
    }

    @Subscribe
    public void onBlockTypeEvent(RequestBlockTypeEvent event) {
        doBlockUser(BLOCK.ACCOUNT_TYPE, event);
    }

    @com.squareup.otto.Subscribe
    public void onCreateMessageEvent(CreateMessageEvent event) {
        super.onCreateMessageEvent(event);
    }

    @Subscribe
    public void onFlagMessageEvent(FlagMessageEvent event) {
        if (event.getException() == null) {
            mMessageView.show(R.string.message_flag);
        } else {
            mErrorMessageView.show(R.string.error_failed_to_flag);
        }
    }

    //////////////////////////////////////////////////////////////////
    // ContextMenu and methods related to admin/moderator features.
    //////////////////////////////////////////////////////////////////
    private boolean hasAdminPermissions() {
        return hasPermission(PermissionsValidator.Permission.DELETE_MESSAGE)
                || hasPermission(PermissionsValidator.Permission.VIEW_MESSAGE_IP);
    }

    private boolean hasPermission(PermissionsValidator.Permission permission) {
        return mUserManager.userHasPermission(mCurrentChatBoxManager.getCurrentChatBox(), permission);
    }

    private void doBlockUser(BLOCK block,
                             RequestBlockEvent event) {
        Intent intent = new Intent(getActivity(), BlockUserIntentService.class);
        intent.putExtra(BlockUserIntentService.EXTRA_MESSAGE, event.getMessage());
        intent.putExtra(BlockUserIntentService.EXTRA_BLOCK_TYPE, block);
        intent.putExtra(BlockUserIntentService.EXTRA_CLEAR_MESSAGE, event.isClearMessage());
        intent.putExtra(BlockUserIntentService.EXTRA_REASON, event.getReason());
        intent.putExtra(BlockUserIntentService.EXTRA_DURATION, event.getDuration());
        getActivity().startService(intent);
    }

    private boolean shouldShowIgnoreButton(String loginId, String userType) {
        BaseUser mCurrentUser = mUserManager.getCurrentUser();
        if (mCurrentUser == null) {
            return false;
        }
        boolean isMe = mUserManager.isCurrentUser(
                BaseUser.computeIdentifier(loginId, userType));
        boolean meOrGuest = isMe
                || BaseUser.isGuest(userType)
                || mCurrentUser.isGuest();


        return !meOrGuest;
    }

    private boolean shouldShowFlagButton(String loginId, String userType) {
        BaseUser mCurrentUser = mUserManager.getCurrentUser();
        if (mCurrentUser == null) {
            return false;
        }
        boolean isMe = mUserManager.isCurrentUser(
                BaseUser.computeIdentifier(loginId, userType));
        boolean meOrGuest = isMe
                || BaseUser.isGuest(userType)
                || mCurrentUser.isGuest();


        return !meOrGuest;
    }

    public static interface Delegate extends CommunicationMessagesFragment.Delegate {
        void showBlockUserDialogFragment(Message message);

        void showConversation(CreateConversationParams.SimpleUser simpleUser);
    }

}
