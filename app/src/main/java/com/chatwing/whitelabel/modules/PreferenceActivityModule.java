package com.chatwing.whitelabel.modules;

import android.app.Activity;
import android.content.Context;

import com.chatwing.whitelabel.activities.MainPreferenceActivity;
import com.chatwing.whitelabel.fragments.SettingsFragment;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by steve on 17/12/2014.
 */
@Module(
        injects = {
                MainPreferenceActivity.class,
                SettingsFragment.class
        },
        addsTo = ChatWingModule.class
)
public class PreferenceActivityModule {
    private Activity mActivity;

    public PreferenceActivityModule(Activity activity) {
        this.mActivity = activity;
    }

}
