package com.chatwing.whitelabel.modules;

import com.chatwing.whitelabel.activities.StartActivity;
import com.chatwingsdk.modules.ChatWingModule;

import dagger.Module;

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
