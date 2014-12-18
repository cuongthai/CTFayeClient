package com.chatwing.whitelabel.modules;

import android.app.Activity;

import com.chatwing.whitelabel.activities.MainPreferenceActivity;
import com.chatwing.whitelabel.fragments.SettingsFragment;
import com.chatwingsdk.modules.ChatWingModule;

import dagger.Module;

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
