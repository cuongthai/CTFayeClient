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

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Author: Huy Nguyen
 * Date: 4/20/13
 * Time: 2:37 PM
 */
public class NetworkUtils {
    private ConnectivityManager mManager;

    public NetworkUtils(ConnectivityManager manager) {
        mManager = manager;
    }

    public boolean hasInternetConnection() {
        NetworkInfo mActiveNetworkInfo = mManager.getActiveNetworkInfo();
        return (mActiveNetworkInfo != null
                && mActiveNetworkInfo.isAvailable()
                && mActiveNetworkInfo.isConnected());
    }
}
