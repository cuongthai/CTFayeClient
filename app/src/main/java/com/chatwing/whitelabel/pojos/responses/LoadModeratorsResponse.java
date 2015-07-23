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


import com.chatwing.whitelabel.pojos.BaseUser;
import com.google.gson.annotations.SerializedName;

/**
 * Created by cuongthai on 4/2/14.
 */
public class LoadModeratorsResponse extends BaseResponse {
    @SerializedName("data")
    private Moderator[] moderators;

    public Moderator[] getModerators() {
        return moderators;
    }

    public static class Moderator extends BaseUser {
        private String name;

        public String getName() {
            return name;
        }

        public void setLoginID(String loginId) {
            this.loginId = loginId;
        }

        public void setLoginType(String loginType) {
            this.type = loginType;
        }

        public void setName(String name) {
            this.name = name;
        }

    }
}
