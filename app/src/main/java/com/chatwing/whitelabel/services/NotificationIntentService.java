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

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;

import com.chatwing.whitelabel.ChatWing;
import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.activities.CommunicationActivity;
import com.chatwing.whitelabel.contentproviders.ChatWingContentProvider;
import com.chatwing.whitelabel.managers.CWNotificationManager;
import com.chatwing.whitelabel.managers.UserManager;
import com.chatwing.whitelabel.pojos.BroadCastMessageResponse;
import com.chatwing.whitelabel.pojos.ChatBox;
import com.chatwing.whitelabel.pojos.Conversation;
import com.chatwing.whitelabel.pojos.Message;
import com.chatwing.whitelabel.pojos.User;
import com.chatwing.whitelabel.pojos.jspojos.MessageResponse;
import com.chatwing.whitelabel.tables.NotificationMessagesTable;
import com.chatwing.whitelabel.utils.LogUtils;
import com.chatwing.whitelabel.utils.StatisticTracker;
import com.google.android.gms.gcm.GcmListenerService;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

/**
 * Author: Huy Nguyen
 * Date: 9/11/13
 * Time: 7:29 AM
 */
public class NotificationIntentService extends GcmListenerService {
    private static final int MAX_MESSAGES_PER_GROUP = 5;

    /**
     * To prevent crashing when updating push format, we introduce push version
     * only process push notification if app push version not older than push
     * Only increase push version on server side if we add/remove fields.
     * If adding fields, we won't increase push version
     */
    private static final int PUSH_VERION_1 = 1; //Init version
    private static final int CURRENT_PUSH_VERSION = PUSH_VERION_1;

    @Inject
    protected UserManager mUserManager;
    @Inject
    protected CWNotificationManager mCWNotificationManager;

    public NotificationIntentService() {
        ChatWing.instance(this).getChatwingGraph().inject(this);
    }

    @Override
    public void onMessageReceived(String from, Bundle extras) {
        if (mUserManager.getCurrentUser() == null) return;//No login no push
        if (from.startsWith("/topics/")) {
            // message received from some topic.
        } else {
            Set<String> keys = extras.keySet();
            for (String k : keys) {
                LogUtils.v("Key in GCM " + k + ":" + extras.get(k));
            }

            if (!supportVersion(extras)) {
                return;
            }

            if (handleBroadCastEvent(extras)) {
                return;
            }
            String params = extras.getString("params");
            if (params == null) return;
            MessageResponse messageResponse = getMessagesFromParams(params);
            if (messageResponse == null) return;
            Message[] messages = messageResponse.getMessages();
            User targetUser = messageResponse.getChatUser();
            if (messages == null || messages.length == 0) {
                return;
            }

            //Fill chatbox_id or conversation_id to message
            fillGroupIDToMessage(messages, extras.getString("type"), messageResponse);

            insertNotificationMessagesToDb(messages);

            if (extras.getString("type").equals("conversation_notification")) {
                String conversationId = messageResponse.getConversation().getId();
                List<Message> freshConversations = getMessagesByGroup(conversationId);
                LogUtils.v("Test notification not receive getMessagesByGroup " + freshConversations.size());

                mCWNotificationManager.notifyForBox(freshConversations,
                        messageResponse.getConversation().getId(),
                        targetUser,
                        false); // We use GCM notification just as backup plan to update latest message so no need to make sound
            } else {
                ChatBox chatbox = messageResponse.getChatbox();

                List<Message> freshChatboxes = getMessagesByGroup(chatbox.getId());
                mCWNotificationManager.notifyForBox(freshChatboxes,
                        chatbox.getName(),
                        chatbox.getId(),
                        false); // We use GCM notification just as backup plan to update latest message so no need to make sound
            }
        }
    }

    private boolean supportVersion(Bundle extras) {
        if (extras == null) {
            return false;
        }
        int pushVersion = extras.getInt("version");
        if (pushVersion <= CURRENT_PUSH_VERSION) {
            return true;
        }
        return false;
    }

    private void fillGroupIDToMessage(Message[] messages, String type, MessageResponse messageResponse) {
        if ("conversation_notification".equals(type)) {
            for (Message message : messages) {
                message.setConversationID(messageResponse.getConversation().getId());
            }
        } else if ("chatbox_notification".equals(type)) {
            for (Message message : messages) {
                message.setChatBoxId(messageResponse.getChatbox().getId());
            }
        }
    }

    private boolean handleBroadCastEvent(Bundle extras) {
        if (extras != null && "broadcast_notification".equals(extras.getString("type"))) {
            String paramsString = extras.getString("params");
            BroadCastMessageResponse broadCastMessageResponse =
                    new Gson().fromJson(paramsString, BroadCastMessageResponse.class);
            mCWNotificationManager.showBroadCastMessage(
                    broadCastMessageResponse.getChatUser().getName(),
                    broadCastMessageResponse.getMessage());
            return true;
        }
        return false;
    }

    private List<Message> getMessagesByGroup(Integer chatboxId) {
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(ChatWingContentProvider.getNotificationMessagesUri(),
                    NotificationMessagesTable.getMinimumProjection(),
                    NotificationMessagesTable.CHAT_BOX_ID + " == " + chatboxId,
                    null,
                    NotificationMessagesTable.CREATED_DATE + " DESC");
            boolean hasNext = cursor.moveToFirst();
            List<Message> messages = new ArrayList<Message>();
            while (hasNext) {
                Message message = NotificationMessagesTable.getMessage(cursor);
                if (message != null) {
                    messages.add(message);
                }

                hasNext = cursor.moveToNext();
            }
            return messages;
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    private List<Message> getMessagesByGroup(String conversationId) {
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(ChatWingContentProvider.getNotificationMessagesUri(),
                    NotificationMessagesTable.getMinimumProjection(),
                    NotificationMessagesTable.CONVERSATION_ID + " ==\"" + conversationId + "\"",
                    null,
                    NotificationMessagesTable.CREATED_DATE + " DESC");
            boolean hasNext = cursor.moveToFirst();
            List<Message> messages = new ArrayList<Message>();
            while (hasNext) {
                Message message = NotificationMessagesTable.getMessage(cursor);
                if (message != null) {
                    messages.add(message);
                }
                hasNext = cursor.moveToNext();
            }
            return messages;
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    private void insertNotificationMessagesToDb(Message[] messages) {
        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
        Uri notificationMessagesUri = ChatWingContentProvider.getNotificationMessagesUri();
        ContentValues values;

        for (Message message : messages) {
            values = NotificationMessagesTable.getContentValues(message);
            batch.add(ContentProviderOperation.newInsert(notificationMessagesUri)
                    .withValues(values)
                    .build());
        }

        try {
            getContentResolver().applyBatch(ChatWingContentProvider.AUTHORITY, batch);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
    }

    private MessageResponse getMessagesFromParams(String params) {
        return new Gson().fromJson(params, MessageResponse.class);
    }
}
