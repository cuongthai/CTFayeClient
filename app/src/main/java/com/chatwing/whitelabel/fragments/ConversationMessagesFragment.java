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

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.contentproviders.ChatWingContentProvider;
import com.chatwing.whitelabel.events.AppendEmoticonEvent;
import com.chatwing.whitelabel.events.CreateConversationEvent;
import com.chatwing.whitelabel.events.CreateMessageEvent;
import com.chatwing.whitelabel.events.CurrentCommunicationEvent;
import com.chatwing.whitelabel.events.CurrentConversationEvent;
import com.chatwing.whitelabel.events.MessageEvent;
import com.chatwing.whitelabel.events.UserUnauthenticatedEvent;
import com.chatwing.whitelabel.managers.ApiManager;
import com.chatwing.whitelabel.managers.CurrentConversationManager;
import com.chatwing.whitelabel.pojos.Conversation;
import com.chatwing.whitelabel.pojos.Message;
import com.chatwing.whitelabel.pojos.User;
import com.chatwing.whitelabel.services.AckConversationIntentService;
import com.chatwing.whitelabel.tables.ConversationTable;
import com.chatwing.whitelabel.utils.LogUtils;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

/**
 * Created by cuongthai on 18/08/2014.
 */
public class ConversationMessagesFragment extends CommunicationMessagesFragment {
    @Inject
    CurrentConversationManager mCurrentConversationManager;
    @Inject
    ApiManager mApiManager;
    private static final long ACK_TIMING_THRESHOLD = 30 * 1000; //30s
    private long mLastReceiveMessageTime;

    public static CommunicationMessagesFragment newInstance() {
        return new ConversationMessagesFragment();
    }

    @Override
    public void onPause() {
        super.onPause();
        Conversation conversation = mCurrentConversationManager.getCurrentConversation();
        if (conversation != null) {
            String id = conversation.getId();
            AckConversationIntentService.ack(getActivity(), id);
        }
    }

    @Override
    protected Message constructMessage(User user, String content,
                                       long createdDate, String randomKey,
                                       Message.Status status) {
        return new Message(
                user,
                mCurrentConversationManager.getCurrentConversation().getId(),
                content,
                createdDate,
                randomKey,
                status);
    }

    @Override
    public boolean addNewMessage(Message newMessage) {
        if (newMessage.getStatus() == Message.Status.PUBLISHED) {
            //Ack if necessary
            if (mLastReceiveMessageTime == 0 || (System.currentTimeMillis() - mLastReceiveMessageTime > ACK_TIMING_THRESHOLD)) {
                AckConversationIntentService.ack(getActivity(), newMessage.getConversationID());
            }
            mLastReceiveMessageTime = System.currentTimeMillis();
        }
        return true;
    }

    @Override
    protected boolean hasCurrentCommunication() {
        return mCurrentConversationManager.getCurrentConversation() != null;
    }

    @Override
    @Subscribe
    public void onAppendEmoticonEvent(AppendEmoticonEvent event) {
        super.onAppendEmoticonEvent(event);
    }

    @Subscribe
    public void onCurrentConversationChanged(CurrentConversationEvent event) {
        CurrentCommunicationEvent.Status status = event.getStatus();
        if (CurrentConversationEvent.Status.REMOVED.equals(status)) {
            mWebview.loadUrl("");
        } else if (CurrentConversationEvent.Status.LOADING.equals(status)) {
        } else if (CurrentConversationEvent.Status.LOADED.equals(status)) {
            try {
                String conversationUrl = mApiManager.getConversationUrl(mUserManager.getCurrentUser(),
                        event.getUrl());
                LogUtils.v("Request conversationUrl url " + conversationUrl);

                mWebview.loadUrl(conversationUrl);
                loadEmoticons(event.getConversation().getEmoticons());
            } catch (ApiManager.UserUnauthenticatedException e) {
                e.printStackTrace();
            }
        }
    }

    @Subscribe
    public void onCreateConversationEvent(CreateConversationEvent event) {
        if (event.getException() != null) {
            if (event.getException() instanceof ApiManager.UserUnauthenticatedException) {
                mBus.post(new UserUnauthenticatedEvent());
                return;
            }
            if (!handleException(event.getException())) {
                mErrorMessageView.show(R.string.error_while_creating_conversation);
            }
            return;
        }

        onCreateConversationResult(event.getResponse().getData());
    }

    @Override
    @Subscribe
    public void onCreateMessageEvent(CreateMessageEvent event) {
        super.onCreateMessageEvent(event);
    }

    private void onCreateConversationResult(Conversation conversation) {
        if (conversation == null) {
            return;
        }
        //Create conversation if not existed
        ContentValues conversationContentValues = ConversationTable.getContentValues(conversation);
        Cursor cursor = null;
        try {
            cursor = getActivity().getContentResolver().query(
                    ChatWingContentProvider.getConversationWithIdUri(conversation.getId()),
                    new String[]{ConversationTable.CONVERSATION_ID},
                    null,
                    null,
                    null);
            if (cursor.getCount() == 0) {
                Uri insert = getActivity().getContentResolver().insert(ChatWingContentProvider.getConversationsUri(),
                        conversationContentValues);
                if ("-1".equals(insert.getLastPathSegment())) {
                    LogUtils.v("insert conversation failed ");
                    return;
                }
            }

            mCurrentConversationManager.loadConversation(conversation.getId());
        }finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    @Override
    protected boolean canHandle(MessageEvent event) {
        return event.isPrivate();
    }

    @Override
    protected boolean isInCurrentCommunicationBox(MessageEvent event) {
        Conversation currentConversation = mCurrentConversationManager.getCurrentConversation();
        return currentConversation != null && currentConversation.getId().equals(event.getConversationId());
    }
}
