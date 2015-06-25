package com.chatwing.whitelabel.pojos;

import java.io.Serializable;

/**
 * Created by nguyenthanhhuy on 10/26/13.
 * A lightweight chat box POJO that only contains neccessary information of a chat box.
 * It's the returned value of chatbox/create and chatbox/search APIs.
 */
public class LightWeightChatBox implements Serializable {
    private int id;
    private String key;
    private String name;
    private String fayeChannel;
    /**
     * Alias of the chat box. Returned in chatbox/search API.
     */
    private String alias;

    public int getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public String getFayeChannel() {
        return fayeChannel;
    }

    public String getAlias() {
        return alias;
    }

    public void setFayeChannel(String fayeChannel) {
        this.fayeChannel = fayeChannel;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public static LightWeightChatBox copyFromChatbox(ChatBox chatbox){
        LightWeightChatBox lightWeightChatBox = new LightWeightChatBox();
        lightWeightChatBox.id = chatbox.getId();
        lightWeightChatBox.fayeChannel = chatbox.getFayeChannel();
        lightWeightChatBox.key = chatbox.getKey();
        lightWeightChatBox.name = chatbox.getName();
        lightWeightChatBox.alias = chatbox.getAlias();
        return lightWeightChatBox;
    }
}
