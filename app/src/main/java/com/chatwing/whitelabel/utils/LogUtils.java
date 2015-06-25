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

package com.chatwing.whitelabel.utils;

import android.text.TextUtils;
import android.util.Log;

import com.chatwing.whitelabel.Constants;
import com.crashlytics.android.Crashlytics;

/**
 * Created by cuongthai on 21/07/2014.
 */
public class LogUtils {
    public static void v(String message) {
        if (Constants.DEBUG) {
            Log.v(Constants.CHATWING_SDK_TAG, message);
        }
        Crashlytics.log(message);
    }

    public static void e(String msg) {
        if (TextUtils.isEmpty(msg)) {
            return;
        }
        if (Constants.DEBUG) {
            Log.e(Constants.CHATWING_SDK_TAG, msg);
        }
        Exception e = new Exception(msg);
        Crashlytics.logException(e);
    }

    public static void e(Throwable th) {
        if (Constants.DEBUG) {
            Log.e(Constants.CHATWING_SDK_TAG, th.getMessage(), th);
        }
        Crashlytics.logException(th);

    }
}
