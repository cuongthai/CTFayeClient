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
 * Author: Huy Nguyen
 * Date: 7/17/13
 * Time: 7:37 AM
 */
public class ChatBoxUnreadCountChangedEvent {
    private int chatBoxId;

    public ChatBoxUnreadCountChangedEvent(int chatBoxId) {
        this.chatBoxId = chatBoxId;
    }

    public int getChatBoxId() {
        return chatBoxId;
    }
}
