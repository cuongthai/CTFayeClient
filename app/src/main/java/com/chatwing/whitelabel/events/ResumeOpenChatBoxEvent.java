package com.chatwing.whitelabel.events;


import com.chatwing.whitelabel.pojos.ChatBox;

/**
 * Created by nguyenthanhhuy on 1/3/14.
 */
public class ResumeOpenChatBoxEvent {
    private ChatBox mChatBox;

    public ResumeOpenChatBoxEvent(ChatBox chatBox) {
        mChatBox = chatBox;
    }

    public ChatBox getChatBox() {
        return mChatBox;
    }
}
