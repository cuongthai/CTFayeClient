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

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.Loader;
import android.text.TextUtils;

import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.events.AppendEmoticonEvent;
import com.chatwing.whitelabel.events.CreateMessageEvent;
import com.chatwing.whitelabel.events.CurrentChatBoxEvent;
import com.chatwing.whitelabel.events.CurrentCommunicationEvent;
import com.chatwing.whitelabel.events.GotMoreMessagesEvent;
import com.chatwing.whitelabel.events.MessageEvent;
import com.chatwing.whitelabel.events.PasswordEnteredEvent;
import com.chatwing.whitelabel.events.PasswordRefusedEvent;
import com.chatwing.whitelabel.events.RequestOpenChatBoxEvent;
import com.chatwing.whitelabel.events.ResumeOpenChatBoxEvent;
import com.chatwing.whitelabel.loaders.CommunicationBoxMessagesLoader;
import com.chatwing.whitelabel.managers.ApiManager;
import com.chatwing.whitelabel.managers.CurrentChatBoxManager;
import com.chatwing.whitelabel.managers.PasswordManager;
import com.chatwing.whitelabel.pojos.ChatBox;
import com.chatwing.whitelabel.pojos.Message;
import com.chatwing.whitelabel.pojos.User;
import com.chatwing.whitelabel.services.AckChatboxIntentService;
import com.chatwing.whitelabel.services.GetMessagesIntentService;
import com.chatwing.whitelabel.utils.LogUtils;
import com.chatwing.whitelabel.utils.StringUtils;
import com.chatwing.whitelabel.validators.PermissionsValidator;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

/**
 * Created by cuongthai on 17/08/2014.
 */
public class ChatMessagesFragment extends CommunicationMessagesFragment {
    @Inject
    CurrentChatBoxManager mCurrentChatBoxManager;
    @Inject
    ApiManager mApiManager;
    @Inject
    PasswordManager mPasswordManager;

    private ChatBox mRequestingChatBox;
    private static final long ACK_TIMING_THRESHOLD = 30 * 1000; //30s
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
    public void onPause() {
        super.onPause();
        ChatBox chatBox = mCurrentChatBoxManager.getCurrentChatBox();
        if (chatBox != null) {
            Integer id = chatBox.getId();
            AckChatboxIntentService.ack(getActivity(), id);
        }
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
            LogUtils.v("Debug messgae not shown LOADED");
            super.handleComposeView(event.getChatbox());
            loadMessagesFromDb();
            updateCommunicationBoxDetail();

            loadEmoticons(event.getChatbox().getEmoticons());
        } else if (CurrentChatBoxEvent.Status.UPDATED.equals(status)) {
            updateCommunicationBoxDetail();
        }
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
            if (mLastReceiveMessageTime == 0 || (System.currentTimeMillis() - mLastReceiveMessageTime > ACK_TIMING_THRESHOLD)) {
                AckChatboxIntentService.ack(getActivity(), newMessage.getChatBoxId());
            }
            mLastReceiveMessageTime = System.currentTimeMillis();
        }
        return super.addNewMessage(newMessage);
    }

    @Override
    protected void loadMessagesFromServer() {
        ChatBox chatBox = mCurrentChatBoxManager.getCurrentChatBox();
        if (chatBox == null
                || mAdapter == null || mIsNoMoreMessages) {
            // Chat box and user are required.
            // So if they are unavailable by now, don't load.
            // Also, don't load if there adapter is not ready or there is no more messages.
            return;
        }

        Intent intent = new Intent(getActivity(), GetMessagesIntentService.class);
        intent.putExtra(GetMessagesIntentService.EXTRA_CHAT_BOX_ID, chatBox.getId());
        intent.putExtra(GetMessagesIntentService.EXTRA_OLDEST_MESSAGE, mAdapter.getOldestMessageItem());
        getActivity().startService(intent);
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
    public void onRequestOpenChatBoxEvent(final RequestOpenChatBoxEvent event) {
        mRequestingChatBox = event.getChatBox();
        onRequestOpenChatBox();
    }

    @Override
    @Subscribe
    public void onGotMoreMessagesEvent(GotMoreMessagesEvent event) {
        super.onGotMoreMessagesEvent(event);
    }

    private void onRequestOpenChatBox() {
        if (!mRequestingChatBox.hasPassword()) {
            mBus.post(new ResumeOpenChatBoxEvent(mRequestingChatBox));
            return;
        }
        hide();
        mDelegate.showPasswordDialogFragment();
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
}
