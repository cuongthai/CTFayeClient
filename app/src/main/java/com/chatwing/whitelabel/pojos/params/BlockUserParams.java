package com.chatwing.whitelabel.pojos.params;

import com.google.gson.annotations.SerializedName;

/**
 * Created by steve on 30/06/2014.
 */
public class BlockUserParams extends Params {
    public static final String METHOD_SOCIAL = "social";
    public static final String METHOD_IP = "ip";
    private String method;

    @SerializedName("login_id")
    private String loginId;
    @SerializedName("login_type")
    private String loginType;

    //Since we may not have block user ip, so we will use message ip to block and let server take care of it
    @SerializedName("message_id")
    private String messageId;
    @SerializedName("user_ip")
    private String ip;

    private String reason;
    private long duration;

    @SerializedName("clear_message")
    private boolean clearMessage;

    @SerializedName("chatbox_id")
    private String chatBoxId;

    private BlockUserParams(String method,
                            boolean clearMessage,
                            String chatBoxId,
                            long duration,
                            String reason) {
        this.method = method;
        this.clearMessage = clearMessage;
        this.chatBoxId = chatBoxId;
        this.duration = duration;
        this.reason = reason;
    }

    /**
     * Block by method social
     *
     * @param loginId
     * @param loginType
     * @param method
     * @param clearMessage
     * @param chatBoxId
     * @param duration
     */
    public BlockUserParams(String loginId,
                           String loginType,
                           String method,
                           boolean clearMessage,
                           String chatBoxId,
                           long duration,
                           String reason) {
        this(method, clearMessage, chatBoxId, duration, reason);
        this.loginId = loginId;
        this.loginType = loginType;
    }

    /**
     * Block by IP(message_id)
     *
     * @param messageId
     * @param method
     * @param clearMessage
     * @param chatBoxId
     * @param duration
     */
    public BlockUserParams(String messageId,
                           String method,
                           boolean clearMessage,
                           String chatBoxId,
                           long duration,
                           String reason) {
        this(method, clearMessage, chatBoxId, duration, reason);
        this.messageId = messageId;
    }

    public String getChatBoxId() {
        return chatBoxId;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getIp() {
        return ip;
    }

    public String getMethod() {
        return method;
    }

    public String getLoginId() {
        return loginId;
    }

    public String getLoginType() {
        return loginType;
    }
}

