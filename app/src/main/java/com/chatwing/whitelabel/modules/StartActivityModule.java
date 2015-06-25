package com.chatwing.whitelabel.modules;

import android.content.Context;

import com.chatwing.whitelabel.activities.StartActivity;

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

    public StartActivityModule(StartActivity startActivity) {
        mActivity = startActivity;
    }




}
