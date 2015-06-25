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

package com.chatwing.whitelabel.managers;

import android.content.Context;

import com.squareup.otto.Bus;

/**
 * Created by cuongthai on 18/08/2014.
 */
public abstract class CurrentCommunicationManager {
    protected Bus mBus;
    Context mContext;

    public CurrentCommunicationManager(Context context,
                                       Bus bus) {
        mContext = context;
        mBus = bus;
    }

    /**
     * Expected to be called by the hosted {@link android.app.Activity}
     * or {@link android.app.Fragment} in its lifecycle.
     */
    public void onResume() {
        mBus.register(this);
    }

    /**
     * Expected to be called by the host {@link android.app.Activity}
     * or {@link android.app.Fragment} in its lifecycle.
     * Background tasks will be stopped here. So when the host object
     * resumes, it will need to manually resume neccessary tasks.
     */
    public void onPause() {
        mBus.unregister(this);
    }

    public abstract void onDestroy();
}
