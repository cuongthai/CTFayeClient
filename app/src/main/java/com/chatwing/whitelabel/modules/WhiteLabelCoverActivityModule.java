package com.chatwing.whitelabel.modules;

import com.chatwing.whitelabel.activities.WhiteLabelCoverActivity;

import dagger.Module;

/**
 * Created by steve on 30/12/2014.
 */
@Module(
        injects = {
                WhiteLabelCoverActivity.class
        },
        addsTo = ChatWingModule.class
)
public class WhiteLabelCoverActivityModule {

    private final WhiteLabelCoverActivity mActivity;

    public WhiteLabelCoverActivityModule(WhiteLabelCoverActivity whiteLabelCoverActivity) {
        mActivity = whiteLabelCoverActivity;
    }
}
