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

import com.chatwing.whitelabel.utils.JsonConstantsProvider;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by cuongthai on 4/1/14.
 */
public class Conversation implements Serializable {
    private String id;
    @SerializedName("date_created")
    private long createdDate;
    @SerializedName("chat_user_id")
    private String chatUserId;
    private User[] users;

    @SerializedName("last_message_date")
    private long lastMessageDate;

    @SerializedName("unread_message")
    private long unreadCount;

    @SerializedName("date_updated")
    private long dateUpdated;

    public Conversation(String id, long createdDate) {
        this.id = id;
        this.createdDate = createdDate;
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        return id.equals(((Conversation) o).id);
    }

    public long getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(long dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User[] getUsers() {
        return users;
    }

    public void setUsers(User[] users) {
        this.users = users;
    }

    public long getCreatedDate() {
        return createdDate;
    }

    public long getLastMessageDate() {
        return lastMessageDate;
    }

    public long getUnreadCount() {
        return unreadCount;
    }

    public static CommunicationBoxJson getDefaultJson() {
        return new Gson().fromJson(JsonConstantsProvider.DEFAULT_COMMUNICATION_JSON, CommunicationBoxJson.class);
    }

    public static Emoticon[] getEmoticons() {
        return JsonConstantsProvider.emoticonObject;
    }

    public String getConversationAlias(String currentUserId) {
        for (User user : users) {
            if (!user.getId().equals(currentUserId)) {
                return user.getName();
            }
        }
        //Talk to myself
        if (users != null && users.length == 1) {
            return users[0].getName();
        }
        return null;
    }

    public void setUnreadCount(long unreadCount) {
        this.unreadCount = unreadCount;
    }

    public void setCreatedDate(long date) {
        createdDate = date;
    }

    public void setLastDate(long date) {
        lastMessageDate = date;
    }

    public boolean allowShowNotification(User currentUser) {
        if (users == null || currentUser == null) return false;
        for (User user : users) {
            if (user.getIdentifier().equals(currentUser.getIdentifier())) {
                return true;
            }
        }
        return false;
    }

    public boolean allowPushNotification(User currentUser) {
        if (users == null || currentUser == null) return false;
        for (User user : users) {
            if (user.getIdentifier().equals(currentUser.getIdentifier())) {
                return user.isConversationPushNotification();
            }
        }
        return false;
    }

    public boolean allowEmailNotification(User currentUser) {
        if (users == null || currentUser == null) return false;
        for (User user : users) {
            if (user.getIdentifier().equals(currentUser.getIdentifier())) {
                return user.isConversationEmailNotification();
            }
        }
        return false;
    }

    public User getMe(User currentUser) {
        if (users == null || currentUser == null) return null;
        for (User user : users) {
            if (user.getIdentifier().equals(currentUser.getIdentifier())) {
                return user;
            }
        }
        return null;
    }

    public Map<String, String> getEmoticonsAsMap() {
        Map<String, String> emos =new HashMap<String, String>();
        for(Emoticon emoticon:JsonConstantsProvider.emoticonObject){
            emos.put(emoticon.getSymbol(), emoticon.getImage());
        }
        return emos;
    }
}
