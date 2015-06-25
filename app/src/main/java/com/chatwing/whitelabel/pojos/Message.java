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

import com.chatwing.whitelabel.pojos.params.Params;
import com.google.gson.annotations.SerializedName;

/**
 * Author: Huy Nguyen
 * Date: 4/6/13
 * Time: 3:23 PM
 */
public class Message extends Params implements Comparable<Message> {

    /**
     * Indicates status of a message. Int value of this enum can be stored in
     * Database ({@link com.chatwing.tables.MessageTable}),
     * it should not be changed without a good reason.
     */
    public enum Status {
        SENDING,
        PUBLISHED,
        FAILED,
        BLOCKED,
        DELETING,
        IGNORED
    }

    // Fields that are parsed from JSON
    private String id;
    @SerializedName("chatbox_id")
    private int chatBoxId;
    @SerializedName("conversation_id")
    private String conversationID;
    private String content;
    @SerializedName("date_created")
    private long createdDate;
    @SerializedName("network_id")
    private int networkId;
    @SerializedName("random_key")
    private String randomKey;
    @SerializedName("login_id")
    private String userId;
    @SerializedName("user_ip")
    private String userIp;
    @SerializedName("user_name")
    private String userName;
    @SerializedName("login_type")
    private String userType;
    @SerializedName("user_avatar")
    private String avatar;

    /**
     * Indicates this message is conversation message or public message.
     */
    private boolean isPrivate;
    /**
     * Status of the message. It is transient so are not part
     * of the JSON representation. Thus, it is neither sent to our server
     * nor stored in value of {@link com.chatwing.tables.MessageTable#DATA}
     * in DB.
     */
    private transient Status status;
    /**
     * The time that this message was started to be sent to server. It can be
     * used to sort messages if 1 of them is being sent (having SENDING
     * status). It should not be part of the JSON representation since the
     * server doesn't need it. Thus, it is transient and is stored in DB in a
     * separated column ({@link com.chatwing.tables.MessageTable#SENDING_DATE}.
     * Also, it should be separately passed together with Message object.
     */
    private transient long sendingDate;

    public Message(User user, int chatBoxId, String content, long createdDate,
                   String randomKey, Status status) {
        this(user, content, createdDate, randomKey, status);
        this.chatBoxId = chatBoxId;
    }

    public Message(User user, String id, String content, long createdDate,
                   String randomKey, Status status) {
        this(user, content, createdDate, randomKey, status);
        this.isPrivate = true;
        this.conversationID = id;
    }

    private Message(User user, String content, long createdDate,
                    String randomKey, Status status) {
        this.userName = user.getName();
        this.userId = user.getLoginId();
        this.userType = user.getLoginType();
        this.userIp = user.getIp(); //TODO User IP's likely to be out of dated.
        this.content = content;
        this.createdDate = createdDate;
        this.randomKey = randomKey;
        this.avatar = user.getAvatar();

        this.status = status;
    }

    public String getId() {
        return id;
    }

    public int getChatBoxId() {
        return chatBoxId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getCreatedDate() {
        return createdDate;
    }

    public String getUserName() {
        return userName;
    }

    public String getRandomKey() {
        return randomKey;
    }

    public Status getStatus() {
        return status;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIp() {
        return userIp;
    }

    public void setIp(String ip) {
        userIp = ip;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }

    public long getSendingDate() {
        return sendingDate;
    }

    public void setSendingDate(long sendingDate) {
        this.sendingDate = sendingDate;
    }

    private boolean isBeingSent() {
        return status == Status.SENDING;
    }

    private boolean hasValidSendingDate() {
        return sendingDate != 0;
    }

    public String getAvatar() {
        return avatar;
    }

    public void copyUserData(Message anotherMsg) {
        this.userName = anotherMsg.userName;
        this.userId = anotherMsg.userId;
        this.userType = anotherMsg.userType;
        this.avatar = anotherMsg.avatar;
        this.content = anotherMsg.content;
        this.isPrivate = anotherMsg.isPrivate;
    }

    @Override
    public int compareTo(Message another) {
        // If either this or the other message is being sent, we should prefer
        // sendingDate to relativeDate for comparison (#101).
        boolean shouldCompareSendingDate = isBeingSent() || another.isBeingSent();
        long thisDate = shouldCompareSendingDate && hasValidSendingDate()
                ? sendingDate
                : createdDate;
        long anotherDate = shouldCompareSendingDate && another.hasValidSendingDate()
                ? another.sendingDate
                : another.createdDate;
        return thisDate < anotherDate ? -1 : (thisDate > anotherDate ? 1 : 0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Message)) {
            return false;
        }
        Message another = (Message) o;
        if (!TextUtils.isEmpty(id)) {
            return id.equals(another.id);
        }
        if (!TextUtils.isEmpty(randomKey)) {
            return randomKey.equals(another.randomKey);
        }
        return compareTo(another) == 0;
    }


    public boolean isPrivate() {
        return conversationID != null ? true : false;
    }

    public String getConversationID() {
        return conversationID;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setLoginType(String loginType) {
        this.userType = loginType;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getUserIdentifier() {
        return BaseUser.computeIdentifier(userId, userType);
    }

    public boolean isTheSameUserType(String userType) {
        return userType.equals(this.userType);
    }

    public String getUserType() {
        return userType;
    }

    public String getUserId() {
        return userId;
    }

    public void setChatBoxId(int chatBoxId) {
        this.chatBoxId = chatBoxId;
    }

    public void setConversationID(String conversationID) {
        this.conversationID = conversationID;
    }
}
