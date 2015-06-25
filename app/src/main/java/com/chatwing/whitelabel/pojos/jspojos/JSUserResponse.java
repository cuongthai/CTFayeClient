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

import com.chatwing.whitelabel.pojos.BaseUser;
import com.google.gson.annotations.SerializedName;

/**
 * Created by steve on 08/12/2014.
 */
public class JSUserResponse extends BaseUser {
    @SerializedName("user_avatar")
    private String userAvatar;
    @SerializedName("user_name")
    private String userName;

    public String getUserAvatar() {
        return userAvatar;
    }

    public String getUserName() {
        return userName;
    }
}
