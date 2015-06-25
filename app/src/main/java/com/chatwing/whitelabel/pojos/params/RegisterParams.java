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

@SuppressWarnings("ALL")
public class RegisterParams extends Params {
    @SerializedName("username")
    private String username;
    private String email;
    private String password;
    @SerializedName("agree")
    private boolean agreeConditions;
    @SerializedName("auto_create_chatbox")
    private boolean autoCreateChatbox;

    public RegisterParams(String email, String password,
                          boolean agreeConditions,
                          boolean autoCreateChatbox) {
        this.username = email;
        this.email = email;
        this.password = password;
        this.agreeConditions = agreeConditions;
        this.autoCreateChatbox = autoCreateChatbox;
    }
}
