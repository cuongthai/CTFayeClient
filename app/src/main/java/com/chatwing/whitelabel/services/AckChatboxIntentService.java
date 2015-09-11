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
import com.chatwing.whitelabel.tables.ChatBoxTable;
import com.chatwing.whitelabel.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by steve on 15/05/2014.
 */
public class AckChatboxIntentService extends BaseIntentService {
    public static final String EXTRA_CHATBOX_IDS = "chatbox_ids";

    public static void ack(Context context, Integer chatboxID) {
        Intent ack = new Intent(context, AckChatboxIntentService.class);
        ArrayList<Integer> chatboxIds =new ArrayList<Integer>();
        chatboxIds.add(chatboxID);
        ack.putIntegerArrayListExtra(AckChatboxIntentService.EXTRA_CHATBOX_IDS, chatboxIds);
        context.startService(ack);
    }

    public static void ack(Context context, ArrayList<Integer> chatboxIDs) {
        Intent ack = new Intent(context, AckChatboxIntentService.class);
        ack.putIntegerArrayListExtra(AckChatboxIntentService.EXTRA_CHATBOX_IDS, chatboxIDs);
        context.startService(ack);
    }

    public AckChatboxIntentService() {
        super("AckChatboxIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) return;
        List<Integer> chatboxIDs =  intent.getIntegerArrayListExtra(EXTRA_CHATBOX_IDS);

        for(Integer chatboxID: chatboxIDs) {
            /**
             * This should be called in these cases:
             * 1. Open chatbox DONE
             * 2. Message received with limit DONE
             * 3. Pause chatbox message fragment DONE
             * 4. When detect out of sync.
             *    (Read in offline)  NO NEED
             *    (Sync categories while in chatbox) DONE
             * 5. Go to another box, current one should ack to flush current messages DONE
             */
            try {
                mApiManager.ackChatbox(mUserManager.getCurrentUser(),
                        chatboxID);
                markAsRead(chatboxID);
                LogUtils.v("Test ACK Chatbox onHandleIntent");
            } catch (Exception e) {
            }
        }
    }

    private void markAsRead(Integer chatboxID) {
        Uri uri = ChatWingContentProvider.getChatBoxWithIdUri(chatboxID);
        ContentValues contentValues = new ContentValues();
        contentValues.put(ChatBoxTable.UNREAD_COUNT, 0);
        getContentResolver().update(uri, contentValues, null, null);
    }
}
