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

package com.chatwing.whitelabel.tasks;

import android.os.AsyncTask;

import com.chatwing.whitelabel.events.TaskFinishedEvent;
import com.squareup.otto.Bus;

/**
 * Author: Huy Nguyen
 * Date: 4/21/13
 * Time: 11:29 AM
 */
public abstract class CallbackTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

    private Bus mBus;
    private Exception mException;

    protected CallbackTask(Bus bus) {
        this.mBus = bus;
    }

    protected abstract Result run(Params... params) throws Exception;

    @Override
    protected Result doInBackground(Params... params) {
        try {
            return run(params);
        } catch (Exception ex) {
            mException = ex;
            return null;
        }
    }

    @Override
    protected void onPostExecute(Result result) {
        super.onPostExecute(result);
        if (!isCancelled()) {
            if (mException != null) {
                mBus.post(TaskFinishedEvent.failedEvent(this, mException));
            } else {
                mBus.post(TaskFinishedEvent.succeedEvent(this, result));
            }
        }
    }
}
