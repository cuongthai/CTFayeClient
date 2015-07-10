package com.chatwing.whitelabel.loaders;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;

import com.chatwing.whitelabel.contentproviders.ChatWingContentProvider;
import com.chatwing.whitelabel.pojos.Message;
import com.chatwing.whitelabel.tables.MessageTable;

import java.util.ArrayList;
import java.util.List;


/**
 * Author: Huy Nguyen
 * Date: 5/11/13
 * Time: 4:16 PM
 */
public class CommunicationBoxMessagesLoader
        extends AsyncTaskLoader<CommunicationBoxMessagesLoader.Result> {
    //TODO cache the cursor and return it immediately if available.
    private int mChatBoxId;
    private String mConversationId;
    private boolean isPrivate;

    public CommunicationBoxMessagesLoader(Context context, int chatBoxId) {
        super(context);
        mChatBoxId = chatBoxId;
        isPrivate = false;
    }

    public CommunicationBoxMessagesLoader(Context context, String conversationId) {
        super(context);
        mConversationId = conversationId;
        isPrivate = true;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public Result loadInBackground() {
        Result result = new Result();
        Cursor cursor = null;

        try {
            Uri uri;
            if (!isPrivate) {
                uri = ChatWingContentProvider.getMessagesInChatBoxUri(mChatBoxId);
            } else {
                uri = ChatWingContentProvider.getMessagesInConversationUri(mConversationId);
            }

            // Load all messages that belong the provided chat box
            // Filter is done later because it's complicated to compare both
            // relative date and created date of messages (like what is done in
            // Message.compareTo(Message)) in SQL query.
            // Since all messages in DB are returned when mMessage is not
            // provided, filtering is rarely done.
            cursor = getContext().getContentResolver().query(
                    uri,
                    MessageTable.getMinimumProjection(),
                    null,
                    null,
                    MessageTable.CREATED_DATE + " DESC");
            int count = cursor.getCount();
            List<Message> messages = new ArrayList<Message>(count);
            if (count > 0 && cursor.moveToFirst()) {
                do {
                    Message message = MessageTable.getMessage(cursor);
                    // Status is not stored in DB since all of them should
                    // have PUBLISHED status.
                    message.setStatus(Message.Status.PUBLISHED);

                    messages.add(message);
                } while (cursor.moveToNext());
            }

            result.messages = messages;
        } catch (Exception exc) {
            result.exception = exc;
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return result;
    }

    public static class Result {
        Exception exception;
        List<Message> messages;

        public Exception getException() {
            return exception;
        }

        public List<Message> getMessages() {
            return messages;
        }
    }
}
