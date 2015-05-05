package com.chatwing.whitelabel.modules;

import android.content.Context;

import com.chatwing.whitelabel.activities.StartActivity;
import com.chatwingsdk.modules.ChatWingModule;
import com.chatwingsdk.modules.ForActivity;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by steve on 30/12/2014.
 */
@Module(
        injects = {
                StartActivity.class
        },
        addsTo = ChatWingModule.class
)
public class StartActivityModule {

    private final StartActivity mActivity;

    @Provides
    @Singleton
    @ForActivity
    Context provideContext() {
        return mActivity;
    }

    public StartActivityModule(StartActivity startActivity) {
        mActivity = startActivity;
    }


}
