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

import android.os.Handler;

import com.chatwing.whitelabel.events.AllSyncsCompletedEvent;
import com.chatwing.whitelabel.services.BaseIntentService;
import com.chatwing.whitelabel.utils.LogUtils;
import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by steve on 16/07/2014.
 */
public class SyncManager {
    private Bus mBus;
    private Handler mHandler;
    private boolean needReload; //true if the app trigger sync

    private static Object lock = new Object();

    private List<Class<? extends BaseIntentService>> mPendingServices;

    public SyncManager(Bus bus, Handler handler) {
        mPendingServices = new ArrayList<Class<? extends BaseIntentService>>();
        mBus = bus;
        mHandler = handler;
    }

    public void setNeedReload(boolean needReload) {
        this.needReload = needReload;
    }

    public void removeServiceFromQueue(BaseIntentService intentService) {
        synchronized (lock) {
            LogUtils.v("Queue inspector: Remove, queue size = " + mPendingServices.size() + ":" + intentService.getClass());
            mPendingServices.remove(intentService.getClass());
            int count = mPendingServices.size();
            if (count == 0) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mBus.post(new AllSyncsCompletedEvent(needReload));
                    }
                });
            }
        }
    }

    public void addToQueue(Class<? extends BaseIntentService> intentServiceClass) {
        synchronized (lock) {
            mPendingServices.add(intentServiceClass);
            LogUtils.v("Queue inspector: Add to queue size = " + mPendingServices.size());
        }
    }

    public void resetQueue() {
        LogUtils.v("Queue inspector: Clear");
        mPendingServices.clear();
        needReload = false;
    }
}
