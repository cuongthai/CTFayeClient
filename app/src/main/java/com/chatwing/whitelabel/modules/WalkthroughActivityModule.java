package com.chatwing.whitelabel.modules;

import android.content.res.Resources;
import android.support.v4.app.FragmentManager;


import com.chatwing.whitelabel.activities.WalkthroughActivity;
import com.chatwing.whitelabel.timers.SafeCountDownTimer;
import com.chatwingsdk.modules.ChatWingModule;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by nguyenthanhhuy on 4/3/14
 */
@Module(
        injects = WalkthroughActivity.class,
        addsTo = ExtendChatWingModule.class
)
public class WalkthroughActivityModule {
    private WalkthroughActivity mActivity;

    public WalkthroughActivityModule(WalkthroughActivity activity) {
        mActivity = activity;
    }

    @Provides
    @Singleton
    FragmentManager provideFragmentManager() {
        return mActivity.getSupportFragmentManager();
    }

    @Provides
    @Singleton
    Resources provideResources() {
        return mActivity.getResources();
    }

    @Provides
    SafeCountDownTimer provideSafeCountDownTimer() {
        return new SafeCountDownTimer(
                WalkthroughActivity.AUTO_SCROLL_TOTAL_TIME,
                WalkthroughActivity.AUTO_SCROLL_INTERVAL);
    }
}
