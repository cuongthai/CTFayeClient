package com.chatwing.whitelabel.utils;

import com.chatwing.whitelabel.pojos.ChatBox;
import com.chatwing.whitelabel.pojos.Conversation;
import com.chatwing.whitelabel.pojos.User;
import com.flurry.android.FlurryAgent;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by steve on 26/05/2014.
 */
public class StatisticTracker {
    public static final String NOTIFICATION_CHATBOX_TYPE = "chatbox";
    public static final String NOTIFICATION_CONVERSATION_TYPE = "conversation";
    public static final String NOTIFICATION_BROADCAST_TYPE = "broadcast";

    public static void startChatBoxEvent(ChatBox chatBox) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("ChatBoxName", chatBox.getName());
        params.put("isAdmin", String.valueOf(chatBox.isAdmin()));
        params.put("isModerator", String.valueOf(chatBox.isModerator()));

        FlurryAgent.logEvent("OpenChatBox", params, true);
    }

    public static void stopChatBoxEvent() {
        FlurryAgent.endTimedEvent("OpenChatBox");
    }

    public static void trackNumberOfBookmarksPerUser(int count) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("count", String.valueOf(count));
        FlurryAgent.logEvent("BookmarksPerUser", params);
    }

    public static void trackReceiveNotification(String type){
        Map<String, String> params = new HashMap<String, String>();
        params.put("type", type);
        FlurryAgent.logEvent("receiveNotification", params);
    }

    public static void trackChatBoxSearch(String query) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("query", query);
        FlurryAgent.logEvent("ChatBoxSearch", params);
    }

    public static void trackNumberOfConversationsPerUser(User user, int count) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("count", String.valueOf(count));
        FlurryAgent.logEvent("ConversationsPerUser", params);
    }

    public static void startConversationEvent(Conversation conversation) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("ConversationID", conversation.getId());

        FlurryAgent.logEvent("OpenConversation", params, true);
    }

    public static void stopConversationEvent() {
        FlurryAgent.endTimedEvent("OpenConversation");
    }

    public static void trackLoginType(String type) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("login_type", type);
        FlurryAgent.logEvent("UserAuthenticate", params);
    }

    public static void trackMessageSent(){
        FlurryAgent.logEvent("MessageSent");
    }
}
