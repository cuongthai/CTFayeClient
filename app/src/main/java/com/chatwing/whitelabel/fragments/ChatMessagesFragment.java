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

package com.chatwing.whitelabel.fragments;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.Loader;
import android.text.TextUtils;

import com.chatwing.whitelabel.Constants;
import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.events.AppendEmoticonEvent;
import com.chatwing.whitelabel.events.CreateMessageEvent;
import com.chatwing.whitelabel.events.CurrentChatBoxEvent;
import com.chatwing.whitelabel.events.CurrentCommunicationEvent;
import com.chatwing.whitelabel.events.EditChatMessageEvent;
import com.chatwing.whitelabel.events.FlagMessageEvent;
import com.chatwing.whitelabel.events.GotMessagesEvent;
import com.chatwing.whitelabel.events.IgnoreUserEvent;
import com.chatwing.whitelabel.events.MessageEvent;
import com.chatwing.whitelabel.events.PasswordEnteredEvent;
import com.chatwing.whitelabel.events.PasswordRefusedEvent;
import com.chatwing.whitelabel.events.RequestBlockEvent;
import com.chatwing.whitelabel.events.RequestBlockIPEvent;
import com.chatwing.whitelabel.events.RequestBlockTypeEvent;
import com.chatwing.whitelabel.events.RequestOpenChatBoxEvent;
import com.chatwing.whitelabel.events.ResumeOpenChatBoxEvent;
import com.chatwing.whitelabel.events.ViewProfileEvent;
import com.chatwing.whitelabel.events.faye.ServerConnectionChangedEvent;
import com.chatwing.whitelabel.loaders.CommunicationBoxMessagesLoader;
import com.chatwing.whitelabel.managers.ApiManager;
import com.chatwing.whitelabel.managers.CurrentChatBoxManager;
import com.chatwing.whitelabel.managers.PasswordManager;
import com.chatwing.whitelabel.managers.UserManager;
import com.chatwing.whitelabel.pojos.BaseUser;
import com.chatwing.whitelabel.pojos.ChatBox;
import com.chatwing.whitelabel.pojos.Message;
import com.chatwing.whitelabel.pojos.User;
import com.chatwing.whitelabel.pojos.params.CreateConversationParams;
import com.chatwing.whitelabel.services.AckChatboxIntentService;
import com.chatwing.whitelabel.services.BlockUserIntentService;
import com.chatwing.whitelabel.services.DeleteMessageIntentService;
import com.chatwing.whitelabel.services.FlagMessageIntentService;
import com.chatwing.whitelabel.services.GetMessagesIntentService;
import com.chatwing.whitelabel.services.IgnoreUserIntentService;
import com.chatwing.whitelabel.utils.LogUtils;
import com.chatwing.whitelabel.utils.StatisticTracker;
import com.chatwing.whitelabel.utils.StringUtils;
import com.chatwing.whitelabel.validators.PermissionsValidator;
import com.chatwing.whitelabel.views.ErrorMessageView;
import com.chatwing.whitelabel.views.QuickMessageView;
import com.cocosw.bottomsheet.BottomSheet;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

/**
 * Created by cuongthai on 17/08/2014.
 */
public class ChatMessagesFragment extends CommunicationMessagesFragment {
    private static final long ACK_TIMING_THRESHOLD = 30 * 1000; //30s

    public enum BLOCK {
        IP,
        ACCOUNT_TYPE
    }

    @Inject
    protected UserManager mUserManager;
    @Inject
    protected ErrorMessageView mErrorMessageView;
    @Inject
    protected QuickMessageView mMessageView;
    @Inject
    protected CurrentChatBoxManager mCurrentChatBoxManager;
    @Inject
    protected ApiManager mApiManager;
    @Inject
    protected PasswordManager mPasswordManager;

    private Delegate mDelegate;
    private ChatBox mRequestingChatBox;
    private long mLastReceiveMessageTime;

    public static CommunicationMessagesFragment newInstance() {
        return new ChatMessagesFragment();
    }

    @Override
    protected Message constructMessage(User user, String content,
                                       long createdDate, String randomKey,
                                       Message.Status status) {
        return new Message(
                user,
                mCurrentChatBoxManager.getCurrentChatBox().getId(),
                content,
                createdDate,
                randomKey,
                status);
    }

    @Override
    protected CommunicationBoxMessagesLoaderCallbacks constructLoaderCallbacks() {
        return new ChatBoxMessagesLoaderCallbacks();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mDelegate = (Delegate) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mDelegate = null;
    }

