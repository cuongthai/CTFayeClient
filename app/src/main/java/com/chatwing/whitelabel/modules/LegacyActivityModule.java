package com.chatwing.whitelabel.modules;

import android.accounts.AccountManager;
import android.content.Context;
import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;

import com.chatwing.whitelabel.activities.LegacyLoginActivity;
import com.chatwing.whitelabel.fragments.AuthenticateFragment;
import com.chatwing.whitelabel.fragments.ForgotPasswordFragment;
import com.chatwing.whitelabel.fragments.GuestLoginFragment;
import com.chatwing.whitelabel.fragments.LoginFragment;
import com.chatwing.whitelabel.fragments.LoginScribeFragment;
import com.chatwing.whitelabel.fragments.LoginTwitterFragment;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;


/**
 * Created by cuongthai on 15/10/2014.
 */
@Module(
        injects = {
                LegacyLoginActivity.class,
                AuthenticateFragment.class,
                LoginFragment.class,
                LoginTwitterFragment.class,
                GuestLoginFragment.class,
                ForgotPasswordFragment.class,
                LoginScribeFragment.class
        },
        addsTo = ChatWingModule.class,
        overrides = true
)
public class LegacyActivityModule {

    private LegacyLoginActivity mActivity;

    public LegacyActivityModule(LegacyLoginActivity activity) {
        this.mActivity = activity;
    }

    @Provides
    @Singleton
    @ForActivity
    LayoutInflater provideLayoutInflater() {
        return (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Provides
    @Singleton
    Resources provideResources() {
        return mActivity.getResources();
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
    @Singleton
    LoginFragment provideLoginFragment() {
        return new LoginFragment();
    }

    @Provides
    @Singleton
    GoogleApiClient provideGoogleApiClient() {
        return new GoogleApiClient.Builder(mActivity)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .addConnectionCallbacks(mActivity)
                .addOnConnectionFailedListener(mActivity)
                .build();
    }

    @Provides
    @Singleton
    String[] provideAvatars() {
        return new String[]{
                "Aladie.png", "Amie.png"
                , "Andie.png", "Beetie.png", "Benie.png", "Billie.png"
                , "Bobie.png", "Bridie.png", "Castrie.png", "Chegie.png"
                , "Christie.png", "Cindie.png", "Clintie.png", "Cohie.png"
                , "Connie.png", "Corlie.png", "Croftie.png", "Dexie.png"
                , "Dukie.png", "Einie.png", "Elie.png", "Fishie.png"
                , "Fridie.png", "Hookie.png", "Indie.png", "Leeie.png"
                , "Leie.png", "Linkie.png", "Luckie.png", "Lukie.png"
                , "Madie3.png", "Maradie.png", "Powie.png", "Putie.png"
                , "Rockie.png"
        };
    }

    @Provides
    @Singleton
    ForgotPasswordFragment provideForgotPasswordFragment() {
        return new ForgotPasswordFragment();
    }
}
