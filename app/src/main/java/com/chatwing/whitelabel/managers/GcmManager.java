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
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.modules.ForApplication;
import com.chatwing.whitelabel.utils.ContextUtils;
import com.chatwing.whitelabel.utils.LogUtils;
import com.chatwing.whitelabel.utils.SharedPrefUtils;

import javax.inject.Inject;


/**
 * Author: Huy Nguyen
 * Date: 9/11/13
 * Time: 11:54 AM
 */
public class GcmManager extends PreferenceManager {
    @Inject
    GcmManager(@ForApplication Context context) {
        super(context);
    }

    public String getRegistrationId() {
        String registrationId = getString(R.string.preference_gcm_registration_id, "");
        if (TextUtils.isEmpty(registrationId)) {
            return "";
        }

        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = getInt(R.string.preference_gcm_app_version, Integer.MIN_VALUE);
        int currentVersion = ContextUtils.getAppVersion(getContext());
        if (registeredVersion != currentVersion) {
            LogUtils.v("App version changed.");
            return "";
        }

        return registrationId;
    }

    public void setRegistrationId(String registrationId) {
        SharedPreferences.Editor editor = getSharedPreferences().edit();
        editor.putString(
                getContext().getString(R.string.preference_gcm_registration_id),
                registrationId);
        editor.putInt(
                getContext().getString(R.string.preference_gcm_app_version),
                ContextUtils.getAppVersion(getContext()));
        SharedPrefUtils.apply(editor);
    }

    public void clearRegistrationId() {
        remove(R.string.preference_gcm_registration_id, R.string.preference_gcm_app_version);
    }
}
