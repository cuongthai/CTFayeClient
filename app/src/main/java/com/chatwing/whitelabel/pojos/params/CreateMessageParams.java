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

package com.chatwing.whitelabel.pojos.params;

import com.google.gson.annotations.SerializedName;

/**
 * Author: Huy Nguyen
 * Date: 7/31/13
 * Time: 3:56 PM
 */
public class CreateMessageParams extends BaseChatBoxParams {
    private String content;
    @SerializedName("random_key")
    private String randomKey;

    public CreateMessageParams(String content, int chatBoxId, String randomKey) {
        super(chatBoxId);
        this.content = content;
        this.randomKey = randomKey;
    }
}
