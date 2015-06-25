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


import com.chatwing.whitelabel.managers.ApiManager;
import com.chatwing.whitelabel.pojos.ChatBox;

/**
 * Created by nguyenthanhhuy on 1/3/14.
 */
@SuppressWarnings("FieldCanBeLocal")
public class CurrentChatBoxEvent extends CurrentCommunicationEvent {
    private ChatBox mChatbox;

    public CurrentChatBoxEvent(Status status, ChatBox chatbox) {
        super(status);
        mChatbox = chatbox;
    }

    @Override
    public String getUrl() {
        return String.format(ApiManager.CHATBOX_URL, mChatbox.getKey());
    }

    public ChatBox getChatbox() {
        return mChatbox;
    }
}
