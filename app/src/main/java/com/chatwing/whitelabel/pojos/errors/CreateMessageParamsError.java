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

package com.chatwing.whitelabel.pojos.errors;

import com.chatwing.whitelabel.pojos.Message;
import com.google.gson.annotations.SerializedName;

/**
 * Author: Huy Nguyen
 * Date: 7/1/13
 * Time: 4:27 PM
 */
public class CreateMessageParamsError {
    public static final String TYPE_EMPTY_MESSAGE = "empty_message";
    public static final String TYPE_LONG_MESSAGE = "too_long_message";
    public static final String TYPE_ILLEGAL_INPUT = "illegal_input";
    public static final String TYPE_BLOCK = "block";

    private String type;
    @SerializedName("reason")
    private String errorMessage;
    @SerializedName("user_message")
    private Message userMessage;

    public String getType() {
        return type;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Message getUserMessage() {
        return userMessage;
    }
}
