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

package com.chatwing.whitelabel.modules;

import android.app.ProgressDialog;
import android.content.Context;
import android.view.LayoutInflater;

import com.chatwing.whitelabel.activities.CommunicationActivity;
import com.chatwing.whitelabel.fragments.AdminListFragment;
import com.chatwing.whitelabel.fragments.CategoriesFragment;
import com.chatwing.whitelabel.fragments.ChatMessagesFragment;
import com.chatwing.whitelabel.fragments.ChatboxesFragment;
import com.chatwing.whitelabel.fragments.CommunicationDrawerFragment;
import com.chatwing.whitelabel.fragments.ConversationMessagesFragment;
import com.chatwing.whitelabel.fragments.ConversationsFragment;
import com.chatwing.whitelabel.fragments.EmoticonsFragment;
import com.chatwing.whitelabel.fragments.NotificationFragment;
import com.chatwing.whitelabel.fragments.PasswordDialogFragment;
import com.chatwing.whitelabel.fragments.ProfileFragment;
import com.chatwing.whitelabel.interfaces.ChatWingJSInterface;
import com.chatwing.whitelabel.interfaces.ChatWingJavaDelegate;
import com.chatwing.whitelabel.interfaces.JSInterfaceImpl;
import com.chatwing.whitelabel.managers.ApiManager;
import com.chatwing.whitelabel.managers.ChatboxModeManager;
import com.chatwing.whitelabel.managers.CommunicationActivityManager;
import com.chatwing.whitelabel.managers.ConversationModeManager;
import com.chatwing.whitelabel.managers.CurrentChatBoxManager;
import com.chatwing.whitelabel.managers.CurrentConversationManager;
import com.chatwing.whitelabel.managers.PasswordManager;
import com.chatwing.whitelabel.managers.UserManager;
import com.chatwing.whitelabel.parsers.BBCodeParser;
import com.chatwing.whitelabel.parsers.BBCodeParserImpl;
import com.chatwing.whitelabel.parsers.EventParser;
import com.chatwing.whitelabel.parsers.EventParserImpl;
import com.chatwing.whitelabel.validators.ChatBoxIdValidator;
import com.chatwing.whitelabel.validators.ConversationIdValidator;
import com.chatwing.whitelabel.views.BBCodeEditText;
import com.squareup.otto.Bus;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by cuongthai on 13/08/2014.
 */
@Module(
        injects = {
                BBCodeEditText.class,
                ProfileFragment.class,
                ChatMessagesFragment.class,
                ConversationMessagesFragment.class,
                ConversationsFragment.class,
                AdminListFragment.class,
                CommunicationActivity.class,
                CommunicationDrawerFragment.class,
                PasswordDialogFragment.class,
                ChatboxesFragment.class,
                CategoriesFragment.class,
                NotificationFragment.class,
                EmoticonsFragment.class,
                JSInterfaceImpl.class
        },
        addsTo = ChatWingModule.class

)
public class CommunicationActivityModule {
    private CommunicationActivity mActivity;

    public CommunicationActivityModule(CommunicationActivity activity) {
        mActivity = activity;
    }

    @Provides
    @Singleton
    @ForActivity
    Context provideActivityContext() {
        return mActivity;
    }

    @Provides
    @Singleton
    EventParser provideEventParser(EventParserImpl impl) {
        return impl;
    }

    /**
     * Provides layout inflater for this activity.
     * It's different from the one provided by Application
     * because this activity uses a different theme from application theme.
     */
    @Provides
    @Singleton
    @ForActivity
    LayoutInflater provideLayoutInflater() {
        return (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /**
     * Provides implementation of BBCodeParser.
     * It should be a singleton to cache {@link org.kefirsf.bb.TextProcessor}s
     * in {@link com.chatwing.whitelabel.parsers.BBCodeParserImpl#mProcessors}
     * since they are expensive to create.
     */
    @Provides
    @Singleton
    BBCodeParser provideBBCodeParser(BBCodeParserImpl impl) {
        return impl;
    }

    @Provides
    @Singleton
    ChatboxModeManager provideChatboxModeManager(Bus bus,
                                                 UserManager userManager,
                                                 ApiManager apiManager,
                                                 CurrentChatBoxManager currentChatBoxManager,
                                                 ChatBoxIdValidator chatBoxIdValidator,
                                                 CommunicationActivityManager communicationActivityManager) {
        return new ChatboxModeManager(
                bus,
                mActivity,
                userManager,
                apiManager,
                currentChatBoxManager,
                chatBoxIdValidator,
                communicationActivityManager);
    }

    @Provides
    @Singleton
    ChatWingJavaDelegate provideChatWingJavaDelegate(JSInterfaceImpl jsInterface){
        return jsInterface;
    }

    @Provides
    @Singleton
    ChatWingJSInterface provideChatWingJSInterface(ChatWingJavaDelegate delegate) {
        return new ChatWingJSInterface(delegate);
    }

    @Provides
    @Singleton
    ConversationModeManager provideConversationModeManager(Bus bus,
                                                           UserManager userManager,
                                                           CurrentConversationManager currentConversationManager,
                                                           ConversationIdValidator conversationIdValidator,
                                                           CommunicationActivityManager communicationActivityManager) {
        return new ConversationModeManager(
                bus,
                mActivity,
                userManager,
                currentConversationManager,
                conversationIdValidator,
                communicationActivityManager);
    }

    @Provides
    @Singleton
    CurrentChatBoxManager provideCurrentChatboxManager(Bus bus,
                                                       ChatBoxIdValidator chatBoxIdValidator,
                                                       PasswordManager passwordManager) {
        return new CurrentChatBoxManager(mActivity, bus, chatBoxIdValidator, passwordManager);
    }

    @Provides
    @Singleton
    CommunicationActivityManager provideCommunicationActivityManager(){
        return new CommunicationActivityManager(mActivity);
    }

    @Provides
    @Singleton
    CurrentConversationManager provideCurrentConversationManager(Bus bus,
                                                                 ConversationIdValidator conversationIdValidator) {
        return new CurrentConversationManager(
                mActivity,
                bus,
                mActivity.getSupportLoaderManager(),
                conversationIdValidator);
    }

    @Provides
    @Singleton
    ProgressDialog provideProgressDialog(){
        ProgressDialog progressDialog = new ProgressDialog(mActivity);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        return progressDialog;
    }

    @Provides
    @Singleton
    PasswordManager providePasswordManager() {
        return new PasswordManager(mActivity);
    }
}

