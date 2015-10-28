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

package com.chatwing.whitelabel.services;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import com.chatwing.whitelabel.ChatWing;
import com.chatwing.whitelabel.Constants;
import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.contentproviders.ChatWingContentProvider;
import com.chatwing.whitelabel.events.SyncCommunicationBoxEvent;
import com.chatwing.whitelabel.managers.ApiManager;
import com.chatwing.whitelabel.pojos.Category;
import com.chatwing.whitelabel.pojos.ChatBox;
import com.chatwing.whitelabel.pojos.Conversation;
import com.chatwing.whitelabel.pojos.User;
import com.chatwing.whitelabel.pojos.responses.ChatBoxDetailsResponse;
import com.chatwing.whitelabel.pojos.responses.ChatBoxListResponse;
import com.chatwing.whitelabel.pojos.responses.LoadConversationsResponse;
import com.chatwing.whitelabel.pojos.responses.LoadModeratorsResponse;
import com.chatwing.whitelabel.tables.CategoryTable;
import com.chatwing.whitelabel.tables.ChatBoxTable;
import com.chatwing.whitelabel.tables.ConversationTable;
import com.chatwing.whitelabel.tables.DefaultUserTable;
import com.chatwing.whitelabel.utils.LogUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by steve on 15/12/2014.
 */
public class SyncCommunicationBoxesIntentService extends BaseIntentService {
    public static final String UPDATE_CATEGORIES_FLAG = "UPDATE_CATEGORIES_FLAG";
    public static final String UPDATE_CONVERSATION_FLAG = "UPDATE_CONVERSATION_FLAG";
    public static final String EXTRA_CONVERSATION_ID = "conversation_id";

    private static boolean sIsInProgress;
    private static final Object sLock = new Object();

