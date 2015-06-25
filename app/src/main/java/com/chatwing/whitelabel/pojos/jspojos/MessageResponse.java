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

package com.chatwing.whitelabel.pojos.jspojos;

import com.chatwing.whitelabel.pojos.ChatBox;
import com.chatwing.whitelabel.pojos.Conversation;
import com.chatwing.whitelabel.pojos.Message;
import com.chatwing.whitelabel.pojos.User;
import com.google.gson.annotations.SerializedName;

/**
 * Created by steve on 06/01/2015.
 */
public class MessageResponse {
    private Message[] messages;
    @SerializedName("chat_user")
    private User chatUser;
    private ChatBox chatbox;
    private Conversation conversation;

    public Message[] getMessages() {
        return messages;
    }

    public ChatBox getChatbox() {
        return chatbox;
    }

    public User getChatUser() {
        return chatUser;
    }

    public Conversation getConversation() {
        return conversation;
    }
}
