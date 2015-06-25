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

import com.chatwing.whitelabel.pojos.ChatBox;
import com.chatwing.whitelabel.utils.StringUtils;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Author: Huy Nguyen
 * Date: 4/30/13
 * Time: 6:30 PM
 */
@SuppressWarnings("ALL")
public class ChatBoxDetailsResponse extends BaseResponse {
    private ChatBox data;

    public ChatBox getData() {
        return data;
    }

    public static class ChatBoxDetailErrorParams {
        @SerializedName("forceLogin") //TODO should be force_login
        private boolean forceLogin;
        @SerializedName("authentication_methods")
        private Map<String, Boolean> authenticationMethods;
        @SerializedName("description")
        private String message;

        public Map<String, Boolean> getAuthenticationMethods() {
            return authenticationMethods;
        }

        public String getMessage() {
            return message;
        }

        public boolean isForceLogin() {
            return forceLogin;
        }

        public String getAuthenticationMethodString() {
            Set<String> keys = authenticationMethods.keySet();
            List<String> methods = new ArrayList<String>();
            for(String key: keys){
                if(authenticationMethods.get(key)){
                    methods.add(key);
                }
            }
            return StringUtils.join(", ", methods.toArray(new String[methods.size()]));
        }
    }
}
