package com.chatwing.whitelabel.services;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;

import com.chatwing.whitelabel.contentproviders.ChatWingContentProvider;
import com.chatwing.whitelabel.events.GotMoreMessagesEvent;
import com.chatwing.whitelabel.pojos.Conversation;
import com.chatwing.whitelabel.pojos.Message;
import com.chatwing.whitelabel.pojos.User;
import com.chatwing.whitelabel.pojos.responses.MessagesResponse;
import com.chatwing.whitelabel.tables.MessageTable;
import com.chatwing.whitelabel.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * Author: Huy Nguyen
 * Date: 7/4/13
 * Time: 2:31 PM
 */
public class GetMessagesIntentService extends BaseIntentService {
    public static final String EXTRA_CHAT_BOX_ID = "chat_box_id";
    public static final String EXTRA_CONVERSATION = "conversation";
    public static final String EXTRA_OLDEST_MESSAGE = "oldest_message";
    private static boolean isRunning;

    public GetMessagesIntentService() {
        super("GetMessagesIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) return;
        isRunning = true;
        int chatBoxId = intent.getIntExtra(EXTRA_CHAT_BOX_ID, 0);
        Conversation conversation = (Conversation) intent.getSerializableExtra(EXTRA_CONVERSATION);
        Message oldestMessage
                = (Message) intent.getSerializableExtra(EXTRA_OLDEST_MESSAGE);
        GotMoreMessagesEvent event;
        try {
            // Save messages to DB and only notify the ones that are inserted
            // to DB.
            MessagesResponse messagesResponse;
            if (chatBoxId != 0) {
                messagesResponse = mApiManager.loadMessages(
                        mUserManager.getCurrentUser(),
                        chatBoxId,
                        oldestMessage);
            } else {
                messagesResponse = mApiManager.loadMessages(
                        mUserManager.getCurrentUser(),
                        conversation.getId(),
                        oldestMessage);
            }
            List<Message> messagesFromServer = messagesResponse.getMessages();
            LogUtils.v(oldestMessage + "<==oldest Loaded from server " + (messagesFromServer != null ? messagesFromServer.size() : "null"));
            List<Message> insertedMessages = new ArrayList<Message>();
            Uri uri = ChatWingContentProvider.getMessagesUri();
            ContentResolver contentResolver = getContentResolver();

            for (Message message : messagesFromServer) {
                String messageId = message.getId();

                //Dirty
                if (chatBoxId == 0) {
                    //Set username for conversation
                    message.setUserName(getUserNameForMessage(message,
                            mUserManager.getCurrentUser(),
                            conversation));
                }

                Uri newRecord = contentResolver.insert(
                        uri,
                        MessageTable.getContentValues(message));
                if (newRecord.getLastPathSegment().equals("-1")) {
                    LogUtils.e("Failed to insert message with ID: " + messageId);
                } else {
                    insertedMessages.add(message);
                }
            }

            if (chatBoxId != 0) {
                event = new GotMoreMessagesEvent(
                        chatBoxId,
                        messagesFromServer,
                        insertedMessages);
            } else {
                event = new GotMoreMessagesEvent(
                        conversation.getId(),
                        messagesFromServer,
                        insertedMessages);
            }
        } catch (Exception exc) {
            if (chatBoxId != 0) {
                event = new GotMoreMessagesEvent(chatBoxId, exc);
            } else {
                event = new GotMoreMessagesEvent(conversation.getId(), exc);
            }
        }
        isRunning = false;
        post(event);
    }

    private String getUserNameForMessage(Message message, User me, Conversation conversation) {
        if (me == null) {
            return null;
        }
        if (me.getIdentifier().equals(message.getUserIdentifier())) {
            return me.getName();
        }
        return conversation.getConversationAlias(me.getId());
    }

    public static boolean isRunning() {
        return isRunning;
    }

    private void post(final GotMoreMessagesEvent event) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mBus.post(event);
            }
        });
    }
}
