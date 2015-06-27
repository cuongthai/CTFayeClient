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
import com.chatwing.whitelabel.tables.CategoryTable;
import com.chatwing.whitelabel.tables.ChatBoxTable;
import com.chatwing.whitelabel.tables.ConversationTable;
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


    private void handleDebug(Intent intent) {
        if (!mNetworkUtils.hasInternetConnection()) {
            return;
        }
        long t1 = System.currentTimeMillis();
        setIsInProgress(true);
        post(SyncCommunicationBoxEvent.startedEvent());

        SyncCommunicationBoxEvent result;
        User user = mUserManager.getCurrentUser();
        try {

            ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
            if (intent.getBooleanExtra(UPDATE_CATEGORIES_FLAG, true)) {
                ChatBoxListResponse chatBoxListResponse = mApiManager.loadChatBoxes();
                ChatBoxDetailsResponse newsBoxResponse = mApiManager.loadChatBoxDetails(user, 3719);
                ChatBoxDetailsResponse newsBoxResponse1 = mApiManager.loadChatBoxDetails(user, 3720);
                ArrayList<ChatBox> chatBoxes = new ArrayList<ChatBox>();
                chatBoxes.add(newsBoxResponse.getData());
                chatBoxes.add(newsBoxResponse1.getData());
                Category category = new Category("Fake News", chatBoxes);

                chatBoxListResponse.getCategories().add(category);
                fetchUnreadCount(chatBoxListResponse);
                addDeleteOldDataOperations(batch);
                addInsertOperations(chatBoxListResponse.getCategories(), batch);
            }

            if (mUserManager.userCanLoadConversations() && intent.getBooleanExtra(UPDATE_CONVERSATION_FLAG, true)) {
                LoadConversationsResponse loadConversationsResponse =
                        mApiManager.loadConversations(user, Constants.MAX_NUMBER_OF_CONVERSATIONS, 0);
                String conversationId = intent.getStringExtra(EXTRA_CONVERSATION_ID);
                addUpdateConversationsOperations(loadConversationsResponse.getData(), batch);
                addMarkAsReadCurrentConversationOperations(conversationId, batch);
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
        LogUtils.v("Time to sync " + (System.currentTimeMillis() - t1) / 1000.0f);
    }

    private void fetchUnreadCount(ChatBoxListResponse chatBoxListResponse) {
//        Map<Integer, Integer> chatBoxUnreadCount = new HashMap<Integer, Integer>();
//        for (Category category : chatBoxListResponse.getCategories()) {
//            for (ChatBox chatBox : category.getChatBoxes()) {
//                int chatboxId = chatBox.getId();
//                int count = 0;
//                try {
//                    count = mApiManager.getUnreadCountForChatbox(mUserManager.getCurrentUser(),
//                            chatboxId);
//                    LogUtils.v("Fetching unread count " + chatboxId + ": count =" + count);
//                    chatBoxUnreadCount.put(chatboxId, count);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//
//        ArrayList<Category> categories = chatBoxListResponse.getCategories();
//        for (Category category : categories) {
//            ArrayList<ChatBox> chatBoxes = category.getChatBoxes();
//            for (ChatBox chatBox : chatBoxes) {
//                if (chatBoxUnreadCount.containsKey(chatBox.getId())) {
//                    chatBox.setUnreadCount(chatBoxUnreadCount.get(chatBox.getId()));
//                }
//            }
//        }
    }


    @Override
    protected void onHandleIntent(Intent intent) {
//        handleChatWingOfficial(intent);
//        handleKentucky(intent);
//        handleSeattle(intent);
        handleDestiny(intent);
//        handleDebug(intent);
//        handleStaging(intent);
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
                if(chatBoxListResponse==null){
                    throw ApiManager.ApiException.createException(new Exception(getString(R.string.error_failed_to_load_chatboxes)));
                }
                fetchUnreadCount(chatBoxListResponse);
                addDeleteOldDataOperations(batch);
                addInsertOperations(chatBoxListResponse.getCategories(), batch);
            }

            if (mUserManager.userCanLoadConversations() && intent.getBooleanExtra(UPDATE_CONVERSATION_FLAG, true)) {
                LoadConversationsResponse loadConversationsResponse =
                        mApiManager.loadConversations(user, Constants.MAX_NUMBER_OF_CONVERSATIONS, 0);
                String conversationId = intent.getStringExtra(EXTRA_CONVERSATION_ID);
                addUpdateConversationsOperations(loadConversationsResponse.getData(), batch);
                addMarkAsReadCurrentConversationOperations(conversationId, batch);
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

    private void handleKentucky(Intent intent) {
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
                ChatBoxListResponse chatBoxListResponse = new ChatBoxListResponse();
                ChatBoxDetailsResponse newsBoxResponse = mApiManager.loadChatBoxDetails(user, 342198);
                ChatBoxDetailsResponse newsBoxResponse1 = mApiManager.loadChatBoxDetails(user, 342199);
                ChatBoxDetailsResponse generalBoxResponse1 = mApiManager.loadChatBoxDetails(user, 342197);
                ChatBoxDetailsResponse generalBoxResponse2 = mApiManager.loadChatBoxDetails(user, 141143);
                ChatBoxDetailsResponse generalBoxResponse3 = mApiManager.loadChatBoxDetails(user, 342196);
                ChatBoxDetailsResponse mediaBoxResponse4 = mApiManager.loadChatBoxDetails(user, 347957);
                ChatBoxDetailsResponse mediaBoxResponse5 = mApiManager.loadChatBoxDetails(user, 347984);

                ArrayList<ChatBox> newChatBoxes = new ArrayList<ChatBox>();
                ArrayList<ChatBox> generalChatBoxes = new ArrayList<ChatBox>();
                ArrayList<ChatBox> mediaChatBoxes = new ArrayList<ChatBox>();
                newChatBoxes.add(newsBoxResponse.getData());
                newChatBoxes.add(newsBoxResponse1.getData());
                generalChatBoxes.add(generalBoxResponse1.getData());
                generalChatBoxes.add(generalBoxResponse2.getData());
                generalChatBoxes.add(generalBoxResponse3.getData());
                mediaChatBoxes.add(mediaBoxResponse4.getData());
                mediaChatBoxes.add(mediaBoxResponse5.getData());
                if (ChatWing.isDebugging()) {
                    ChatBoxDetailsResponse mediaBoxResponse6 = mApiManager.loadChatBoxDetails(user, 326493);
                    mediaChatBoxes.add(mediaBoxResponse6.getData());
                }
                Category category1 = new Category("General", generalChatBoxes);
                Category category2 = new Category("News", newChatBoxes);
                Category category3 = new Category("Media", mediaChatBoxes);

                chatBoxListResponse.getCategories().add(category1);
                chatBoxListResponse.getCategories().add(category2);
                chatBoxListResponse.getCategories().add(category3);
                fetchUnreadCount(chatBoxListResponse);
                addDeleteOldDataOperations(batch);
                addInsertOperations(chatBoxListResponse.getCategories(), batch);
            }

            if (mUserManager.userCanLoadConversations() && intent.getBooleanExtra(UPDATE_CONVERSATION_FLAG, true)) {
                LoadConversationsResponse loadConversationsResponse =
                        mApiManager.loadConversations(user, Constants.MAX_NUMBER_OF_CONVERSATIONS, 0);
                String conversationId = intent.getStringExtra(EXTRA_CONVERSATION_ID);
                addUpdateConversationsOperations(loadConversationsResponse.getData(), batch);
                addMarkAsReadCurrentConversationOperations(conversationId, batch);
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

    private void handleSeattle(Intent intent) {
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
                ChatBoxListResponse chatBoxListResponse = new ChatBoxListResponse();
                ChatBoxDetailsResponse announcementBoxResponse = mApiManager.loadChatBoxDetails(user, 375126);
                ChatBoxDetailsResponse companyNewsBoxResponse = mApiManager.loadChatBoxDetails(user, 375132);
                ChatBoxDetailsResponse companyPicturesBoxResponse = mApiManager.loadChatBoxDetails(user, 375136);
                ChatBoxDetailsResponse companyVideosBoxResponse = mApiManager.loadChatBoxDetails(user, 375135);
                ChatBoxDetailsResponse newProductIntelligencesBoxResponse = mApiManager.loadChatBoxDetails(user, 375139);
                ChatBoxDetailsResponse employeeVideoInstructionsBoxResponse = mApiManager.loadChatBoxDetails(user, 375138);
                ChatBoxDetailsResponse newPoliciesBoxResponse = mApiManager.loadChatBoxDetails(user, 375134);

                ArrayList<ChatBox> announcementBoxGroup = new ArrayList<ChatBox>();
                ArrayList<ChatBox> companyNewsBoxGroup = new ArrayList<ChatBox>();
                ArrayList<ChatBox> companyPicturesBoxGroup = new ArrayList<ChatBox>();
                ArrayList<ChatBox> companyVideosBoxGroup = new ArrayList<ChatBox>();
                ArrayList<ChatBox> newProductIntelligencesBoxGroup = new ArrayList<ChatBox>();
                ArrayList<ChatBox> employeeVideoInstructionsBoxGroup = new ArrayList<ChatBox>();
                ArrayList<ChatBox> newPoliciesBoxGroup = new ArrayList<ChatBox>();

                announcementBoxGroup.add(announcementBoxResponse.getData());
                companyNewsBoxGroup.add(companyNewsBoxResponse.getData());
                companyPicturesBoxGroup.add(companyPicturesBoxResponse.getData());
                companyVideosBoxGroup.add(companyVideosBoxResponse.getData());
                newProductIntelligencesBoxGroup.add(newProductIntelligencesBoxResponse.getData());
                employeeVideoInstructionsBoxGroup.add(employeeVideoInstructionsBoxResponse.getData());
                newPoliciesBoxGroup.add(newPoliciesBoxResponse.getData());

                if (ChatWing.isDebugging()) {
                    ChatBoxDetailsResponse mediaBoxResponse6 = mApiManager.loadChatBoxDetails(user, 326493);
                    ChatBoxDetailsResponse mediaBoxResponse7 = mApiManager.loadChatBoxDetails(user, 368830);
                    announcementBoxGroup.add(mediaBoxResponse6.getData());
                    announcementBoxGroup.add(mediaBoxResponse7.getData());
                }
                Category category1 = new Category("Announcement Room", announcementBoxGroup);
                Category category2 = new Category("Company News Room", companyNewsBoxGroup);
                Category category3 = new Category("Company Pictures", companyPicturesBoxGroup);
                Category category4 = new Category("Company Videos", companyVideosBoxGroup);
                Category category5 = new Category("New Product Intelligence", newProductIntelligencesBoxGroup);
                Category category6 = new Category("Employee Video Instructions", employeeVideoInstructionsBoxGroup);
                Category category7 = new Category("New Policies ", newPoliciesBoxGroup);

                chatBoxListResponse.getCategories().add(category1);
                chatBoxListResponse.getCategories().add(category2);
                chatBoxListResponse.getCategories().add(category3);
                chatBoxListResponse.getCategories().add(category4);
                chatBoxListResponse.getCategories().add(category5);
                chatBoxListResponse.getCategories().add(category6);
                chatBoxListResponse.getCategories().add(category7);
                fetchUnreadCount(chatBoxListResponse);
                addDeleteOldDataOperations(batch);
                addInsertOperations(chatBoxListResponse.getCategories(), batch);
            }

            if (mUserManager.userCanLoadConversations() && intent.getBooleanExtra(UPDATE_CONVERSATION_FLAG, true)) {
                LoadConversationsResponse loadConversationsResponse =
                        mApiManager.loadConversations(user, Constants.MAX_NUMBER_OF_CONVERSATIONS, 0);
                String conversationId = intent.getStringExtra(EXTRA_CONVERSATION_ID);
                addUpdateConversationsOperations(loadConversationsResponse.getData(), batch);
                addMarkAsReadCurrentConversationOperations(conversationId, batch);
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

    private void handleDestiny(Intent intent) {
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
                ChatBoxListResponse chatBoxListResponse = new ChatBoxListResponse();
                ChatBoxDetailsResponse chatboxResponse1 = mApiManager.loadChatBoxDetails(user, 303738);
                ChatBoxDetailsResponse chatboxResponse2 = mApiManager.loadChatBoxDetails(user, 303718);
                ChatBoxDetailsResponse chatboxResponse3 = mApiManager.loadChatBoxDetails(user, 303734);
                ChatBoxDetailsResponse chatboxResponse4 = mApiManager.loadChatBoxDetails(user, 303717);

                ArrayList<ChatBox> generalGroup = new ArrayList<ChatBox>();
                generalGroup.add(chatboxResponse1.getData());
                generalGroup.add(chatboxResponse2.getData());
                generalGroup.add(chatboxResponse3.getData());
                generalGroup.add(chatboxResponse4.getData());

                if (ChatWing.isDebugging()) {
                    ChatBoxDetailsResponse mediaBoxResponse6 = mApiManager.loadChatBoxDetails(user, 326493);
                    ChatBoxDetailsResponse mediaBoxResponse7 = mApiManager.loadChatBoxDetails(user, 368830);
                    generalGroup.add(mediaBoxResponse6.getData());
                    generalGroup.add(mediaBoxResponse7.getData());
                }
                Category category1 = new Category("General", generalGroup);

                chatBoxListResponse.getCategories().add(category1);

                fetchUnreadCount(chatBoxListResponse);
                addDeleteOldDataOperations(batch);
                addInsertOperations(chatBoxListResponse.getCategories(), batch);
            }

            if (mUserManager.userCanLoadConversations() && intent.getBooleanExtra(UPDATE_CONVERSATION_FLAG, true)) {
                LoadConversationsResponse loadConversationsResponse =
                        mApiManager.loadConversations(user, Constants.MAX_NUMBER_OF_CONVERSATIONS, 0);
                String conversationId = intent.getStringExtra(EXTRA_CONVERSATION_ID);
                addUpdateConversationsOperations(loadConversationsResponse.getData(), batch);
                addMarkAsReadCurrentConversationOperations(conversationId, batch);
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

    private Map<Integer, Integer> getUnreads(Cursor query) {
        Map<Integer, Integer> maps = new HashMap<Integer, Integer>();
        boolean has = query.moveToFirst();
        while (has) {
            int id = query.getInt(query.getColumnIndex(ChatBoxTable._ID));
            int unread = query.getInt(query.getColumnIndex(ChatBoxTable.UNREAD_COUNT));


            maps.put(id, unread);
            has = query.moveToNext();
        }
        return maps;
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

    private void addUpdateConversationsOperations(Conversation[] conversations,
                                                  ArrayList<ContentProviderOperation> batch) {
        Uri conversationUri = ChatWingContentProvider.getConversationsUri();
        batch.add(ContentProviderOperation
                .newDelete(conversationUri)
                .build());

        addInsertConversationsOperations(conversations, batch);
    }

    private void addInsertConversationsOperations(Conversation[] conversations, ArrayList<ContentProviderOperation> batch) {
        Uri uri = ChatWingContentProvider.getConversationsUri();
        ContentValues values;

        for (Conversation conversation : conversations) {
            values = ConversationTable.getContentValues(conversation);
            batch.add(ContentProviderOperation.newInsert(uri)
                    .withValues(values)
                    .build());
        }
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

        batch.add(ContentProviderOperation.newDelete(categoriesUri).build());

        ContentProviderOperation.Builder operationBuilder
                = ContentProviderOperation.newDelete(chatBoxesUri);
        batch.add(operationBuilder.build());
    }


    public static boolean isInProgress() {
        synchronized (sLock) {
            return sIsInProgress;
        }
    }
}
