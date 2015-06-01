package com.chatwing.whitelabel.managers;

import android.content.Context;

import com.chatwing.whitelabel.Constants;
import com.chatwing.whitelabel.R;
import com.chatwingsdk.modules.ForActivity;

import javax.inject.Inject;

/**
 * Created by steve on 19/03/2015.
 */
public class BuildManager {
    @Inject
    @ForActivity
    Context mContext;

    public boolean isSupportedAds() {
        return mContext.getResources().getBoolean(R.bool.show_ads);
    }

    public boolean isOfficialChatWingApp() {
        return mContext.getResources().getBoolean(R.bool.official);
    }

    public boolean isSupportedRegister() {
        return mContext.getResources().getBoolean(R.bool.allow_register);
    }

    public boolean isSupportedRss() {
        return mContext.getResources().getBoolean(R.bool.support_rss);
    }

    public boolean isSupportedMusicBox() {
        return mContext.getResources().getBoolean(R.bool.support_music_box);
    }

    public boolean isCustomLoginType() {
        return Constants.BUILD_LOGIN_TYPE.equals(mContext.getResources().getString(R.string.build_login_type));
    }


}
