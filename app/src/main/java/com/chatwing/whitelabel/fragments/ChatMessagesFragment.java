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

import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;

import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.events.AppendEmoticonEvent;
import com.chatwing.whitelabel.events.CreateMessageEvent;
import com.chatwing.whitelabel.events.CurrentChatBoxEvent;
import com.chatwing.whitelabel.events.CurrentCommunicationEvent;
import com.chatwing.whitelabel.events.MessageEvent;
import com.chatwing.whitelabel.events.PasswordEnteredEvent;
import com.chatwing.whitelabel.events.PasswordRefusedEvent;
import com.chatwing.whitelabel.events.RequestOpenChatBoxEvent;
import com.chatwing.whitelabel.events.ResumeOpenChatBoxEvent;
import com.chatwing.whitelabel.managers.ApiManager;
import com.chatwing.whitelabel.managers.CurrentChatBoxManager;
import com.chatwing.whitelabel.managers.PasswordManager;
import com.chatwing.whitelabel.pojos.ChatBox;
import com.chatwing.whitelabel.pojos.Message;
import com.chatwing.whitelabel.pojos.User;
import com.chatwing.whitelabel.services.AckChatboxIntentService;
import com.chatwing.whitelabel.utils.LogUtils;
import com.chatwing.whitelabel.utils.StringUtils;
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
        if (status == CurrentChatBoxEvent.Status.REMOVED) {
            LogUtils.v("Debug messgae not shown REMOVED");
            mWebview.loadUrl("");
            //TODO notify GetMessagesIntentService to stop loading
        } else if (CurrentChatBoxEvent.Status.LOADING.equals(status)) {

        } else if (CurrentChatBoxEvent.Status.LOADED.equals(status)) {
            LogUtils.v("Debug messgae not shown LOADED");
            super.handleComposeView(event.getChatbox());

            String chatboxUrl = mApiManager.getChatboxUrl(mUserManager.getCurrentUser(),
                    event.getUrl());
            LogUtils.v(mWebview + " Request chatbox url " + chatboxUrl);
            mWebview.loadUrl(chatboxUrl);
            loadEmoticons(event.getChatbox().getEmoticons());
        } else if (CurrentChatBoxEvent.Status.UPDATED.equals(status)) {
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
        return true;
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

    private void onRequestOpenChatBox() {
        if (!mRequestingChatBox.hasPassword()) {
            mBus.post(new ResumeOpenChatBoxEvent(mRequestingChatBox));
            return;
        }
        hide();
        mDelegate.showPasswordDialogFragment();
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
}
