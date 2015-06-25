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

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.chatwing.whitelabel.contentproviders.ChatWingContentProvider;
import com.chatwing.whitelabel.tables.ConversationTable;
import com.chatwing.whitelabel.utils.LogUtils;


/**
 * Created by steve on 15/05/2014.
 */
public class AckConversationIntentService extends BaseIntentService {
    public static final String EXTRA_CONVERSATION_ID = "conversation_id";

    public static void ack(Context context, String conversationId) {
        Intent ack = new Intent(context, AckConversationIntentService.class);
        ack.putExtra(AckConversationIntentService.EXTRA_CONVERSATION_ID, conversationId);
        context.startService(ack);
    }

    public AckConversationIntentService() {
        super("AckConversationIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String conversationId = intent.getStringExtra(EXTRA_CONVERSATION_ID);
        /**
         * This should be called in these cases:
         * 1. Open conversation    DONE
         * 2. Message received with limit  DONE
         * 3. Pause conversation message fragment DONE
         * 4. When detect out of sync.
         *    (Read in offline)  DONE
         *    (Sync categories while in conversation) DONE
         * 5. Switch to new conversation, current one should ack to flush current messages DONE
         */
        try {
            mApiManager.ackConversation(mUserManager.getCurrentUser(),
                    conversationId);
            markAsRead(conversationId);
            LogUtils.v("Test ACK onHandleIntent");
        } catch (Exception e) {
            LogUtils.e(e);
        }
    }

    private void markAsRead(String conversationID) {
        Uri uri = ChatWingContentProvider.getConversationWithIdUri(conversationID);
        ContentValues contentValues = new ContentValues();
        contentValues.put(ConversationTable.UNREAD_COUNT, 0);
        contentValues.put(ConversationTable.DATE_UPDATED, System.currentTimeMillis());
        getContentResolver().update(uri, contentValues, null, null);
    }
}
