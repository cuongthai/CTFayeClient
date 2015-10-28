package com.chatwing.whitelabel.services;

import android.content.ContentProviderOperation;
import android.content.Intent;
import android.net.Uri;

import com.chatwing.whitelabel.contentproviders.ChatWingContentProvider;
import com.chatwing.whitelabel.events.GotMessagesEvent;
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
    public static final String EXTRA_MORE = "more";
    public static final int MAX_MESSAGES = 20;

    public GetMessagesIntentService() {
        super("GetMessagesIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) return;
        int chatBoxId = intent.getIntExtra(EXTRA_CHAT_BOX_ID, 0);
        boolean loadMore = intent.getBooleanExtra(EXTRA_MORE, true);
        Conversation conversation = (Conversation) intent.getSerializableExtra(EXTRA_CONVERSATION);
        Message oldestMessage
                = (Message) intent.getSerializableExtra(EXTRA_OLDEST_MESSAGE);
        //If not load from tail, will remove oldestMessage
        if (!loadMore) {
            oldestMessage = null;
        }
        GotMessagesEvent event;
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

            Uri uri = ChatWingContentProvider.getMessagesUri();
            ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
            for (Message message : messagesFromServer) {
                //Dirty
                if (chatBoxId == 0) {
                    //Set username for conversation
                    message.setUserName(getUserNameForMessage(message,
                            mUserManager.getCurrentUser(),
                            conversation));
                }

                batch.add(ContentProviderOperation.newInsert(uri)
                        .withValues(MessageTable.getContentValues(message))
                        .build());
            }
            getContentResolver().applyBatch(ChatWingContentProvider.AUTHORITY, batch);

            LogUtils.v(oldestMessage + "<==oldest Loaded from server " + (messagesFromServer != null ? messagesFromServer.size() : "null") + " loadMore " + loadMore);

            if (chatBoxId != 0) {
                event = new GotMessagesEvent(
                        chatBoxId,
                        messagesFromServer,
                        loadMore);
            } else {
                event = new GotMessagesEvent(
                        conversation.getId(),
                        messagesFromServer,
                        loadMore);
            }
        } catch (Exception exc) {
            if (chatBoxId != 0) {
                event = new GotMessagesEvent(chatBoxId, exc);
            } else {
                event = new GotMessagesEvent(conversation.getId(), exc);
            }
        }
        post(event);
        LogUtils.v("oldest Loaded from server NOT RUNNING NOW");
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

    private void post(final GotMessagesEvent event) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mBus.post(event);
            }
        });
    }
}
