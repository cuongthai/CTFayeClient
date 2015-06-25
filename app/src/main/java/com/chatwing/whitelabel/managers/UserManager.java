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

package com.chatwing.whitelabel.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.TextUtils;
import android.text.format.DateUtils;

import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.events.PingEvent;
import com.chatwing.whitelabel.events.TaskFinishedEvent;
import com.chatwing.whitelabel.parsers.BBCodePair;
import com.chatwing.whitelabel.pojos.BaseUser;
import com.chatwing.whitelabel.pojos.ChatBox;
import com.chatwing.whitelabel.pojos.IgnoreUser;
import com.chatwing.whitelabel.pojos.User;
import com.chatwing.whitelabel.pojos.UserProfile;
import com.chatwing.whitelabel.pojos.responses.ChatBoxDetailsResponse;
import com.chatwing.whitelabel.tasks.PingUserTask;
import com.chatwing.whitelabel.utils.SharedPrefUtils;
import com.chatwing.whitelabel.validators.PermissionsValidator;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Provider;

/**
 * Author: Huy Nguyen
 * Date: 3/26/13
 * Time: 11:09 PM
 */
@SuppressWarnings("FieldCanBeLocal")
public class UserManager extends PreferenceManager {
    private static final int MSG_PING = 1;
    private final Bus mBus;
    private Provider<PingUserTask> mPingUserProvider;
    private PingUserTask mPingTask;

    private PermissionsValidator mPermissionsValidator;
    private Map<String, User> mUsers;
    private User mCurrentUser;
    protected Handler mPingHandler;
    private static final long PING_INTERVAL = 60 * DateUtils.SECOND_IN_MILLIS;

    private static Type gsonTypeToken = new TypeToken<Map<String, User>>() {
    }.getType();

    public UserManager(Context context,
                       Bus bus,
                       PermissionsValidator permissionsValidator,
                       Provider<PingUserTask> pingUserProvider) {
        super(context);
        mPermissionsValidator = permissionsValidator;
        mPingUserProvider = pingUserProvider;
        mBus = bus;
        loadUsers();
        loadCurrentUser();
    }

    public void onResume(){
        mBus.register(this);
    }

    public void onPause(){
        mBus.unregister(this);
        if (mPingHandler != null) {
            mPingHandler.removeMessages(MSG_PING);
        }
    }


    //////////////////////////////////////////////////////////
    // Users
    //////////////////////////////////////////////////////////
    private void loadUsers() {
        String usersJson = getString(R.string.preference_users, null);
        if (!TextUtils.isEmpty(usersJson)) {
            mUsers = new Gson().fromJson(usersJson, gsonTypeToken);
        } else {
            mUsers = new HashMap<String, User>();
        }
    }

    private void loadCurrentUser() {
        String currentUserId = getString(R.string.preference_current_user_id, null);
        if (mUsers != null && currentUserId != null) {
            if (mUsers.containsKey(currentUserId)) {
                mCurrentUser = mUsers.get(currentUserId);
                mCurrentUser.setProfile(loadCurrentUserProfile());
            }
        }
    }

    /**
     * Requests to invalidate current cached user.
     * This is needed when some changes have been made directly
     * via {@link SharedPreferences}
     * instead of via {@link #addUser(com.chatwing.whitelabel.pojos.User)}.
     */
    public void invalidateUser() {
        mCurrentUser = null;
        loadUsers();
        loadCurrentUser();
    }

    public User getCurrentUser() {
        return mCurrentUser;
    }

    /**
     * Set user to our account db. Duplication will be removed
     *
     * @param user
     */
    public void addUser(User user) {
        if (user == null) return;

        loadUsers();
        mUsers.put(user.getId(), user);
        String userJson = new Gson().toJson(mUsers, gsonTypeToken);
        setString(R.string.preference_users, userJson);
        storeCurrentUserProfileToPrefs(user.getProfile());
    }

