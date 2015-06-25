package com.chatwing.whitelabel.events;


import com.chatwing.whitelabel.pojos.ChatBox;

/**
 * Created by nguyenthanhhuy on 1/3/14.
 */
public class RequestOpenChatBoxEvent {
    private ChatBox mChatBox;

    public RequestOpenChatBoxEvent(ChatBox chatBox) {
        mChatBox = chatBox;
    }

    public ChatBox getChatBox() {
        return mChatBox;
    }
}
