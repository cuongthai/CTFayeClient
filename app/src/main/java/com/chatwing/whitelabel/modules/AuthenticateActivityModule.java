/*
 * Copyright (C) 2014 ChatWing
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chatwing.whitelabel.modules;

import android.content.Context;
import android.content.res.Resources;


import com.chatwing.whitelabel.activities.AuthenticateActivity;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;


/**
 * Created by nguyenthanhhuy on 3/19/14.
 */
@Module(
        injects = {
                AuthenticateActivity.class,
        },
        addsTo = ChatWingModule.class
)
public class AuthenticateActivityModule {
    private AuthenticateActivity mActivity;

    public AuthenticateActivityModule(AuthenticateActivity activity) {
        this.mActivity = activity;
    }

    @Provides
    @Singleton
    Resources provideResources() {
        return mActivity.getResources();
    }

}