    public User activateUser(String userId)
            throws UserAccountNotFoundException {
        loadUsers();
        if (mUsers.containsKey(userId)) {
            setString(R.string.preference_current_user_id, userId);
            mCurrentUser = mUsers.get(userId);
            storeCurrentUserProfileToPrefs(mCurrentUser.getProfile());
            return mCurrentUser;
        } else {
            throw new UserAccountNotFoundException();
        }
    }

    public void removeUsers() {
        mUsers = null;
        mCurrentUser = null;
        remove(R.string.preference_users);
        removeUserProfile();
        removeStyle();
    }

    public boolean userCanCreateChatBox() {
        return mPermissionsValidator.canCreateChatBox(mCurrentUser);
    }

    public boolean userCanLoadConversations() {
        return mPermissionsValidator.canDoConversation(mCurrentUser);
    }

    public boolean userCanBookmark() {
        return mPermissionsValidator.canBookmark(mCurrentUser);
    }

    public boolean userCanSendMessage() {
        return mPermissionsValidator.canSendMessage(mCurrentUser);
    }

    public boolean userCanChangeSettings() {
        return mPermissionsValidator.canChangeSettings(mCurrentUser);
    }

    /**
     * Determines whether this user has a given permission in a given chat box or not.
     */
    public boolean userHasPermission(ChatBox chatBox, PermissionsValidator.Permission permission) {
        return mPermissionsValidator.hasPermission(mCurrentUser, chatBox, permission);
    }

    //////////////////////////////////////////////////////////
    // User.UserProfile
    //////////////////////////////////////////////////////////
    private UserProfile loadCurrentUserProfile() {
        boolean enableSound = getBoolean(
                R.string.preference_play_new_message_sound,
                R.bool.default_play_new_message_sound);
        boolean scrollToLatestMessage = getBoolean(
                R.string.preference_scroll_to_latest_message,
                R.bool.default_scroll_to_latest_message);
        boolean rememberPreviousStyle = getBoolean(
                R.string.preference_remember_previous_style,
                R.bool.default_remember_previous_style);
        String name = getString(R.string.preference_name, null);
        String username = getString(R.string.preference_username, null);
        String email = getString(R.string.preference_email, null);
        return new UserProfile(enableSound, scrollToLatestMessage,
                rememberPreviousStyle,
                name,
                username,
                email);
    }

