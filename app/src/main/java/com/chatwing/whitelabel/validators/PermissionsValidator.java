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

package com.chatwing.whitelabel.validators;


import com.chatwing.whitelabel.pojos.ChatBox;
import com.chatwing.whitelabel.pojos.User;

import javax.inject.Inject;

/**
 * Created by nguyenthanhhuy on 2/20/14.
 */
public class PermissionsValidator {

    public enum Permission {
        DELETE_MESSAGE("allow_delete_message"),
        VIEW_MESSAGE_IP("allow_view_ip"),
        BLOCK_USER("allow_block_user"),
        UNBLOCK_USER("allow_unblock_user"),
        SEND_MESSAGE("allow_send_message");

        private final String text;

        private Permission(final String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    @Inject
    PermissionsValidator() {
    }

    public boolean canCreateChatBox(User user) {
        return user != null
                && user.isSessionValid()
                && user.isChatWing();
    }

    public boolean canBookmark(User user) {
        return user != null
                && user.isSessionValid()
                && user.isChatWing();
    }

    public boolean canSendMessage(User user) {
        return user != null
                && user.isSessionValid();
    }

    public boolean canChangeSettings(User user) {
        return user != null
                && user.isSessionValid()
                && !user.isGuest();
    }

    public boolean canDoConversation(User user) {
        return user != null
                && user.isSessionValid()
                && !user.isGuest();
    }

    /**
     * Determines whether an user has a permission in a chat box or not.
     */
    public boolean hasPermission(User user, ChatBox chatBox, Permission permission) {
        if (chatBox != null && user != null && user.isSessionValid()) {
            if (chatBox.isAdmin()) {
                return true;
            }
            if (chatBox.isModerator() && chatBox.getPermissions().get(permission.toString())) {
                return true;
            }
        }
        return false;
    }
}
