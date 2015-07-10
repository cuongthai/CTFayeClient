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
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.Loader;

import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.contentproviders.ChatWingContentProvider;
import com.chatwing.whitelabel.events.AppendEmoticonEvent;
import com.chatwing.whitelabel.events.CreateConversationEvent;
import com.chatwing.whitelabel.events.CreateMessageEvent;
import com.chatwing.whitelabel.events.CurrentChatBoxEvent;
import com.chatwing.whitelabel.events.CurrentCommunicationEvent;
import com.chatwing.whitelabel.events.CurrentConversationEvent;
import com.chatwing.whitelabel.events.GotMoreMessagesEvent;
import com.chatwing.whitelabel.events.MessageEvent;
import com.chatwing.whitelabel.events.UserUnauthenticatedEvent;
import com.chatwing.whitelabel.loaders.CommunicationBoxMessagesLoader;
import com.chatwing.whitelabel.managers.ApiManager;
import com.chatwing.whitelabel.managers.CurrentConversationManager;
import com.chatwing.whitelabel.pojos.Conversation;
import com.chatwing.whitelabel.pojos.Message;
import com.chatwing.whitelabel.pojos.User;
import com.chatwing.whitelabel.services.AckConversationIntentService;
import com.chatwing.whitelabel.services.GetMessagesIntentService;
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
    protected CommunicationBoxMessagesLoaderCallbacks constructLoaderCallbacks() {
        return new ConversationBoxMessagesLoaderCallbacks();
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
        return super.addNewMessage(newMessage);
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

    @Override
    @Subscribe
    public void onGotMoreMessagesEvent(GotMoreMessagesEvent event) {
        super.onGotMoreMessagesEvent(event);
    }

    @Subscribe
    public void onCurrentConversationChanged(CurrentConversationEvent event) {
        CurrentCommunicationEvent.Status status = event.getStatus();
        if (CurrentConversationEvent.Status.REMOVED.equals(status)) {
            mIsNoMoreMessages = false;
            mAdapter.clear();
            getLoaderManager().destroyLoader(0);
        } else if (CurrentConversationEvent.Status.LOADING.equals(status)) {
        } else if (CurrentConversationEvent.Status.LOADED.equals(status)) {
            loadMessagesFromDb();
            updateCommunicationBoxDetail();

            loadEmoticons(event.getConversation().getEmoticons());
        }else if (CurrentChatBoxEvent.Status.UPDATED.equals(status)) {
            updateCommunicationBoxDetail();
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
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }


    @Override
    protected void updateCommunicationBoxDetail() {
        Conversation conversation = mCurrentConversationManager.getCurrentConversation();
        if (conversation == null) {
            return;
        }
        doUpdateCommunicationBoxDetail(Conversation.getDefaultJson(), conversation.getEmoticonsAsMap(), false);
    }

    @Override
    protected void loadMessagesFromServer() {
        Conversation conversation = mCurrentConversationManager.getCurrentConversation();
        if (conversation == null || mUserManager.getCurrentUser() == null
                || mAdapter == null || mIsNoMoreMessages) {
            // Chat box and user are required.
            // So if they are unavailable by now, don't load.
            // Also, don't load if there adapter is not ready or there is no more messages.
            return;
        }


        Intent intent = new Intent(getActivity(), GetMessagesIntentService.class);
        intent.putExtra(GetMessagesIntentService.EXTRA_CONVERSATION_ID, conversation.getId());
        intent.putExtra(GetMessagesIntentService.EXTRA_OLDEST_MESSAGE, mAdapter.getOldestMessageItem());
        getActivity().startService(intent);
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

    public class ConversationBoxMessagesLoaderCallbacks extends CommunicationBoxMessagesLoaderCallbacks {
        @Override
        public Loader<CommunicationBoxMessagesLoader.Result> onCreateLoader(int id, Bundle args) {
            return new CommunicationBoxMessagesLoader(
                    getActivity(),
                    mCurrentConversationManager.getCurrentConversation().getId());
        }
    }
}