    @Override
    public void onPause() {
        super.onPause();
        ChatBox chatBox = mCurrentChatBoxManager.getCurrentChatBox();
        if (chatBox != null) {
            Integer id = chatBox.getId();
            AckChatboxIntentService.ack(getActivity(), id);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        StatisticTracker.stopChatBoxEvent();
    }

    @Override
    protected boolean hasCurrentCommunication() {
        return mCurrentChatBoxManager.getCurrentChatBox() != null;
    }

    @Override
    @Subscribe
    public void onAppendEmoticonEvent(AppendEmoticonEvent event) {
        super.onAppendEmoticonEvent(event);
    }

    @Subscribe
    public void onCurrentChatBoxChanged(CurrentChatBoxEvent event) {
        LogUtils.v("onCurrentChatBoxChanged");

        CurrentCommunicationEvent.Status status = event.getStatus();
        LogUtils.v("Loading message onCurrentChatBoxChanged " + status);

        if (status == CurrentChatBoxEvent.Status.REMOVED) {
            mIsNoMoreMessages = false;
            LogUtils.v("Debug messgae not shown REMOVED");
            //TODO notify GetMessagesIntentService to stop loading
            mAdapter.clear();
            getLoaderManager().destroyLoader(0);
        } else if (CurrentChatBoxEvent.Status.LOADING.equals(status)) {

        } else if (CurrentChatBoxEvent.Status.LOADED.equals(status)) {
            super.handleComposeView(event.getChatbox());
            loadMessagesFromDb();
            updateCommunicationBoxDetail();

            loadEmoticons(event.getChatbox().getEmoticons());
            StatisticTracker.startChatBoxEvent(event.getChatbox());
        } else if (CurrentChatBoxEvent.Status.UPDATED.equals(status)) {
            updateCommunicationBoxDetail();
        }
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

    @Override
    @Subscribe
    public void onCreateMessageEvent(CreateMessageEvent event) {
        super.onCreateMessageEvent(event);
    }

    @Override
    public boolean addNewMessage(Message newMessage) {
        if (newMessage.getStatus() == Message.Status.PUBLISHED) {
            //Ack if necessary
            if (mLastReceiveMessageTime == 0
                    || (System.currentTimeMillis() - mLastReceiveMessageTime > ACK_TIMING_THRESHOLD)) {
                AckChatboxIntentService.ack(getActivity(), newMessage.getChatBoxId());
            }
            mLastReceiveMessageTime = System.currentTimeMillis();
        }
        return super.addNewMessage(newMessage);
    }

    @Override
    protected void loadMessagesFromServer(boolean forceLoadLatest) {
        ChatBox chatBox = mCurrentChatBoxManager.getCurrentChatBox();
        if (chatBox == null
                || mAdapter == null
                || (mIsNoMoreMessages && !forceLoadLatest)) {
            // Chat box and user are required.
            // So if they are unavailable by now, don't load.
            // Also, don't load if there adapter is not ready or there is no more messages
            //If forceLoadLatest, it means not load from tail. Will trigger full reload
            return;
        }

        Intent intent = new Intent(getActivity(), GetMessagesIntentService.class);
        intent.putExtra(GetMessagesIntentService.EXTRA_CHAT_BOX_ID, chatBox.getId());
        intent.putExtra(GetMessagesIntentService.EXTRA_OLDEST_MESSAGE, mAdapter.getOldestMessageItem());
        intent.putExtra(GetMessagesIntentService.EXTRA_MORE, !forceLoadLatest);
        getActivity().startService(intent);
    }

    @Override
    public void deleteMessage(Message message) {
        ChatBox currentChatBox = mCurrentChatBoxManager.getCurrentChatBox();
        if (currentChatBox == null) {
            return;
        }
        mAdapter.removeByMessageId(message.getId());
    }

    @Override
    public void deleteMessageByIp(Message message) {
        ChatBox currentChatBox = mCurrentChatBoxManager.getCurrentChatBox();
        if (currentChatBox == null) {
            return;
        }
        mAdapter.removeByIp(message.getIp());
    }

    @Override
    public void deleteMessageBySocialAccount(Message message) {
        ChatBox currentChatBox = mCurrentChatBoxManager.getCurrentChatBox();
        if (currentChatBox == null) {
            return;
        }
        mAdapter.removeBySocialAccount(message.getUserType());
    }

    @Override
    protected boolean canHandle(MessageEvent event) {
        // We ignore conversation message type, only process chat
        return !event.isPrivate();
    }

    @Override
    protected boolean isInCurrentCommunicationBox(MessageEvent event) {
        ChatBox currentChatBox = mCurrentChatBoxManager.getCurrentChatBox();
        return currentChatBox != null && currentChatBox.getId() == event.getChatBoxId();
    }

    @Subscribe
    public void onIgnoreUserUpdate(IgnoreUserEvent event) {
        mAdapter.notifyDataSetChanged();
    }

    @Subscribe
    public void onBlockIpEvent(RequestBlockIPEvent event) {
        doBlockUser(BLOCK.IP, event);
    }

    @Subscribe
    public void onBlockTypeEvent(RequestBlockTypeEvent event) {
        doBlockUser(BLOCK.ACCOUNT_TYPE, event);
    }

    @Subscribe
    public void onFlagMessageEvent(FlagMessageEvent event) {
        if (event.getException() == null) {
            mMessageView.show(R.string.message_flag);
        } else {
            mErrorMessageView.show(R.string.error_failed_to_flag);
        }
    }

    @Subscribe
    public void onRequestOpenChatBoxEvent(final RequestOpenChatBoxEvent event) {
        mRequestingChatBox = event.getChatBox();
        onRequestOpenChatBox();
    }

    @Override
    @Subscribe
    public void onGotMessagesEvent(GotMessagesEvent event) {
        super.onGotMessagesEvent(event);
    }

    @Subscribe
    public void onServerConnectionChangedEvent(ServerConnectionChangedEvent event) {
        //Faye
        if (event.getStatus() == ServerConnectionChangedEvent.Status.CONNECTED) {
            loadMessagesFromServer(true);
        }
    }

    @Override
    protected void updateCommunicationBoxDetail() {
        ChatBox chatBox = mCurrentChatBoxManager.getCurrentChatBox();
        if (chatBox == null) {
            return;
        }
        doUpdateCommunicationBoxDetail(chatBox.getJson(), chatBox.getEmoticonsAsMap(), hasAdminPermissions());
    }

    @Subscribe
    public void onPasswordEntered(PasswordEnteredEvent event) {
        String password = event.getPassword();
        String actualPassword = mRequestingChatBox.getChatboxPassword();

        if (TextUtils.isEmpty(password)
                || !StringUtils.getMd5Hash(password).equals(actualPassword)) {
            mErrorMessageView.show(R.string.error_invalid_password);
            // Repeat
            onRequestOpenChatBox();
            return;
        }

        if (event.shouldRemember()) {
            mPasswordManager.rememberChatBoxPassword(mRequestingChatBox.getKey());
        }

        mBus.post(new ResumeOpenChatBoxEvent(mRequestingChatBox));
        show();
        mRequestingChatBox = null;
    }

    @Subscribe
    public void onPasswordRefused(PasswordRefusedEvent event) {
        mCurrentChatBoxManager.removeCurrentChatBox();
        show();
        mRequestingChatBox = null;
    }

    private void hide() {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.hide(this);
        fragmentTransaction.commit();
    }

    private void show() {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.show(this);
        fragmentTransaction.commit();
    }

    public class ChatBoxMessagesLoaderCallbacks extends CommunicationBoxMessagesLoaderCallbacks {
        @Override
        public Loader<CommunicationBoxMessagesLoader.Result> onCreateLoader(int id, Bundle args) {
            LogUtils.v("ChatBoxMessagesLoaderCallbacks create");
            return new CommunicationBoxMessagesLoader(
                    getActivity(),
                    mCurrentChatBoxManager.getCurrentChatBox().getId());
        }
    }

    private boolean hasAdminPermissions() {
        return hasPermission(PermissionsValidator.Permission.DELETE_MESSAGE)
                || hasPermission(PermissionsValidator.Permission.VIEW_MESSAGE_IP);
    }

    private boolean hasPermission(PermissionsValidator.Permission permission) {
        return mUserManager.userHasPermission(mCurrentChatBoxManager.getCurrentChatBox(), permission);
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

    private void onRequestOpenChatBox() {
        if (!mRequestingChatBox.hasPassword()) {
            mBus.post(new ResumeOpenChatBoxEvent(mRequestingChatBox));
            return;
        }
        hide();
        mDelegate.showPasswordDialogFragment();
    }
}
