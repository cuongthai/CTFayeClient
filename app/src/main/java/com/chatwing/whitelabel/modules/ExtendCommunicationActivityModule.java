package com.chatwing.whitelabel.modules;

import com.chatwing.whitelabel.activities.ExtendCommunicationActivity;
import com.chatwing.whitelabel.fragments.AccountDialogFragment;
import com.chatwing.whitelabel.fragments.BlockUserDialogFragment;
import com.chatwing.whitelabel.fragments.BookmarkedChatBoxesDrawerFragment;
import com.chatwing.whitelabel.fragments.ExtendChatMessagesFragment;
import com.chatwing.whitelabel.fragments.ExtendCommunicationDrawerFragment;
import com.chatwing.whitelabel.fragments.FeedDrawerFragment;
import com.chatwing.whitelabel.fragments.FeedFragment;
import com.chatwing.whitelabel.fragments.MusicDrawerFragment;
import com.chatwing.whitelabel.fragments.MusicFragment;
import com.chatwing.whitelabel.fragments.OnlineUsersFragment;
import com.chatwing.whitelabel.fragments.PhotoPickerDialogFragment;
import com.chatwing.whitelabel.interfaces.ChatWingJavaDelegate;
import com.chatwing.whitelabel.interfaces.WLJSInterfaceImpl;
import com.chatwing.whitelabel.managers.ApiManager;
import com.chatwing.whitelabel.managers.ApiManagerImpl;
import com.chatwing.whitelabel.managers.BuildManager;
import com.chatwing.whitelabel.managers.ChatboxModeManager;
import com.chatwing.whitelabel.managers.CommunicationActivityManager;
import com.chatwing.whitelabel.managers.ConversationModeManager;
import com.chatwing.whitelabel.managers.CurrentChatBoxManager;
import com.chatwing.whitelabel.managers.CurrentConversationManager;
import com.chatwing.whitelabel.managers.ExtendChatBoxModeManager;
import com.chatwing.whitelabel.managers.ExtendConversationModeManager;
import com.chatwing.whitelabel.managers.ExtendCurrentChatboxManager;
import com.chatwing.whitelabel.managers.FeedModeManager;
import com.chatwing.whitelabel.managers.MusicModeManager;
import com.chatwing.whitelabel.managers.PasswordManager;
import com.chatwing.whitelabel.managers.UserManager;
import com.chatwing.whitelabel.tasks.LoadOnlineUsersTask;
import com.chatwing.whitelabel.validators.ChatBoxIdValidator;
import com.chatwing.whitelabel.validators.ConversationIdValidator;
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
                WLJSInterfaceImpl.class,
                FeedDrawerFragment.class,
                FeedFragment.class,
                MusicFragment.class,
                MusicDrawerFragment.class,
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
    ApiManager provideApiManager(ApiManagerImpl impl) {
        return impl;
    }


    @Provides
    @Singleton
    ChatboxModeManager provideChatboxModeManager(Bus bus,
                                                 UserManager userManager,
                                                 ApiManager apiManager,
                                                 CurrentChatBoxManager currentChatBoxManager,
                                                 BuildManager buildManager,
                                                 ChatBoxIdValidator chatBoxIdValidator,
                                                 CommunicationActivityManager communicationActivityManager) {
        return new ExtendChatBoxModeManager(
                bus,
                mActivity,
                mActivity,
                userManager,
                apiManager,
                currentChatBoxManager,
                buildManager,
                chatBoxIdValidator,
                communicationActivityManager);
    }

    @Provides
    @Singleton
    ConversationModeManager provideConversationModeManager(Bus bus,
                                                           UserManager userManager,
                                                           CurrentConversationManager currentConversationManager,
                                                           ConversationIdValidator conversationIdValidator,
                                                           CommunicationActivityManager communicationActivityManager) {
        return new ExtendConversationModeManager(
                bus,
                mActivity,
                userManager,
                currentConversationManager,
                conversationIdValidator,
                communicationActivityManager);
    }

    @Provides
    @Singleton
    FeedModeManager provideFeedModeManager(Bus bus,
                                                 UserManager userManager,
                                                 CommunicationActivityManager communicationActivityManager) {
        return new FeedModeManager(
                bus,
                mActivity,
                userManager,
                communicationActivityManager);
    }

    @Provides
    @Singleton
    MusicModeManager provideMusicModeManager(Bus bus,
                                           UserManager userManager,
                                           CommunicationActivityManager communicationActivityManager) {
        return new MusicModeManager(
                bus,
                mActivity,
                userManager,
                communicationActivityManager);
    }


    @Provides
    @Singleton
    CurrentChatBoxManager provideCurrentChatboxManager(Bus bus,
                                                       ChatBoxIdValidator chatBoxIdValidator,
                                                       Provider<LoadOnlineUsersTask> taskProvider,
                                                       PasswordManager passwordManager) {
        return new ExtendCurrentChatboxManager(mActivity,
                bus,
                chatBoxIdValidator,
                taskProvider,
                passwordManager);
    }


}
