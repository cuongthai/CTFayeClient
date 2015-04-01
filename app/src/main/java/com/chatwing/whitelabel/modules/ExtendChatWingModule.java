package com.chatwing.whitelabel.modules;

import android.app.SearchManager;
import android.content.Context;

import com.chatwing.whitelabel.managers.ApiManager;
import com.chatwing.whitelabel.managers.WLApiManagerImpl;
import com.chatwing.whitelabel.services.BlockUserIntentService;
import com.chatwing.whitelabel.services.CreateBookmarkIntentService;
import com.chatwing.whitelabel.services.DeleteBookmarkIntentService;
import com.chatwing.whitelabel.services.DeleteMessageIntentService;
import com.chatwing.whitelabel.services.DownloadUserDetailIntentService;
import com.chatwing.whitelabel.services.FlagMessageIntentService;
import com.chatwing.whitelabel.services.IgnoreUserIntentService;
import com.chatwing.whitelabel.services.SyncBookmarkIntentService;
import com.chatwing.whitelabel.services.UpdateAvatarIntentService;
import com.chatwing.whitelabel.services.UpdateUserProfileService;
import com.chatwing.whitelabel.services.VerifyEmailIntentService;
import com.chatwingsdk.modules.ChatWingModule;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by steve on 17/12/2014.
 */
@Module(
        injects = {
                UpdateUserProfileService.class,
                DeleteMessageIntentService.class,
                BlockUserIntentService.class,
                IgnoreUserIntentService.class,
                FlagMessageIntentService.class,
                DownloadUserDetailIntentService.class,
                SyncBookmarkIntentService.class,
                CreateBookmarkIntentService.class,
                VerifyEmailIntentService.class,
                DeleteBookmarkIntentService.class,
                UpdateAvatarIntentService.class
        },
        addsTo = ChatWingModule.class,
        library = true,
        overrides = true
)
public class ExtendChatWingModule {
    private final Context mApplication;

    public ExtendChatWingModule(Context application) {
        mApplication = application;
    }

    @Provides
    @Singleton
    ApiManager provideApiManager(WLApiManagerImpl impl) {
        return impl;
    }

    @Singleton
    @Provides
    SearchManager provideSearchManager() {
        return (SearchManager) mApplication.getSystemService(Context.SEARCH_SERVICE);
    }

}
