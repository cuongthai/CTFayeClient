package com.chatwing.whitelabel.modules;

import com.chatwing.whitelabel.managers.ApiManager;
import com.chatwing.whitelabel.managers.WLApiManagerImpl;
import com.chatwing.whitelabel.services.UpdateUserProfileService;
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