    /**
     * We only store one profile to preference
     * since only one activate user is loaded and one set of prefs is activated
     *
     * @param userProfile
     */
    private void storeCurrentUserProfileToPrefs(UserProfile userProfile) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putBoolean(
                getContext().getString(R.string.preference_play_new_message_sound),
                userProfile.isSoundEnabled());
        editor.putBoolean(
                getContext().getString(R.string.preference_scroll_to_latest_message),
                userProfile.shouldScrollToLatestMessage());
        editor.putBoolean(
                getContext().getString(R.string.preference_remember_previous_style),
                userProfile.shouldRememberPreviousStyle());
        editor.putString(
                getContext().getString(R.string.preference_name),
                userProfile.getName());
        editor.putString(
                getContext().getString(R.string.preference_email),
                userProfile.getEmail());
        editor.putString(
                getContext().getString(R.string.preference_username),
                userProfile.getUsername());
        SharedPrefUtils.apply(editor);
    }

    private void removeUserProfile() {
        remove(R.string.preference_play_new_message_sound,
                R.string.preference_scroll_to_latest_message,
                R.string.preference_remember_previous_style,
                R.string.preference_name,
                R.string.preference_username);
    }

    ///////////////////////////////////////////////////////////////////
    // Style
    ///////////////////////////////////////////////////////////////////
    public Set<BBCodePair> getStyle() {
        Set<String> styles = getStringSet(R.string.preference_style, null);
        if (styles == null) {
            return null;
        }
        Set<BBCodePair> pairs = new TreeSet<BBCodePair>();
        for (String s : styles) {
            pairs.add(new BBCodePair(s));
        }
        return pairs;
    }

    public void saveStyle(Set<BBCodePair> pairs) {
        if (pairs == null || pairs.isEmpty()) {
            removeStyle();
            return;
        }
        Set<String> strings = new HashSet<String>();
        for (BBCodePair p : pairs) {
            strings.add(p.toString());
        }
        setStringSet(R.string.preference_style, strings);
    }

    public void removeStyle() {
        remove(R.string.preference_style);
    }

    public boolean shouldScrollToLastMessage() {
        return mCurrentUser == null ? true : mCurrentUser.getProfile().shouldScrollToLatestMessage();
    }

    public boolean isSoundEnabled() {
        return mCurrentUser == null ? true : getCurrentUser().getProfile().isSoundEnabled();
    }

    public boolean shouldRememberPreviousStyle() {
        return mCurrentUser == null ? true : getCurrentUser().getProfile().shouldRememberPreviousStyle();
    }

    public Collection<User> getAllUsers() {
        loadUsers();
        return mUsers.values();
    }

    public void removeUser(User user) {
        if (user == null) return;
        loadUsers();
        mUsers.remove(user.getId());
        String userJson = new Gson().toJson(mUsers, gsonTypeToken);
        setString(R.string.preference_users, userJson);
        removeUserProfile();
    }

    public boolean acceptAccessChatbox(User currentUser,
                                       ChatBoxDetailsResponse.ChatBoxDetailErrorParams chatBoxDetailErrorParams) {
        if (currentUser == null || chatBoxDetailErrorParams == null) {
            return false;
        }
        String loginType = currentUser.getLoginType();
        Map<String, Boolean> methods = chatBoxDetailErrorParams.getAuthenticationMethods();
        if (methods.containsKey(loginType)) {
            return methods.get(loginType);
        }

        return false;
    }

    public boolean hasIgnored(String loginId, String userType) {
        if (mCurrentUser == null) {
            return false;
        }
        ArrayList<IgnoreUser> ignoreList = mCurrentUser.getIgnoreList();
        if (ignoreList == null) return false;
        for (IgnoreUser ignoreUser : ignoreList) {
            if (ignoreUser.getIdentifier().equals(
                    BaseUser.computeIdentifier(loginId, userType))) {
                return true;
            }
        }
        return false;
    }

    public void updateUser(User user, User updatedUser) {
        if (user == null || updatedUser == null) return;
        if (mCurrentUser.getId().equals(user.getId())) {
            updatedUser.setAccessToken(user.getAccessToken());
            addUser(updatedUser);
            mCurrentUser = updatedUser;
        }
    }

    public void saveCurrentUser() {
        if (mCurrentUser != null) {
            String userJson = new Gson().toJson(mUsers, gsonTypeToken);
            setString(R.string.preference_users, userJson);
            storeCurrentUserProfileToPrefs(mCurrentUser.getProfile());
        }
    }

    public boolean isCurrentUser(String userIdentifier) {
        return mCurrentUser.getIdentifier().equals(userIdentifier);
    }

    public void ping() {
        if (mCurrentUser == null) {
            return;
        }
        if (mPingTask != null
                && mPingTask.getStatus() != AsyncTask.Status.FINISHED) {
            // There is a running task, no need to start a new one.
            return;
        }
        mPingTask = mPingUserProvider.get();
        mPingTask.execute();
    }

    @Subscribe
    public void onTaskFinishedEvent(TaskFinishedEvent event) {
        if (event.getTask() != mPingTask) {
            return;
        }
        Exception exception = event.getException();
        if (exception == null) {
            //Use this mechanism to ensure we wont ping when app is paused
            mBus.post(new PingEvent());
        }
    }

    @Subscribe
    public void onPingEvent(PingEvent event) {
        // Also, check and only post new message to the handler if
        // its queue is empty. That ensures that the update interval
        // is correct.
        if (mPingHandler == null) {
            mPingHandler = new Handler() {
                @Override
                public void handleMessage(android.os.Message msg) {
                    ping();
                }
            };
        }
        if (!mPingHandler.hasMessages(MSG_PING)) {
            android.os.Message msg = android.os.Message.obtain(
                    mPingHandler, MSG_PING);
            mPingHandler.sendMessageDelayed(msg,
                    PING_INTERVAL);
        }
    }

    public static class UserAccountNotFoundException extends Exception {
    }
}
