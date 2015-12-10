package com.chatwing.whitelabel.modules;

import com.chatwing.whitelabel.activities.LegacyLoginActivity;
import com.chatwing.whitelabel.activities.PhotoViewerActivity;
import com.chatwing.whitelabel.fragments.AuthenticateFragment;
import com.chatwing.whitelabel.fragments.ForgotPasswordFragment;
import com.chatwing.whitelabel.fragments.GuestLoginFragment;
import com.chatwing.whitelabel.fragments.LoginFragment;
import com.chatwing.whitelabel.fragments.LoginScribeFragment;
import com.chatwing.whitelabel.fragments.LoginTwitterFragment;

import dagger.Module;

/**
 * Created by cuongthai on 11/16/15.
 */
@Module(
        injects = {
                PhotoViewerActivity.class
        },
        addsTo = ChatWingModule.class,
        overrides = true
)
public class PhotoViewerModule {
    private PhotoViewerActivity mActivity;

    public PhotoViewerModule(PhotoViewerActivity activity) {
        this.mActivity = activity;
    }
}
