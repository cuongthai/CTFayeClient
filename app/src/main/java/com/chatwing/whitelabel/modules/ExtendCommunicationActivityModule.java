package com.chatwing.whitelabel.modules;

import com.chatwing.whitelabel.activities.ExtendCommunicationActivity;
import com.chatwing.whitelabel.fragments.AccountDialogFragment;
import com.chatwing.whitelabel.fragments.BlockUserDialogFragment;
import com.chatwing.whitelabel.fragments.BookmarkedChatBoxesDrawerFragment;
import com.chatwing.whitelabel.fragments.ExtendChatMessagesFragment;
import com.chatwing.whitelabel.fragments.ExtendCommunicationDrawerFragment;
import com.chatwing.whitelabel.fragments.OnlineUsersFragment;
import com.chatwing.whitelabel.fragments.PhotoPickerDialogFragment;
import com.chatwing.whitelabel.interfaces.WLJSInterfaceImpl;
import com.chatwing.whitelabel.managers.ApiManager;
import com.chatwing.whitelabel.managers.ExtendChatBoxModeManager;
import com.chatwing.whitelabel.managers.ExtendConversationModeManager;
import com.chatwing.whitelabel.managers.ExtendCurrentChatboxManager;
import com.chatwing.whitelabel.managers.WLApiManagerImpl;
import com.chatwing.whitelabel.tasks.LoadOnlineUsersTask;
import com.chatwingsdk.activities.CommunicationActivity;
import com.chatwingsdk.interfaces.ChatWingJavaDelegate;
import com.chatwingsdk.interfaces.JSInterfaceImpl;
import com.chatwingsdk.managers.ApiManagerImpl;
import com.chatwingsdk.managers.ChatboxModeManager;
import com.chatwingsdk.managers.ConversationModeManager;
import com.chatwingsdk.managers.CurrentChatBoxManager;
import com.chatwingsdk.managers.CurrentConversationManager;
import com.chatwingsdk.managers.UserManager;
import com.chatwingsdk.modules.CommunicationActivityModule;
import com.chatwingsdk.validators.ChatBoxIdValidator;
import com.chatwingsdk.validators.ConversationIdValidator;
import com.squareup.otto.Bus;

import javax.inject.Provider;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by steve on 10/12/2014.
 */
@Module(
        injects = {
                ExtendChatMessagesFragment.class,
                ExtendCommunicationActivity.class,
                ExtendCommunicationDrawerFragment.class,
                BookmarkedChatBoxesDrawerFragment.class,
                AccountDialogFragment.class,
                OnlineUsersFragment.class,
                PhotoPickerDialogFragment.class,
                BlockUserDialogFragment.class,
                WLJSInterfaceImpl.class
        },

        addsTo = CommunicationActivityModule.class,
        overrides = true
)
public class ExtendCommunicationActivityModule {
    private ExtendCommunicationActivity mActivity;

    public ExtendCommunicationActivityModule(ExtendCommunicationActivity activity) {
        mActivity = activity;
    }

    @Provides
    @Singleton
    ChatWingJavaDelegate provideChatWingJavaDelegate(WLJSInterfaceImpl jsInterface){
        return jsInterface;
    }

    @Provides
    @Singleton
    ApiManager provideApiManager(WLApiManagerImpl impl) {
        return impl;
    }


    @Provides
    @Singleton
    ChatboxModeManager provideChatboxModeManager(Bus bus,
                                                 UserManager userManager,
                                                 ApiManager apiManager,
                                                 CurrentChatBoxManager currentChatBoxManager) {
        return new ExtendChatBoxModeManager(
                bus,
                mActivity,
                userManager,
                apiManager,
                currentChatBoxManager);
    }

    @Provides
    @Singleton
    ConversationModeManager provideConversationModeManager(Bus bus,
                                                           UserManager userManager,
                                                           CurrentConversationManager currentConversationManager,
                                                           ConversationIdValidator conversationIdValidator) {
        return new ExtendConversationModeManager(
                bus,
                mActivity,
                userManager,
                currentConversationManager,
                conversationIdValidator);
    }

    @Provides
    @Singleton
    CurrentChatBoxManager provideCurrentChatboxManager(Bus bus,
                                                       ChatBoxIdValidator chatBoxIdValidator,
                                                       Provider<LoadOnlineUsersTask> taskProvider) {
        return new ExtendCurrentChatboxManager(mActivity,
                bus,
                chatBoxIdValidator,
                taskProvider);
    }
}
