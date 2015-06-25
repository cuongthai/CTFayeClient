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

import java.io.Serializable;

/**
 * Author: Huy Nguyen
 * Date: 7/9/13
 * Time: 11:26 AM
 */
public class ViewProfileEvent implements Serializable {
    private final String mAvatarUrl;
    private final String mUserName;
    private final String mUserType;
    private final String mLoginId;
    private boolean mDenyReply;

    private String mUrl;

    public ViewProfileEvent(String url,
                            String avatarUrl,
                            String userName,
                            String userType,
                            String loginId,
                            boolean denyReply) {
        mUrl = url;
        mAvatarUrl = avatarUrl;
        mUserName = userName;
        mUserType = userType;
        mLoginId =  loginId;
        mDenyReply = denyReply;
    }

    public String getUserName() {
        return mUserName;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getAvatarUrl() {
        return mAvatarUrl;
    }

    public String getUserType() {
        return mUserType;
    }

    public String getLoginId(){
        return mLoginId;
    }

    public boolean isDenyReply() {
        return mDenyReply;
    }

}
