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

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;

import com.chatwing.whitelabel.ChatWing;
import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.activities.CommunicationActivity;
import com.chatwing.whitelabel.contentproviders.ChatWingContentProvider;
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
public class NotificationIntentService extends IntentService {
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
    NotificationManager mNotificationManager;
    @Inject
    UserManager mUserManager;

    public NotificationIntentService() {
        super("GcmIntentService");
        ChatWing.instance(this).getChatwingGraph().inject(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (mUserManager.getCurrentUser() == null) return;//No login no push
        Bundle extras = intent.getExtras();
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

            notifyForBox(freshConversations, messageResponse.getConversation(), targetUser);
        } else {
            ChatBox chatbox = messageResponse.getChatbox();

            List<Message> freshChatboxes = getMessagesByGroup(chatbox.getId());
            notifyForBox(freshChatboxes, chatbox);
        }
    }

    private boolean supportVersion(Bundle extras) {
        if (extras == null) {
            return false;
        }
        int pushVersion = extras.getInt("version");
        if(pushVersion<=CURRENT_PUSH_VERSION){
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
            showBroadCastMessage(broadCastMessageResponse.getChatUser().getName(), broadCastMessageResponse.getMessage());
            return true;
        }
        return false;
    }

    private void showBroadCastMessage(String name, String message) {
        StatisticTracker.trackReceiveNotification(StatisticTracker.NOTIFICATION_BROADCAST_TYPE);

        PendingIntent contentIntent = PendingIntent.getActivity(this,
                0,
                new Intent(this, ChatWing.instance(this).getMainActivityClass()),
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(getString(R.string.broadcast_tag) + " " + name + " ")
                        .setTicker(message)
                        .setContentText(message);

        builder.setContentIntent(contentIntent);
        builder.setAutoCancel(true);
        mNotificationManager.notify(message.hashCode(), builder.build());
    }

    private void notifyForBox(List<Message> messages, Conversation conversation, User targetUser) {
        if (!targetUser.equals(mUserManager.getCurrentUser()))
            return; //Not send to me, so nothing right now
        if (messages.size() == 0) return;
        StatisticTracker.trackReceiveNotification(StatisticTracker.NOTIFICATION_CONVERSATION_TYPE);

        Intent i = new Intent(this, ChatWing.instance(this).getMainActivityClass());
        i.setAction(CommunicationActivity.ACTION_OPEN_CONVERSATION);
        i.putExtra(CommunicationActivity.CONVERSATION_ID, conversation.getId());
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        LogUtils.v("Check user name " + messages.get(0).getUserName());
        doNotify(i, messages.get(0).getUserName(), messages, conversation.getId().hashCode());
    }

    private void notifyForBox(List<Message> messages, ChatBox chatbox) {
        if (messages.size() == 0) return;

        StatisticTracker.trackReceiveNotification(StatisticTracker.NOTIFICATION_CHATBOX_TYPE);
        Intent i = new Intent(this, ChatWing.instance(this).getMainActivityClass());
        i.setAction(CommunicationActivity.ACTION_OPEN_CHATBOX);
        i.putExtra(CommunicationActivity.CHATBOX_ID, chatbox.getId());
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        doNotify(i, chatbox.getName(), messages, chatbox.getId());
    }

    private void doNotify(Intent i, String contentTitle, List<Message> messages, int notificationCode) {
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(contentTitle)
                        .setTicker(messages.get(0).getContent())
                        .setContentText(messages.get(0).getContent());
        NotificationCompat.InboxStyle inboxStyle =
                new NotificationCompat.InboxStyle();
        String[] lastMessages = new String[Math.min(messages.size(), MAX_MESSAGES_PER_GROUP)];
        inboxStyle.setBigContentTitle(contentTitle);
        for (int j = 0; j < lastMessages.length; j++) {
            inboxStyle.addLine(messages.get(j).getContent());
        }
        builder.setStyle(inboxStyle);
        builder.setNumber(messages.size());

        builder.setContentIntent(contentIntent);
        builder.setAutoCancel(true);
        mNotificationManager.notify(notificationCode, builder.build());
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
                messages.add(message);

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
                messages.add(message);
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
