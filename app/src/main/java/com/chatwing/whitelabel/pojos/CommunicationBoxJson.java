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

import com.chatwing.whitelabel.utils.ColorUtils;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Author: Huy Nguyen
 * Date: 5/12/13
 * Time: 9:54 AM
 */
public class CommunicationBoxJson implements Serializable{
    private String mainColor;
    private String messageContentColor;
    private String messageDateColor;
    private String messageUserColor;
    private String messageAreaColor;
    private String messageInputBgColor;
    private String messageInputColor;
    private String chatBackgroundImage;
    private String chatboxBackgroundImage;
    private String messageBodyBackgroundColor;
    private boolean showAvatar;
    private boolean showLoginIcon;
    private int externalImageMaxWidth;
    private int externalImageMaxHeight;
    @SerializedName("chatboxPassword")
    private String chatBoxPassword;
    private boolean usePassword;

    private String userListBackgroundImage;
    @SerializedName("userListBgColor")
    private String userListBackgroundColor;
    private String userListColor;

    private String announcementContent;
    private String announcementColor;
    private boolean allowCloseAnnouncement;
    private boolean enableAnnouncement;
    private String announcementBgColor;
    private boolean announcementTransparentBg;


    public int getMainColor() {
        return ColorUtils.parse(mainColor);
    }

    /** Used when didn't change in BBCode **/
    public int getMessageContentColor() {
        return ColorUtils.parse(messageContentColor);
    }

    public int getMessageDateColor() {
        return ColorUtils.parse(messageDateColor);
    }

    public int getMessageUserColor() {
        return ColorUtils.parse(messageUserColor);
    }

    public int getMessageAreaColor() {
        return ColorUtils.parse(messageAreaColor);
    }

    public int getMessageInputBgColor() {
        return ColorUtils.parse(messageInputBgColor);
    }

    public int getMessageInputColor() {
        return ColorUtils.parse(messageInputColor);
    }

    public String getChatBackgroundImage() {
        return chatBackgroundImage;
    }

    public String getChatboxBackgroundImage() {
        return chatboxBackgroundImage;
    }

    public int getMessageBodyBackgroundColor() {
        return ColorUtils.parse(messageBodyBackgroundColor);
    }

    public boolean isShowAvatar() {
        return showAvatar;
    }
    public boolean isShowLoginIcon() {
        return showLoginIcon;
    }

    public int getExternalImageMaxWidth() {
        return externalImageMaxWidth;
    }

    public int getExternalImageMaxHeight() {
        return externalImageMaxHeight;
    }

    public String getChatBoxPassword() {
        return chatBoxPassword;
    }

    public boolean isUsePassword() {
        return usePassword;
    }

    public String getUserListBackgroundImage() {
        return userListBackgroundImage;
    }

    public int getUserListBackgroundColor() {
        return ColorUtils.parse(userListBackgroundColor);
    }

    public int getUserListColor() {
        return ColorUtils.parse(userListColor);
    }

    public String getAnnouncementContent() {
        return announcementContent;
    }

    public String getAnnouncementColor(){
        return announcementColor;
    }

    public String getAnnouncementBgColor() {
        return announcementBgColor;
    }

    public boolean isAllowCloseAnnouncement() {
        return allowCloseAnnouncement;
    }

    public boolean isAnnouncementTransparentBg() {
        return announcementTransparentBg;
    }

    public boolean isEnableAnnouncement() {
        return enableAnnouncement;
    }
}
