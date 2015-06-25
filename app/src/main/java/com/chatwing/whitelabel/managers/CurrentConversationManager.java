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

package com.chatwing.whitelabel.managers;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.chatwing.whitelabel.Constants;
import com.chatwing.whitelabel.contentproviders.ChatWingContentProvider;
import com.chatwing.whitelabel.events.CurrentChatBoxEvent;
import com.chatwing.whitelabel.events.CurrentConversationEvent;
import com.chatwing.whitelabel.events.LoadCurrentConversationFailedEvent;
import com.chatwing.whitelabel.pojos.Conversation;
import com.chatwing.whitelabel.pojos.params.CreateConversationParams;
import com.chatwing.whitelabel.services.AckConversationIntentService;
import com.chatwing.whitelabel.services.CreateConversationIntentService;
import com.chatwing.whitelabel.tables.ConversationTable;
import com.chatwing.whitelabel.utils.LogUtils;
import com.chatwing.whitelabel.validators.ConversationIdValidator;
import com.squareup.otto.Bus;

/**
 * Created by cuongthai on 21/07/2014.
 */
public class CurrentConversationManager extends CurrentCommunicationManager {
    private final ConversationIdValidator mConversationIdValidator;
    protected final LoaderManager mLoaderManager;
    private Conversation mCurrentConversation;


    public CurrentConversationManager(Context context,
                                      Bus bus,
                                      LoaderManager loaderManager,
                                      ConversationIdValidator conversationIdValidator) {
        super(context, bus);
        mLoaderManager = loaderManager;
        mConversationIdValidator = conversationIdValidator;
    }

    public void loadConversation(String conversationKey) {
        if (!mConversationIdValidator.isValid(conversationKey)) {
            return;
        }

        removeCurrentConversation();
        mBus.post(new CurrentConversationEvent(CurrentChatBoxEvent.Status.LOADING, null));

        loadFromDB(conversationKey);
    }

    public void removeCurrentConversation() {
        LogUtils.v("Test ACK removeCurrentConversation");
        if (mCurrentConversation != null) {
            AckConversationIntentService.ack(mContext, mCurrentConversation.getId());
        }
        mCurrentConversation = null;
        stopBackgroundTasks();
        mBus.post(new CurrentConversationEvent(CurrentChatBoxEvent.Status.REMOVED, null));
    }

    @Override
    public void onPause() {
        super.onPause();
        stopBackgroundTasks();
    }

    @Override
    public void onDestroy() {
        mCurrentConversation = null;
    }

    private void stopBackgroundTasks() {
        mLoaderManager.destroyLoader(0);
    }


    private void setCurrentConversation(Conversation newConversation) {
        if (newConversation == null) {
            return;
        }
        LogUtils.v("Test ACK setCurrentConversation");

        CurrentConversationEvent.Status status =
                mCurrentConversation != null && mCurrentConversation.equals(newConversation)
                        ? CurrentConversationEvent.Status.UPDATED
                        : CurrentConversationEvent.Status.LOADED;
        mCurrentConversation = newConversation;
        mBus.post(new CurrentConversationEvent(status, newConversation));
    }

    private void loadFromDB(String id) {
        Bundle args = new Bundle(1);
        args.putString(LoaderCallbacks.ARG_CONVERSATION_ID, id);
        mLoaderManager.restartLoader(0, args, new LoaderCallbacks());
    }

    public class LoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {
        public static final String ARG_CONVERSATION_ID = "conversation_id";

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            String conversationId = args.getString(ARG_CONVERSATION_ID);
            Uri uri = ChatWingContentProvider.getConversationWithIdUri(conversationId);
            return new CursorLoader(
                    mContext,
                    uri,
                    ConversationTable.getMinimumProjection(),
                    null,
                    null,
                    null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            Conversation newConversation = null;
            if (data.getCount() != 0) {
                data.moveToFirst();
                try {
                    newConversation = ConversationTable.getConversation(data);
                } catch (Exception exc) {
                    LogUtils.e(exc);
                    mBus.post(new LoadCurrentConversationFailedEvent(exc));
                }
            }

            if (newConversation != null) {
                setCurrentConversation(newConversation);
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
        }
    }

    public void loadConversationForUser(String mRequestedUserHashKey) {
        Intent createConversation = new Intent(mContext, CreateConversationIntentService.class);
        createConversation.putExtra(CreateConversationIntentService.EXTRA_USER,
                new CreateConversationParams.SimpleUser(mRequestedUserHashKey, Constants.TYPE_ENTERPRISE));
        mContext.startService(createConversation);

        mBus.post(new CurrentConversationEvent(CurrentChatBoxEvent.Status.LOADING, null));
    }

    public Conversation getCurrentConversation() {
        return mCurrentConversation;
    }


}
