package com.chatwing.whitelabel.managers;

import android.content.Context;

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
}
