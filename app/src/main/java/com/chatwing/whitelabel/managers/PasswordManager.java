package com.chatwing.whitelabel.managers;

import android.content.Context;

import com.chatwing.whitelabel.R;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by steve on 11/06/2014.
 */
public class PasswordManager extends PreferenceManager {

    public PasswordManager(Context context) {
        super(context);
    }

    public void rememberChatBoxPassword(String key) {
        Set<String> chatboxes = getStringSet(R.string.preference_chat_boxes_password_remembered, new HashSet<String>());
        chatboxes.add(key);
        setStringSet(R.string.preference_chat_boxes_password_remembered, chatboxes);
    }

    public boolean isRememberPassword(String key) {
        Set<String> chatboxes = getStringSet(R.string.preference_chat_boxes_password_remembered, new HashSet<String>());
        return chatboxes.contains(key);
    }

    public void resetRememberedPassword() {
        setStringSet(R.string.preference_chat_boxes_password_remembered, new HashSet<String>());
    }
}
