package com.chatwing.whitelabel.modules;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;


import com.chatwing.whitelabel.activities.CreateChatBoxActivity;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by nguyenthanhhuy on 4/15/14.
 */
@Module(
        injects = {
                CreateChatBoxActivity.class
        },
        addsTo = ChatWingModule.class
)
public class CreateChatBoxActivityModule {
    private Activity mActivity;

    public CreateChatBoxActivityModule(Activity activity) {
        this.mActivity = activity;
    }

    @Provides
    @Singleton
    Resources provideResources() {
        return mActivity.getResources();
    }
}
