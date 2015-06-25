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

package com.chatwing.whitelabel.pojos.responses;


import com.chatwing.whitelabel.pojos.Conversation;

/**
 * Created by cuongthai on 4/1/14.
 */
@SuppressWarnings("ALL")
public class CreateConversationResponse extends BaseResponse {
    private Conversation data;

    @Override
    public String toString() {
        return data.toString();
    }

    public Conversation getData() {
        return data;
    }
}
