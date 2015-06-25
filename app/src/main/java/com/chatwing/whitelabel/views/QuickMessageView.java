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

package com.chatwing.whitelabel.views;

import android.content.Context;
import android.widget.Toast;

import com.chatwing.whitelabel.modules.ForApplication;

import javax.inject.Inject;

/**
 * Created by nguyenthanhhuy on 4/17/14.
 */
public class QuickMessageView {
    protected Context mContext;

    @Inject
    QuickMessageView(@ForApplication Context context) {
        mContext = context;
    }

    public void show(String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
    }

    public void show(int resId) {
        Toast.makeText(mContext, resId, Toast.LENGTH_LONG).show();
    }
}
