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


import com.chatwing.whitelabel.pojos.responses.CreateMessageResponse;

/**
 * Author: Huy Nguyen
 * Date: 6/29/13
 * Time: 11:32 AM
 */
public class CreateMessageEvent extends MessageEvent {
    /**
     * The response from server. Response can contain
     * an {@link com.chatwing.whitelabel.pojos.errors.CreateMessageParamsError} which is returned by
     * the server and should be handled properly.
     */
    private CreateMessageResponse response;

    public CreateMessageEvent(int chatBoxId, CreateMessageResponse response) {
        super(chatBoxId);
        this.response = response;
    }

    public CreateMessageEvent(String conversationId, CreateMessageResponse response) {
        super(conversationId);
        this.response = response;
    }

    public CreateMessageEvent(int chatBoxId, Exception exception) {
        super(chatBoxId, exception);
    }

    public CreateMessageEvent(String conversationId, Exception exception) {
        super(conversationId, exception);
    }

    public CreateMessageResponse getResponse() {
        return response;
    }
}
