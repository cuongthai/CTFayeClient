package com.chatwing.whitelabel.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.events.FlagMessageEvent;
import com.chatwing.whitelabel.events.MessageEditEvent;
import com.chatwing.whitelabel.events.RequestBlockEvent;
import com.chatwing.whitelabel.events.RequestBlockIPEvent;
import com.chatwing.whitelabel.events.RequestBlockTypeEvent;
import com.chatwing.whitelabel.services.BlockUserIntentService;
import com.chatwing.whitelabel.services.DeleteMessageIntentService;
import com.chatwing.whitelabel.services.FlagMessageIntentService;
import com.chatwing.whitelabel.services.IgnoreUserIntentService;
import com.chatwingsdk.utils.StatisticTracker;
import com.chatwingsdk.events.internal.CurrentChatBoxEvent;
import com.chatwingsdk.events.internal.PasswordEnteredEvent;
import com.chatwingsdk.events.internal.PasswordRefusedEvent;
import com.chatwingsdk.events.internal.RequestOpenChatBoxEvent;
import com.chatwingsdk.fragments.ChatMessagesFragment;
import com.chatwingsdk.fragments.CommunicationMessagesFragment;
import com.chatwingsdk.managers.CurrentChatBoxManager;
import com.chatwingsdk.managers.UserManager;
import com.chatwingsdk.pojos.BaseUser;
import com.chatwingsdk.pojos.Message;
import com.chatwingsdk.validators.PermissionsValidator;
import com.chatwingsdk.views.ErrorMessageView;
import com.chatwingsdk.views.QuickMessageView;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

/**
 * Created by steve on 31/12/2014.
 */
public class ExtendChatMessagesFragment extends ChatMessagesFragment {
    @Inject
    CurrentChatBoxManager mCurrentChatBoxManager;
    @Inject
    UserManager mUserManager;
    @Inject
    ErrorMessageView mErrorMessageView;
    @Inject
    QuickMessageView mMessageView;
    private Message previousSelectedMessage;
    private Delegate mDelegate;

    public enum BLOCK {
        IP,
        TYPE
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
        registerForContextMenu(mWebview);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (previousSelectedMessage == null) return;
        boolean showIgnoreButton = mUserManager.getCurrentUser() == null
                ? false
                : shouldShowIgnoreButton(previousSelectedMessage.getUserId(), previousSelectedMessage.getUserType());
        boolean showFlagButton = mUserManager.getCurrentUser() == null
                ? false
                : shouldShowFlagButton(previousSelectedMessage.getUserId(), previousSelectedMessage.getUserType());
        if (!(hasAdminPermissions() || showIgnoreButton || showFlagButton)) {
            return;
        }
        MenuInflater menuInflater = getActivity().getMenuInflater();
        menuInflater.inflate(R.menu.context_menu_chat_message, menu);

        boolean canDeleteMessage = hasPermission(PermissionsValidator.Permission.DELETE_MESSAGE);
        boolean canBlockUser = hasPermission(PermissionsValidator.Permission.BLOCK_USER);

        // Only show options that the user can do.
        MenuItem blockItem = menu.findItem(R.id.block);
        MenuItem deleteItem = menu.findItem(R.id.delete);

        deleteItem.setVisible(canDeleteMessage);
        blockItem.setVisible(canBlockUser);

        //Ignore button
        MenuItem ignoreItem = menu.findItem(R.id.ignore);
        ignoreItem.setVisible(showIgnoreButton);
        if (mUserManager.hasIgnored(previousSelectedMessage.getUserId(), previousSelectedMessage.getUserType())) {
            ignoreItem.setTitle(R.string.title_unignore);
        } else {
            ignoreItem.setTitle(R.string.title_ignore);
        }

        //Ignore button
        MenuItem flagButton = menu.findItem(R.id.flag);
        flagButton.setVisible(showFlagButton);
    }

    @Override
    public void onContextMenuClosed(Menu menu) {
        super.onContextMenuClosed(menu);
        previousSelectedMessage = null;
    }

    @Subscribe
    public void onMessageEditEvent(MessageEditEvent event) {
        final Message[] messages = event.getMessages();
        if (messages.length == 1) {
            previousSelectedMessage = messages[0];
            mWebview.showContextMenu();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getString(R.string.title_dialog_select_message))
                    .setItems(getMessagesArray(messages), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int position) {
                            previousSelectedMessage = messages[position];
                            mWebview.showContextMenu();
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    private CharSequence[] getMessagesArray(Message[] messages) {
        CharSequence[] messageContents = new CharSequence[messages.length];
        for (int i = 0; i < messages.length; i++) {
            messageContents[i] = messages[i].getContent();
        }
        return messageContents;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete:
                deleteMessage(previousSelectedMessage);
                return true;
            case R.id.block:
                blockUser(previousSelectedMessage);
                return true;
            case R.id.ignore:
                ignoreUser(previousSelectedMessage);
                return true;
            case R.id.flag:
                flagMessage(previousSelectedMessage);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void flagMessage(Message message) {
        if (message == null) return;
        Intent intent = new Intent(getActivity(), FlagMessageIntentService.class);
        intent.putExtra(FlagMessageIntentService.EXTRA_MESSAGE_ID, message.getId());
        getActivity().startService(intent);
    }

    private void ignoreUser(Message message) {
        if (message == null) return;
        Intent intent = new Intent(getActivity(), IgnoreUserIntentService.class);
        intent.putExtra(IgnoreUserIntentService.EXTRA_USER_ID, message.getUserId());
        intent.putExtra(IgnoreUserIntentService.EXTRA_USER_TYPE, message.getUserType());
        intent.putExtra(IgnoreUserIntentService.EXTRA_IGNORED, mUserManager.hasIgnored(message.getUserId(), message.getUserType()));
        getActivity().startService(intent);
    }

    private void deleteMessage(Message message) {
        if (message == null) return;
        // Double check for permission.
        // If user doesn't have the permission, the UI element shouldn't be showed anyway.
        if (!hasPermission(PermissionsValidator.Permission.DELETE_MESSAGE)) {
            mErrorMessageView.show(R.string.error_delete_message_no_permission);
            return;
        }

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
    public void onAppendEmoticonEvent(com.chatwingsdk.events.internal.AppendEmoticonEvent event) {
        super.onAppendEmoticonEvent(event);
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
    public void onCurrentChatBoxChanged(com.chatwingsdk.events.internal.CurrentChatBoxEvent event) {
        CurrentChatBoxEvent.Status status = event.getStatus();
        super.onCurrentChatBoxChanged(event);
        if (status == CurrentChatBoxEvent.Status.LOADED) {
            StatisticTracker.startChatBoxEvent(event.getChatbox());
        }
    }

    @Subscribe
    public void onBlockIpEvent(RequestBlockIPEvent event) {
        doBlockUser(BLOCK.IP, event);
    }

    @Subscribe
    public void onBlockTypeEvent(RequestBlockTypeEvent event) {
        doBlockUser(BLOCK.TYPE, event);
    }

    @com.squareup.otto.Subscribe
    public void onCreateMessageEvent(com.chatwingsdk.events.internal.CreateMessageEvent event) {
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
        intent.putExtra(BlockUserIntentService.EXTRA_BLOCK, block);
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
    }

}
