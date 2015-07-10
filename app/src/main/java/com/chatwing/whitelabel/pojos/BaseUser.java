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

package com.chatwing.whitelabel.pojos;

import com.chatwing.whitelabel.Constants;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by cuongthai on 05/08/2014.
 */
public abstract class BaseUser implements Serializable {
    @SerializedName("login_id")
    protected String loginId;
    @SerializedName("login_type")
    protected String type;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BaseUser)) {
            return false;
        }
        return getIdentifier().equals(((BaseUser) o).getIdentifier());
    }

    public String getIdentifier() {
        return computeIdentifier(loginId, type);
    }

    public static String computeIdentifier(String loginId, String loginType) {
        return loginType + "-" + loginId;
    }

    public boolean isGuest(){
        return isGuest(type);
    }

    public boolean isChatWing(){
        return Constants.TYPE_CHATWING.equals(type);
    }

    public boolean isAppUser(){
        return Constants.TYPE_APP.equals(type);
    }

    public static boolean isGuest(String userType) {
        return Constants.TYPE_GUEST.equals(userType);
    }

    public String getLoginId() {
        return loginId;
    }

    public String getLoginType() {
        return type;
    }
}
