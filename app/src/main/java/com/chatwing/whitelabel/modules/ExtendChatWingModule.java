package com.chatwing.whitelabel.modules;

import com.chatwing.whitelabel.managers.ApiManager;
import com.chatwing.whitelabel.managers.WLApiManagerImpl;
import com.chatwing.whitelabel.services.BlockUserIntentService;
import com.chatwing.whitelabel.services.DeleteMessageIntentService;
import com.chatwing.whitelabel.services.DownloadUserDetailIntentService;
import com.chatwing.whitelabel.services.IgnoreUserIntentService;
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
                DownloadUserDetailIntentService.class,
                VerifyEmailIntentService.class,
                UpdateAvatarIntentService.class
        },
        addsTo = ChatWingModule.class,
        overrides = true
)
public class ExtendChatWingModule {

    @Provides
    @Singleton
    ApiManager provideApiManager(WLApiManagerImpl impl) {
        return impl;
    }

}
