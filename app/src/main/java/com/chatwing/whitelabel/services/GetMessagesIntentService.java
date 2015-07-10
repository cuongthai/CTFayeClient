package com.chatwing.whitelabel.services;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;

import com.chatwing.whitelabel.contentproviders.ChatWingContentProvider;
import com.chatwing.whitelabel.events.GotMoreMessagesEvent;
import com.chatwing.whitelabel.pojos.Message;
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
    public static final String EXTRA_CONVERSATION_ID = "conversation_id";
    public static final String EXTRA_OLDEST_MESSAGE = "oldest_message";
    private static boolean isRunning;

    public GetMessagesIntentService() {
        super("GetMessagesIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        isRunning = true;
        int chatBoxId = intent.getIntExtra(EXTRA_CHAT_BOX_ID, 0);
        String conversationId = intent.getStringExtra(EXTRA_CONVERSATION_ID);
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
                        conversationId,
                        oldestMessage);
            }
            List<Message> messagesFromServer = messagesResponse.getMessages();
            LogUtils.v(oldestMessage+ "<==oldest Loaded from server "+(messagesFromServer!=null?messagesFromServer.size():"null"));
            List<Message> insertedMessages = new ArrayList<Message>();
            Uri uri = ChatWingContentProvider.getMessagesUri();
            ContentResolver contentResolver = getContentResolver();

            for (Message message : messagesFromServer) {
                String messageId = message.getId();
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
                        conversationId,
                        messagesFromServer,
                        insertedMessages);
            }
        } catch (Exception exc) {
            if (chatBoxId != 0) {
                event = new GotMoreMessagesEvent(chatBoxId, exc);
            } else {
                event = new GotMoreMessagesEvent(conversationId, exc);
            }
        }
        isRunning = false;
        post(event);
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
