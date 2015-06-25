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
import android.os.Build;

import com.chatwing.whitelabel.utils.SharedPrefUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Set;

/**
 * Author: Huy Nguyen
 * Date: 6/22/13
 * Time: 11:22 AM
 */
public abstract class PreferenceManager {
    private Context mContext;
    private SharedPreferences mSharedPreferences;

    public PreferenceManager(Context context) {
        mContext = context;
        mSharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
    }

    public Context getContext() {
        return mContext;
    }

    public SharedPreferences getSharedPreferences() {
        return mSharedPreferences;
    }

    public boolean getBoolean(int keyResId, int defaultValueResId) {
        boolean defaultValue = mContext.getResources().getBoolean(defaultValueResId);
        return getBoolean(keyResId, defaultValue);
    }

    public boolean getBoolean(int keyResId, boolean defaultValue) {
        String key = mContext.getString(keyResId);
        return mSharedPreferences.getBoolean(key, defaultValue);
    }

    public void setBoolean(int keyResId, boolean value) {
        String key = mContext.getString(keyResId);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(key, value);
        SharedPrefUtils.apply(editor);
    }

    public int getInt(int keyResId, int defaultValue) {
        String key = mContext.getString(keyResId);
        return mSharedPreferences.getInt(key, defaultValue);
    }

    public long getLong(int keyResId, long defaultValue) {
        String key = mContext.getString(keyResId);
        return mSharedPreferences.getLong(key, defaultValue);
    }

    public String getString(int keyResId, String defaultValue) {
        String key = mContext.getString(keyResId);
        return mSharedPreferences.getString(key, defaultValue);
    }

    public void setInt(int keyResId, int value) {
        String key = mContext.getString(keyResId);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(key, value);
        SharedPrefUtils.apply(editor);
    }

    public void setLong(int keyResId, long value) {
        String key = mContext.getString(keyResId);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putLong(key, value);
        SharedPrefUtils.apply(editor);
    }

    public void setString(int keyResId, String value) {
        String key = mContext.getString(keyResId);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(key, value);
        SharedPrefUtils.apply(editor);
    }

    public Set<String> getStringSet(int keyResId, Set<String> defValues) {
        String key = mContext.getString(keyResId);
        if (Build.VERSION.SDK_INT >= 11) {
            return mSharedPreferences.getStringSet(key, defValues);
        } else {
            String jsonValue = mSharedPreferences.getString(key, null);
            if (jsonValue == null) {
                return defValues;
            }

            Gson gson = new Gson();
            Type setType = new TypeToken<Set<String>>() {
            }.getType();
            return gson.fromJson(jsonValue, setType);
        }
    }

    public void setStringSet(int keyResId, Set<String> values) {
        if (Build.VERSION.SDK_INT >= 11) {
            String key = mContext.getString(keyResId);
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putStringSet(key, values);
            SharedPrefUtils.apply(editor);
        } else {
            Gson gson = new Gson();
            String jsonValue = gson.toJson(values);
            setString(keyResId, jsonValue);
        }
    }

    public void remove(int... keyResIds) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        for (int id : keyResIds) {
            editor.remove(mContext.getString(id));
        }
        SharedPrefUtils.apply(editor);
    }
}
