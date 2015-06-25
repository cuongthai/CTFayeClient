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

package com.chatwing.whitelabel;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.chatwing.whitelabel.activities.CommunicationActivity;
import com.chatwing.whitelabel.contentproviders.ChatWingContentProvider;
import com.chatwing.whitelabel.events.UnreadMessagesEvent;
import com.chatwing.whitelabel.interfaces.JSInterfaceImpl;
import com.chatwing.whitelabel.managers.UserManager;
import com.chatwing.whitelabel.modules.ChatWingModule;
import com.chatwing.whitelabel.modules.ForMainThread;
import com.chatwing.whitelabel.services.SyncCommunicationBoxesIntentService;
import com.squareup.otto.Bus;

import javax.inject.Inject;

import dagger.ObjectGraph;

/**
 * Created by cuongthai on 19/07/2014.
 */
public class ChatWing {
    private static ChatWing chatWing;
    private static boolean isDebugging = true;
    private final Context mContext;

    private ObjectGraph mChatwingGraph;
    @Inject
    @ForMainThread
    protected Handler mHandler;
    @Inject
    Bus mBus;
    @Inject
    UserManager mUserManager;

    private static String mAppId;
    private static String mAppSecret;
    private static String mClientId;

    private String[] mChatboxIds;
    private Class<? extends Activity> mAuthenticationEntranceClass;
    private JSInterfaceImpl mJSDelegate;
    private Class<? extends CommunicationActivity> mainActivityClass;

    public void loadUnreadCount() {
        if (mUserManager.getCurrentUser() != null) {
            int count = ChatWingContentProvider.countUnreadMessagesInConversations(mContext.getContentResolver());
            post(new UnreadMessagesEvent(count));
            mContext.startService(new Intent(mContext, SyncCommunicationBoxesIntentService.class));
        }
    }

    private void post(final UnreadMessagesEvent unreadMessagesEvent) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mBus.post(unreadMessagesEvent);
            }
        });
    }

    public static void initialize(Context context,
                                  String appId,
                                  String appSecret,
                                  String[] chatboxIds,
                                  Class<? extends Activity> authenticateEntranceActivityClass) {
        ChatWing instance = instance(context);
        instance.mAppId = appId;
        instance.mAppSecret = appSecret;
        instance.mClientId = "android";
        instance.mChatboxIds = chatboxIds;
        instance.mAuthenticationEntranceClass = authenticateEntranceActivityClass;
    }

    public static void setIsDebugging(boolean isDebugging) {
        ChatWing.isDebugging = isDebugging;
    }

    public static ChatWing instance(Context context) {
        if (chatWing == null) {
            chatWing = new ChatWing(context);
        }
        return chatWing;
    }

    private ChatWing(Context context) {
        mContext = context;
        mChatwingGraph = ObjectGraph.create(new ChatWingModule(context));
        mChatwingGraph.inject(this);
    }

    public static void destroy() {
        chatWing = null;
    }

    public static String getAppId() {
        if (mAppId == null) {
            throw new ExceptionInInitializerError("You need to initialize ChatWing first");
        }
        return mAppId;
    }

    public static String getClientID() {
        if (mClientId == null) {
            throw new ExceptionInInitializerError("You need to initialize ChatWing first");
        }
        return mClientId;
    }

    public static String getAppSecret() {
        if (mAppSecret == null) {
            throw new ExceptionInInitializerError("You need to initialize client_secret");
        }
        return mAppSecret;
    }

    public static void inject(Context context, Object obj) {
        instance(context).mChatwingGraph.inject(obj);
    }

    public ObjectGraph getChatwingGraph() {
        return mChatwingGraph;
    }

    public Bus getBus() {
        return mBus;
    }

    public Class<? extends Activity> getAuthenticationClass() {
        return mAuthenticationEntranceClass;
    }

    public static boolean isDebugging() {
        return isDebugging;
    }

    public Class<? extends CommunicationActivity> getMainActivityClass() {
        if (mainActivityClass == null) {
            mainActivityClass = CommunicationActivity.class;
        }
        return mainActivityClass;
    }

    public void setMainActivityClass(Class<? extends CommunicationActivity> mainActivityClass) {
        this.mainActivityClass = mainActivityClass;
    }
}
