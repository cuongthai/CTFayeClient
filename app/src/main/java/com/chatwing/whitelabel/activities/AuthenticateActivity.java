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

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.TextView;

import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.events.TaskFinishedEvent;
import com.chatwing.whitelabel.events.UserAuthenticationEvent;
import com.chatwing.whitelabel.managers.ApiManager;
import com.chatwing.whitelabel.managers.ProgressViewsManager;
import com.chatwing.whitelabel.modules.AuthenticateActivityModule;
import com.chatwing.whitelabel.pojos.params.oauth.AuthenticationParams;
import com.chatwing.whitelabel.pojos.responses.AuthenticationResponse;
import com.chatwing.whitelabel.tasks.StartSessionTask;
import com.chatwing.whitelabel.utils.LogUtils;
import com.chatwing.whitelabel.utils.NetworkUtils;
import com.squareup.otto.Bus;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Created by nguyenthanhhuy on 4/14/14.
 */
public abstract class AuthenticateActivity extends BaseABFragmentActivity {

    public static final String INTENT_USER = "IntentUser";

    private AsyncTask<?, ?, ?> mCurrentTask;

    @Inject
    protected Provider<StartSessionTask> mStartSessionTaskProvider;
    @Inject
    protected Bus mBus;
    @Inject
    protected NetworkUtils mNetworkUtils;
    @Inject
    protected ProgressViewsManager mProgressViewsManager;

    @Override
    protected List<Object> getModules() {
        return Arrays.<Object>asList(new AuthenticateActivityModule(this));
    }

    /////////////////////////////////////////////////////////////
    // Activity life cycle
    /////////////////////////////////////////////////////////////
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(getAuthenticationLayout());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mProgressViewsManager.setViews(
                findViewById(R.id.progress_container),
                (TextView) findViewById(R.id.progress_text),
                findViewById(R.id.fragment_container)
        );

        if (!mNetworkUtils.hasInternetConnection()) {
            //TODO in case user logged in but there is not internet
            // connection, should ask user and don't show login screen,
            // since it can be confusing.
            mErrorMessageView.show(R.string.error_network_connection);
        }
    }

    protected abstract int getAuthenticationLayout();

    @Override
    protected void onResume() {
        super.onResume();
        mBus.register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBus.unregister(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopCurrentTask();
    }


    ///////////////////////////////////////////////////////////
    // Otto subscribe methods.
    ///////////////////////////////////////////////////////////

    /**
     * Subclasses MUST override this method with {@link com.squareup.otto.Subscribe}
     * annotation in order for Otto to work.
     */
    protected void onUserInfoChanged(UserAuthenticationEvent event) {
        String tag = event.getTag();
        UserAuthenticationEvent.Status status = event.getStatus();

        if (isActive() && !TextUtils.isEmpty(tag)) {
            getSupportFragmentManager().popBackStack(tag, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        if (status == UserAuthenticationEvent.Status.SUCCEED) {
            startSession(event.getParams());
        } else if (status == UserAuthenticationEvent.Status.FAILED) {
            mErrorMessageView.show(event.getException(), getString(R.string.error_obtain_access_token));
            mProgressViewsManager.showProgress(false);
        }
    }

    /**
     * Subclasses MUST override this method with {@link com.squareup.otto.Subscribe}
     * annotation in order for Otto to work.
     */
    protected void onTaskFinished(TaskFinishedEvent event) {
        AsyncTask<?, ?, ?> task = event.getTask();
        if (task != mCurrentTask) {
            return;
        }

        mCurrentTask = null;
        mProgressViewsManager.showProgress(false);

        if (task instanceof StartSessionTask) {
            onTaskFinished(event, (StartSessionTask) task);
        }
    }

    ///////////////////////////////////////////////////////////
    // Instance methods
    ///////////////////////////////////////////////////////////
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                    finish();
                } else {
                    getSupportFragmentManager().popBackStack();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void startSession(AuthenticationParams params) {
        mProgressViewsManager.showProgress(true, R.string.progress_starting_session);
        StartSessionTask task = mStartSessionTaskProvider.get();
        startTask(task.execute(params));
    }

    /**
     * Handle general task exception if exist, otherwise handle Authentication Success Flow
     *
     * @param event
     * @param task
     */
    protected void onTaskFinished(TaskFinishedEvent event,
                                  StartSessionTask task) {

        if (event.getStatus() == TaskFinishedEvent.Status.FAILED) {
            if (event.getException() instanceof ApiManager.OtherApplicationException) {
                mErrorMessageView.show(event.getException().getMessage());
                return;
            }
            //Others error
            mErrorMessageView.show(event.getException(), getString(R.string.error_invalid_external_access_token));
            return;
        }
        LogUtils.v("Google Authenticate: onTaskFinished No Error");

        //Ok, no error! Handle Success Flow
        AuthenticationResponse result = (AuthenticationResponse) event.getResult();
        if (result.getUser() != null) {
            Intent intent = new Intent();
            intent.putExtra(INTENT_USER, result.getUser());
            setResult(RESULT_OK, intent);
            finish();
            return;
        }
    }

    /**
     * Utility method for sub class to start a task
     *
     * @param task
     */
    protected void startTask(AsyncTask<?, ?, ?> task) {
        stopCurrentTask();
        mCurrentTask = task;
    }

    protected int getFragmentContainerId() {
        return R.id.fragment_container;
    }

    private void stopCurrentTask() {
        if (mCurrentTask != null) {
            if (mCurrentTask.getStatus() != AsyncTask.Status.FINISHED) {
                mCurrentTask.cancel(true);
            }
            mCurrentTask = null;
        }
    }
}
