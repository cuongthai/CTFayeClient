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

package com.chatwing.whitelabel.services;

import android.app.IntentService;
import android.os.Handler;

import com.chatwing.whitelabel.ChatWing;
import com.chatwing.whitelabel.managers.ApiManager;
import com.chatwing.whitelabel.managers.SyncManager;
import com.chatwing.whitelabel.managers.UserManager;
import com.chatwing.whitelabel.modules.ForMainThread;
import com.chatwing.whitelabel.utils.NetworkUtils;
import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.ObjectGraph;

/**
 * Author: Huy Nguyen
 * Date: 7/4/13
 * Time: 2:39 PM
 */
public abstract class BaseIntentService extends IntentService {
    private ObjectGraph mObjectGraph;

    public BaseIntentService(String name) {
        super(name);
        //Auto redelivery intent when process die
        setIntentRedelivery(true);
    }

    @Inject
    @ForMainThread
    protected Handler mHandler;
    @Inject
    protected Bus mBus;
    @Inject
    protected ApiManager mApiManager;
    @Inject
    protected UserManager mUserManager;
    @Inject
    NetworkUtils mNetworkUtils;
    @Inject
    protected SyncManager mSyncManager;

    @Override
    public void onCreate() {
        super.onCreate();
        ObjectGraph chatwingGraph = ChatWing.instance(getApplicationContext()).getChatwingGraph();
        mObjectGraph = chatwingGraph.plus(getModules().toArray());
        // Inject ourselves so subclasses will have dependencies fulfilled when this method returns.
        mObjectGraph.inject(this);
    }

    protected List<Object> getModules() {
        return new ArrayList<Object>();
    }
}
