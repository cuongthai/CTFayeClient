package com.chatwing.whitelabel.services;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;

import com.chatwing.whitelabel.contentproviders.ChatWingContentProvider;
import com.chatwing.whitelabel.events.CreateBookmarkEvent;
import com.chatwing.whitelabel.managers.ApiManager;
import com.chatwing.whitelabel.pojos.ChatBox;
import com.chatwing.whitelabel.pojos.LightWeightChatBox;
import com.chatwing.whitelabel.pojos.SyncedBookmark;
import com.chatwing.whitelabel.pojos.responses.CreateBookmarkResponse;
import com.chatwing.whitelabel.tables.ChatBoxTable;
import com.chatwing.whitelabel.tables.SyncedBookmarkTable;
import com.chatwing.whitelabel.utils.LogUtils;

import java.util.ArrayList;

import javax.inject.Inject;


/**
 * Created by steve on 21/06/2014.
 */
public class CreateBookmarkIntentService extends BaseIntentService {
    public static final String CHATBOX_KEY = "chatbox";
    private static final String ACTION_CREATE_BOOKMARK = "ACTION_CREATE_BOOKMARK";

    @Inject
    protected ApiManager mApiManager;

    private CreateBookmarkEvent mCreateBookmarkEvent;

    public CreateBookmarkIntentService() {
        super("CreateBookmarkIntentService");
    }

    public static void start(Context context, LightWeightChatBox chatBox) {
        Intent intent = new Intent(context, CreateBookmarkIntentService.class);
        intent.setAction(ACTION_CREATE_BOOKMARK);
        intent.putExtra(CHATBOX_KEY, chatBox);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (mUserManager.getCurrentUser() == null) {
            return;
        }
        String action = intent.getAction();
        if (ACTION_CREATE_BOOKMARK.equals(action)) {
            LightWeightChatBox lightWeighChatBox =
                    (LightWeightChatBox) intent.getSerializableExtra(CHATBOX_KEY);

            ChatBox chatbox = new ChatBox(
                    lightWeighChatBox.getId(),
                    lightWeighChatBox.getKey(),
                    lightWeighChatBox.getName(),
                    lightWeighChatBox.getFayeChannel(),
                    lightWeighChatBox.getAlias()
            );
            // Add chat box to DB before setting it as our current chat box,
            // so that we get notify when this chat box is changed (#131).
            // FIX ME: right now category is required for a chat box.
            // And that requirement is incorrect. So this will be fixed after
            // the DB Scheme is changed, probably (#142.)

            ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();

            //Add missing chatbox if needed
            ContentValues chatBoxContentValues = ChatBoxTable.getContentValues(
                    chatbox, " ");
            batch.add(ContentProviderOperation
                    .newInsert(ChatWingContentProvider.getChatBoxesUri())
                    .withValues(chatBoxContentValues)
                    .build());
            try {
                //Create local bookmark with unsync flag
                SyncedBookmark bookmark = SyncedBookmark.createLocalBookmark(lightWeighChatBox);
                ContentValues bookmarkContentValue = SyncedBookmarkTable.getContentValues(bookmark);
                batch.add(ContentProviderOperation
                        .newInsert(ChatWingContentProvider.getSyncedBookmarksUri())
                        .withValues(bookmarkContentValue)
                        .build());

                getContentResolver().applyBatch(ChatWingContentProvider.AUTHORITY, batch);

                //Create remote bookmark, will update sync flag
                CreateBookmarkResponse bookmarkResponse = mApiManager.createBookmark(
                        mUserManager.getCurrentUser(),
                        lightWeighChatBox.getId());
                mCreateBookmarkEvent = new CreateBookmarkEvent(bookmarkResponse);
            } catch (ApiManager.InvalidIdentityException iie) {
                //Ignore log
                mCreateBookmarkEvent = new CreateBookmarkEvent(iie);
            } catch (Exception e) {
                LogUtils.e(e);
                mCreateBookmarkEvent = new CreateBookmarkEvent(e);
            }
            post(mCreateBookmarkEvent);
        }
    }

    private void post(final CreateBookmarkEvent event) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mBus.post(event);
            }
        });
    }
}
