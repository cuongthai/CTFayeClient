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
package com.chatwing.whitelabel.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.chatwing.whitelabel.ChatWing;
import com.chatwing.whitelabel.managers.UserManager;
import com.chatwing.whitelabel.views.ErrorMessageView;
import com.chatwing.whitelabel.views.QuickMessageView;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.squareup.otto.Bus;

import java.util.List;

import javax.inject.Inject;

import dagger.ObjectGraph;


/**
 * Created by nguyenthanhhuy on 11/25/13.
 */
public abstract class BaseABFragmentActivity extends AppCompatActivity {
    public static final int RESULT_EXCEPTION = 800;

    @Inject
    protected ErrorMessageView mErrorMessageView;
    @Inject
    protected QuickMessageView mQuickMessageView;
    @Inject
    protected UserManager userManager;
    @Inject
    protected Bus mBus;

    private ObjectGraph mObjectGraph;
    private boolean mIsActive = false;
    private boolean isVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create the activity graph by .plus-ing our modules onto the application graph.
        ObjectGraph chatwingGraph = ChatWing.instance(getApplicationContext()).getChatwingGraph();
        mObjectGraph = chatwingGraph.plus(getModules().toArray());

        // Inject ourselves so subclasses will have dependencies fulfilled when this method returns.
        mObjectGraph.inject(this);

        // Create global configuration and initialize ImageLoader with this config
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
                .build();
        ImageLoader.getInstance().init(config);
    }

    @Override
    protected void onResume() {
        super.onResume();
        userManager.onResume();
        userManager.ping();
        isVisible = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        userManager.onPause();
        isVisible = false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mIsActive = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        mIsActive = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Eagerly clear the reference to the activity graph to allow it to be garbage collected as
        // soon as possible.
        mObjectGraph = null;
    }

    /**
     * A list of modules to use for the individual activity graph. Subclasses can override this
     * method to provide additional modules provided they call and include the modules returned by
     * calling {@code super.getModules()}.
     */
    protected abstract List<Object> getModules();

    /**
     * Inject the supplied {@code object} using the activity-specific graph.
     */
    public void inject(Object object) {
        if (mObjectGraph == null) return;
        mObjectGraph.inject(object);
    }

    public boolean isActive() {
        return mIsActive;
    }

    public boolean isVisible() {
        return isVisible;
    }
}
