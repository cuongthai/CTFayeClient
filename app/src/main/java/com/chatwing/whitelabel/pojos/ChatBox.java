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

import android.text.TextUtils;

import com.chatwing.whitelabel.utils.JsonConstantsProvider;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: Huy Nguyen
 * Date: 4/14/13
 * Time: 11:43 AM
 */

public class ChatBox implements Serializable{
    private int id;
    private String key;
    private String name;

    private Network network;
    private String fayeChannel;
    private CommunicationBoxJson json;
    private Emoticon[] emoticons;
    private List<Filter> filters;
    private boolean isAdmin;
    private boolean isModerator;
    private String alias;
    public Map<String, Boolean> permissions;
    @SerializedName("authentication_methods")
    public Map<String, Boolean> authenticationMethods;
    @SerializedName("audio_url")
    private String audioUrl;
    @SerializedName("audio_name")
    private String audioName;

    @SerializedName("is_read_only")
    private boolean isReadOnly;
    private Map<String, Boolean> notificationStatus;

    // Variables belong to application logic.
    // Since they are not part of response from server, they should be transient
    // and not be encoded into JSON representation.
    // They should be stored in separated columns in ChatBoxTable in DB if needed.
    /**
     * Indicate number of unread messages in the chat box.
     */
    private transient int unreadCount = 0;

    public ChatBox(int id, String key, String name, String fayeChannel, String alias) {
        this.id = id;
        this.key = key;
        this.name = name;
        this.fayeChannel = fayeChannel;
        this.alias = alias;
    }

    public int getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public String getChatboxPassword() {
        return json != null ? json.getChatBoxPassword() : null;
    }

    public boolean hasPassword() {
        return !TextUtils.isEmpty(getChatboxPassword());
    }

    public Network getNetwork() {
        return network;
    }

    public int getNetworkId() {
        // Network is optional
        return network == null ? 0 : network.getId();
    }

    public String getFayeChannel() {
        return fayeChannel;
    }

    public CommunicationBoxJson getJson() {
        return json == null ? JsonConstantsProvider.communicationJsonObject : json;
    }

    public Emoticon[] getEmoticons() {
        return emoticons == null ? JsonConstantsProvider.emoticonObject : emoticons;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setJson(CommunicationBoxJson json) {
        this.json = json;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public List<Filter> getFilters() {
        return filters == null ? new ArrayList<Filter>() : filters;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public boolean isModerator() {
        return isModerator;
    }

    public Map<String, Boolean> getPermissions() {
        return permissions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return (o instanceof ChatBox) && this.id == ((ChatBox) o).id;
    }

    /**
     * @return true if this chat box has detailed information and can be
     * rendered properly. false otherwise.
     */
    public boolean hasDetails() {
        return json != null;
    }

    public void setFayeChannel(String faye) {
        fayeChannel = faye;
    }

    public String getAlias() {
        return alias;
    }

    public void setFilters(List<Filter> filters) {
        this.filters = filters;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public boolean isReadOnly() {
        return isReadOnly;
    }

    public Map<String, Boolean> getNotificationStatus() {
        return notificationStatus;
    }

    public String getAudioName() {
        return audioName;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public Map<String, String> getEmoticonsAsMap() {
        Map<String, String> emos =new HashMap<String, String>();
        for(Emoticon emoticon:emoticons){
            emos.put(emoticon.getSymbol(), emoticon.getImage());
        }
        return emos;
    }
}
