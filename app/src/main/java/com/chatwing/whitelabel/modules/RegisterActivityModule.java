package com.chatwing.whitelabel.modules;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;

import com.chatwing.whitelabel.activities.RegisterActivity;
import com.chatwing.whitelabel.fragments.RegisterFragment;
import com.chatwing.whitelabel.managers.ApiManager;
import com.chatwing.whitelabel.managers.WLApiManagerImpl;
import com.chatwingsdk.modules.AuthenticateActivityModule;
import com.chatwingsdk.modules.ForActivity;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by cuongthai on 27/10/2014.
 */
@Module(
        injects = {
                RegisterActivity.class,
                RegisterFragment.class
        },
        addsTo = AuthenticateActivityModule.class
)
public class RegisterActivityModule {
    private Activity mActivity;

    public RegisterActivityModule(Activity activity) {
        this.mActivity = activity;
    }

    @Provides
    @Singleton
    @ForActivity
    Context provideContext() {
        return mActivity;
    }

    @Provides
    @Singleton
    @ForActivity
    AccountManager provideAccountManager() {
        return AccountManager.get(mActivity);
    }

    @Provides
    ApiManager provideApiManager(WLApiManagerImpl impl) {
        return impl;
    }
}