    public SyncCommunicationBoxesIntentService() {
        super("SyncCommunicationBoxesIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        handleChatWingOfficial(intent);
    }

    private void handleChatWingOfficial(Intent intent) {
        if (!mNetworkUtils.hasInternetConnection()) {
            return;
        }
        setIsInProgress(true);
        post(SyncCommunicationBoxEvent.startedEvent());

        SyncCommunicationBoxEvent result;
        User user = mUserManager.getCurrentUser();
        try {

            ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
            if (intent.getBooleanExtra(UPDATE_CATEGORIES_FLAG, true)) {
                ChatBoxListResponse chatBoxListResponse = mApiManager.loadChatBoxes();
                if (chatBoxListResponse == null) {
                    throw ApiManager.ApiException.createException(new Exception(getString(R.string.error_failed_to_load_chatboxes)));
                }
                addDeleteOldDataOperations(batch);
                addInsertOperations(chatBoxListResponse.getCategories(), batch);
            }

            if (mUserManager.userCanLoadConversations() && intent.getBooleanExtra(UPDATE_CONVERSATION_FLAG, true)) {
                LoadConversationsResponse loadConversationsResponse =
                        mApiManager.loadConversations(user, Constants.MAX_NUMBER_OF_CONVERSATIONS, 0);
                LoadModeratorsResponse loadModeratorsResponse =
                        mApiManager.loadModerators(user, Constants.MAX_NUMBER_OF_DEFAULT_USERS, 0);
                String conversationId = intent.getStringExtra(EXTRA_CONVERSATION_ID);
                addUpdateConversationsOperations(loadConversationsResponse.getData(), loadModeratorsResponse.getModerators(), batch);
                addMarkAsReadCurrentConversationOperations(conversationId, batch);

                addUpdateModeratorsOperations(loadModeratorsResponse.getModerators(), batch);
            }

            getContentResolver().applyBatch(ChatWingContentProvider.AUTHORITY, batch);
            getContentResolver().notifyChange(ChatWingContentProvider.getCategorizedChatBoxesUri(), null);

            result = SyncCommunicationBoxEvent.succeedEvent();
        } catch (final Exception exc) {
            LogUtils.e(exc);
            result = SyncCommunicationBoxEvent.failedEvent(exc);
        }

        setIsInProgress(false);
        post(result);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSyncManager.removeServiceFromQueue(this);
    }

    private void addMarkAsReadCurrentConversationOperations(String conversationId,
                                                            ArrayList<ContentProviderOperation> batch) {
        if (conversationId == null) return;
        Uri uri = ChatWingContentProvider.getConversationWithIdUri(conversationId);
        ContentValues contentValues = new ContentValues();
        contentValues.put(ConversationTable.UNREAD_COUNT, 0);
        contentValues.put(ConversationTable.DATE_UPDATED, System.currentTimeMillis());
        batch.add(ContentProviderOperation.newUpdate(uri).withValues(contentValues).build());
    }

    private void post(final SyncCommunicationBoxEvent event) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mBus.post(event);
            }
        });
    }

    private static void setIsInProgress(boolean isInProgress) {
        synchronized (sLock) {
            sIsInProgress = isInProgress;
        }
    }

    private void addUpdateModeratorsOperations(LoadModeratorsResponse.Moderator[] moderators,
                                               ArrayList<ContentProviderOperation> batch) {
        if (moderators == null) {
            return;
        }
        Uri moderatorsUri = ChatWingContentProvider.getModeratorsUri();
        batch.add(ContentProviderOperation
                .newDelete(moderatorsUri)
                .build());

        addInsertModeratorsOperations(moderators, batch);
    }

    private void addUpdateConversationsOperations(Conversation[] conversations,
                                                  LoadModeratorsResponse.Moderator[] moderators,
                                                  ArrayList<ContentProviderOperation> batch) {

        Uri conversationUri = ChatWingContentProvider.getConversationsUri();
        batch.add(ContentProviderOperation
                .newDelete(conversationUri)
                .build());

        addInsertConversationsOperations(conversations, moderators, batch);
    }

    private void addInsertModeratorsOperations(LoadModeratorsResponse.Moderator[] moderators, ArrayList<ContentProviderOperation> batch) {
        Uri uri = ChatWingContentProvider.getModeratorsUri();
        ContentValues values;

        for (LoadModeratorsResponse.Moderator moderator : moderators) {
            values = DefaultUserTable.getContentValues(moderator);
            batch.add(ContentProviderOperation.newInsert(uri)
                    .withValues(values)
                    .build());
        }
    }

    private void addInsertConversationsOperations(Conversation[] conversations,
                                                  LoadModeratorsResponse.Moderator[] moderators,
                                                  ArrayList<ContentProviderOperation> batch) {
        Uri uri = ChatWingContentProvider.getConversationsUri();
        ContentValues values;

        for (Conversation conversation : conversations) {
            User targetUser = conversation.getTargetUser(mUserManager.getCurrentUser());

            values = ConversationTable.getContentValues(conversation, mUserManager.getCurrentUser());
            if (moderators != null && targetUser != null && isModerator(targetUser, moderators)) {
                values.put(ConversationTable.IS_MODERATOR, 1);
            } else {
                values.put(ConversationTable.IS_MODERATOR, 0);
            }

            batch.add(ContentProviderOperation.newInsert(uri)
                    .withValues(values)
                    .build());
        }
    }

    private boolean isModerator(User targetUser, LoadModeratorsResponse.Moderator[] moderators) {
        for (LoadModeratorsResponse.Moderator moderator : moderators) {
            if (moderator.getIdentifier().equals(targetUser.getIdentifier())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds {@link ContentProviderOperation}s to the given batch
     * which will insert given categories and their chat boxes.
     */
    private void addInsertOperations(List<Category> categories,
                                     List<ContentProviderOperation> batch) {
        Uri categoriesUri = ChatWingContentProvider.getCategoriesUri();
        Uri chatBoxesUri = ChatWingContentProvider.getChatBoxesUri();

        ContentValues values;
        String categoryTitle;

        for (Category category : categories) {
            values = CategoryTable.getContentValues(category);
            batch.add(ContentProviderOperation.newInsert(categoriesUri)
                    .withValues(values)
                    .build());
            categoryTitle = category.getTitle();
            for (ChatBox chatBox : category.getChatBoxes()) {
                values = ChatBoxTable.getContentValues(chatBox, categoryTitle);
                batch.add(ContentProviderOperation.newInsert(chatBoxesUri)
                        .withValues(values)
                        .build());
            }
        }
    }

    /**
     * Adds {@link ContentProviderOperation}s to the given batch
     * which will delete all existing categories, chat boxes that are neither
     * bookmarked nor tagged and all messages.
     */
    private void addDeleteOldDataOperations(List<ContentProviderOperation> batch) {
        Uri categoriesUri = ChatWingContentProvider.getCategoriesUri();
        Uri chatBoxesUri = ChatWingContentProvider.getCategorizedChatBoxesUri();
        Uri messagesUri = ChatWingContentProvider.getMessagesUri();

        batch.add(ContentProviderOperation.newDelete(categoriesUri).build());

        ContentProviderOperation.Builder operationBuilder
                = ContentProviderOperation.newDelete(chatBoxesUri);
        batch.add(operationBuilder.build());

        //Delete messages (private message included)
        batch.add(ContentProviderOperation.newDelete(messagesUri).build());
    }


    public static boolean isInProgress() {
        synchronized (sLock) {
            return sIsInProgress;
        }
    }
}
