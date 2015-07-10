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

import android.content.Intent;

import com.chatwing.whitelabel.contentproviders.ChatWingContentProvider;
import com.chatwing.whitelabel.events.CreateMessageEvent;
import com.chatwing.whitelabel.pojos.Message;
import com.chatwing.whitelabel.pojos.User;
import com.chatwing.whitelabel.pojos.responses.CreateMessageResponse;
import com.chatwing.whitelabel.tables.MessageTable;


/**
 * Author: Huy Nguyen
 * Date: 6/29/13
 * Time: 11:27 AM
 */
public class CreateMessageIntentService extends BaseIntentService {
    public static final String EXTRA_MESSAGE = "message";

    public CreateMessageIntentService() {
        super("CreateMessageIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        User user = mUserManager.getCurrentUser();
        Message message = (Message) intent.getSerializableExtra(EXTRA_MESSAGE);
        if (user == null || message == null) return;
        CreateMessageEvent event;
        try {
            CreateMessageResponse response = mApiManager.createMessage(user, message);
            message = response.getMessage();

            if (message != null) {
                // Save the new message to DB
                getContentResolver().insert(
                        ChatWingContentProvider.getMessagesUri(),
                        MessageTable.getContentValues(message));
            }

            if (!message.isPrivate()) {
                event = new CreateMessageEvent(message.getChatBoxId(), response);
            } else {
                event = new CreateMessageEvent(message.getConversationID(), response);
            }
        } catch (Exception e) {
            if (!message.isPrivate()) {
                event = new CreateMessageEvent(message.getChatBoxId(), e);
            } else {
                event = new CreateMessageEvent(message.getConversationID(), e);
            }
        }
        post(event);
    }

    private void post(final CreateMessageEvent event) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mBus.post(event);
            }
        });
    }
}
