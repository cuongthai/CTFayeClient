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

package com.chatwing.whitelabel.events;

/**
 * Created by nguyenthanhhuy on 12/21/13.
 */
public class MessageEvent extends ExceptionEvent {
    private int chatBoxId;
    private String conversationId;
    private boolean isPrivate;

    public MessageEvent(int chatBoxId) {
        this.chatBoxId = chatBoxId;
        isPrivate = false;
    }

    public MessageEvent(String conversationId) {
        this.conversationId = conversationId;
        isPrivate = true;
    }

    public MessageEvent(int chatBoxId, Exception exception) {
        super(exception);
        this.chatBoxId = chatBoxId;
        isPrivate = false;
    }

    public MessageEvent(String conversationId, Exception exception) {
        super(exception);
        this.conversationId = conversationId;
        isPrivate = true;
    }

    public int getChatBoxId() {
        return chatBoxId;
    }

    public String getConversationId() {
        return conversationId;
    }

    public boolean isPrivate() {
        return isPrivate;
    }
}
