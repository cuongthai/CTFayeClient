package com.chatwing.whitelabel.loaders;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;

import com.chatwing.whitelabel.contentproviders.ChatWingContentProvider;
import com.chatwing.whitelabel.pojos.Message;
import com.chatwing.whitelabel.services.GetMessagesIntentService;
import com.chatwing.whitelabel.tables.MessageTable;
import com.chatwing.whitelabel.utils.LogUtils;

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
        onContentChanged(); //prevent duplication calls in loadInBackground
    }

    public CommunicationBoxMessagesLoader(Context context, String conversationId) {
        super(context);
        mConversationId = conversationId;
        isPrivate = true;
        onContentChanged(); //prevent duplication calls in loadInBackground
    }

    @Override
    protected void onStartLoading() {
        if (takeContentChanged()) { //prevent duplication calls in loadInBackground
            LogUtils.v("Loader onStartLoading");
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad(); //prevent duplication calls in loadInBackground
    }

    @Override
    public Result loadInBackground() {
        Result result = new Result();
        Cursor cursor = null;
        LogUtils.v("Loader is running in background");
        try {
            Uri uri;
            if (!isPrivate) {
                uri = ChatWingContentProvider.getMessagesInChatBoxUri(mChatBoxId);
            } else {
                uri = ChatWingContentProvider.getMessagesInConversationUri(mConversationId);
            }

            //Only return top 20 messages from DB. loadMore only happen on the remote.
            //Although messages are coming from GetMessagesService and Faye.
            //Do this because, we reload message from the HEAD! so this should help latest messages on the screen

            cursor = getContext().getContentResolver().query(
                    uri,
                    MessageTable.getMinimumProjection(),
                    null,
                    null,
                    MessageTable.CREATED_DATE + " DESC LIMIT "+ GetMessagesIntentService.MAX_MESSAGES);
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
            LogUtils.v("Loader loaded "+messages.size()+ " from DB ");
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
