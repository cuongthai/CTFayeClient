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

package com.chatwing.whitelabel;


import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;

import com.chatwing.whitelabel.activities.RegisterActivity;
import com.chatwing.whitelabel.fragments.AuthenticateFragment;
import com.chatwing.whitelabel.fragments.ForgotPasswordFragment;
import com.chatwing.whitelabel.fragments.GooglePlusDialogFragment;
import com.chatwing.whitelabel.fragments.GuestLoginFragment;
import com.chatwing.whitelabel.fragments.LoginFragment;
import com.chatwing.whitelabel.fragments.LoginTwitterFragment;
import com.chatwing.whitelabel.managers.ApiManager;
import com.chatwing.whitelabel.modules.LegacyActivityModule;
import com.chatwing.whitelabel.pojos.responses.ResetPasswordResponse;
import com.chatwing.whitelabel.tasks.GetGooglePlusAccessTokenTask;
import com.chatwing.whitelabel.tasks.ResetPasswordTask;
import com.chatwing.whitelabel.validators.EmailValidator;
import com.chatwingsdk.activities.AuthenticateActivity;
import com.chatwingsdk.events.internal.TaskFinishedEvent;
import com.chatwingsdk.events.internal.UserAuthenticationEvent;
import com.chatwingsdk.managers.ProgressViewsManager;
import com.chatwingsdk.managers.UserManager;
import com.chatwingsdk.pojos.User;
import com.chatwingsdk.pojos.params.oauth.AuthenticationParams;
import com.chatwingsdk.utils.LogUtils;
import com.chatwingsdk.utils.NetworkUtils;
import com.chatwingsdk.views.QuickMessageView;
import com.facebook.Session;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.plus.PlusClient;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class LegacyLoginActivity extends AuthenticateActivity
        implements AuthenticateFragment.Delegate,
        LoginFragment.Delegate,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        GuestLoginFragment.Delegate,
        ForgotPasswordFragment.Delegate {
    private static final String TAG_FRAGMENT_LOGIN_TWITTER =
            "login_twitter_fragment";
    private static final String TAG_FRAGMENT_LOGIN_GUEST =
            "login_guest_fragment";
    protected static final String TAG_FRAGMENT_MAIN_AUTHENTICATE = "main_authenticate_fragment";
    protected static final String TAG_FRAGMENT_GOOGLE_PLUS =
            "google_plus_dialog";

    protected static final String TAG_FRAGMENT_FORGOT_PASSWORD =
            "forgot_password_fragment";

    protected static final String BACK_TACK_FORGOT_PASSWORD =
            "forgot_password_back_stack_name";
    @Inject
    LoginFragment mLoginFragment;
    @Inject
    NetworkUtils mNetworkUtils;
    @Inject
    PlusClient mPlusClient;
    @Inject
    ProgressViewsManager mProgressViewsManager;
    @Inject
    Bus mBus;
    @Inject
    GuestLoginFragment guestLoginFragment;
    @Inject
    ForgotPasswordFragment mForgotPasswordFragment;
    @Inject
    protected QuickMessageView mConfirmMessageView;
    @Inject
    UserManager mUserManager;

    private ConnectionResult mConnectionResult;
    protected static final int REQUEST_CODE_SIGN_IN_GOOGLE_PLUS = 9000;
    protected static final int REQUEST_CODE_GET_GOOGLE_PLAY_SERVICES = 10000;
    protected static final int REQUEST_CODE_USER_RECOVERABLE_AUTH = 11000;
    private static final String BACK_STACK_NAME_LOGIN_TWITTER =
            "login_twitter_back_stack_name";
    private static final String BACK_STACK_NAME_LOGIN_GUEST =
            "login_guest_back_stack_name";
    protected static final int REQUEST_CODE_AUTHENTICATE = 1000;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.title_login);
        getSupportFragmentManager()
                .beginTransaction()
                .add(
                        R.id.main_auth_fragment_container,
                        mLoginFragment,
                        TAG_FRAGMENT_MAIN_AUTHENTICATE)
                .commit();
    }

    @Override
    @Subscribe
    public void onUserInfoChanged(UserAuthenticationEvent event) {
        super.onUserInfoChanged(event);
    }

    @Override
    @Subscribe
    public void onTaskFinished(TaskFinishedEvent event) {
        super.onTaskFinished(event);
        AsyncTask<?, ?, ?> task = event.getTask();
        if (task instanceof ResetPasswordTask) {
            onTaskFinished(event, (ResetPasswordTask) task);
        }
    }

    @Override
    protected List<Object> getModules() {
        List<Object> modules = new ArrayList<Object>(super.getModules());
        modules.add(new LegacyActivityModule(this));
        return modules;
    }

    @Override
    protected void startSession(AuthenticationParams params) {
        super.startSession(params);
        logoutOfServices();
    }

    private void logoutOfServices() {
        Session session = Session.getActiveSession();
        if (session != null) {
            session.closeAndClearTokenInformation();
        }

        if (mPlusClient.isConnected()) {
            mPlusClient.clearDefaultAccount();
            mPlusClient.disconnect();
        }
    }

    @Override
    protected int getAuthenticationLayout() {
        return R.layout.activity_login;
    }

    @Override
    public void login(String accountType) {
        if (!mNetworkUtils.hasInternetConnection()) {
            mErrorMessageView.show(R.string.error_network_connection);
            return;
        }

        if (accountType.equals(Constants.TYPE_GOOGLE)) {
            if (!isPlayServicesAvailable()) {
                return;
            }

            if (!mPlusClient.isConnected()) {
                if (mConnectionResult == null) {
                    connectPlusClient();
                } else {
                    try {
                        mConnectionResult.startResolutionForResult(
                                this,
                                REQUEST_CODE_SIGN_IN_GOOGLE_PLUS);
                    } catch (IntentSender.SendIntentException e) {
                        // Try connecting again.
                        mConnectionResult = null;
                        connectPlusClient();
                    }
                }
            } else {
                // PlusClient connected. Looked like it failed last time
                // (either during getting access token from G+ server or
                // authenticating with our server). And now user is trying
                // again, let's try to get access token again and re-authenticate
                // with our server.
                loadGooglePlusAccessToken();
            }
            return;
        }

        Fragment fragment = null;
        String fragmentTag = null;
        String backStackName = null;

        if (accountType.equals(Constants.TYPE_TWITTER)) {
            fragmentTag = TAG_FRAGMENT_LOGIN_TWITTER;
            backStackName = BACK_STACK_NAME_LOGIN_TWITTER;
            fragment = LoginTwitterFragment.newInstance(backStackName);
        } else if (accountType.equals(Constants.TYPE_GUEST)) {
            fragmentTag = TAG_FRAGMENT_LOGIN_GUEST;
            backStackName = BACK_STACK_NAME_LOGIN_GUEST;
            fragment = guestLoginFragment;
        }

        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(getFragmentContainerId(), fragment, fragmentTag)
                    .addToBackStack(backStackName)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.register:
                Intent i = new Intent(this, RegisterActivity.class);
                startActivityForResult(i, REQUEST_CODE_AUTHENTICATE);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SIGN_IN_GOOGLE_PLUS
                || requestCode == REQUEST_CODE_GET_GOOGLE_PLAY_SERVICES) {
            // Google+ resolve error
            if (resultCode == Activity.RESULT_OK
                    && !mPlusClient.isConnected()
                    && !mPlusClient.isConnecting()) {
                mConnectionResult = null;
                connectPlusClient();
            } else {
                mProgressViewsManager.showProgress(false);
            }
        } else if (requestCode == REQUEST_CODE_USER_RECOVERABLE_AUTH) {
            if (resultCode == RESULT_OK) {
                connectPlusClient();
            } else {
                mProgressViewsManager.showProgress(false);
            }
        } else if (requestCode == REQUEST_CODE_AUTHENTICATE && resultCode == RESULT_OK) {
            checkUser((User) data.getSerializableExtra(AuthenticateActivity.INTENT_USER));
        }
    }

    @Override
    public AuthenticateFragment.Info getInfo() {
        return new AuthenticateFragment.Info(
                R.string.title_login_fb,
                R.string.title_login_google_plus,
                R.string.title_login_tumblr,
                R.string.title_login_twitter,
                R.string.title_login_yahoo,
                R.string.title_login_guest
        );
    }

    @Override
    public void inject(Fragment fragment) {
        super.inject(fragment);
    }

    @Override
    public void forgotPassword() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(getFragmentContainerId(),
                        new ForgotPasswordFragment(),
                        TAG_FRAGMENT_FORGOT_PASSWORD)
                .addToBackStack(BACK_TACK_FORGOT_PASSWORD)
                .commit();
    }

    private void connectPlusClient() {
        mProgressViewsManager.showProgress(true, R.string.progress_logging_in);
        mPlusClient.connect();
    }

    private void loadGooglePlusAccessToken() {
        mProgressViewsManager.showProgress(true, R.string.progress_getting_access_token);
        GetGooglePlusAccessTokenTask task = new GetGooglePlusAccessTokenTask(
                mBus,
                this,
                mPlusClient.getAccountName(),
                Constants.GOOGLE_PLUS_SCOPES,
                null);
        startTask(task.execute());
    }

    private void checkUser(User intentUser) {
        if (intentUser == null) {
            return;
        }

        User user = mUserManager.getCurrentUser();

        if (user != null
                && user.equals(intentUser)) {
            //Okay, user is set
            if (user.isSessionValid()) {
                Intent intent = new Intent();
                intent.putExtra(AuthenticateActivity.INTENT_USER, intentUser);
                setResult(RESULT_OK, intent);
                finish();
            } else {
                // Session expired.
                mUserManager.removeUser(user);
            }
        }
    }


    private void onTaskFinished(TaskFinishedEvent event,
                                @SuppressWarnings("UnusedParameters") ResetPasswordTask task) {
        ResetPasswordResponse response = (ResetPasswordResponse) event.getResult();
        if (event.getStatus() == TaskFinishedEvent.Status.SUCCEED) {
            //FIXME: this is not localizable
            mConfirmMessageView.show(response.getData());

            getSupportFragmentManager().popBackStack(
                    BACK_TACK_FORGOT_PASSWORD,
                    FragmentManager.POP_BACK_STACK_INCLUSIVE);
        } else {
            if ((event.getException() instanceof ApiManager.ValidationException ||
                    event.getException() instanceof EmailValidator.InvalidEmailException)
                    && mForgotPasswordFragment.isAdded()) {
                mForgotPasswordFragment.setEmailError(getString(R.string.error_invalid_email));
                return;
            }
            //Other errors (Unknown or general errors) we let other to handle it
            mErrorMessageView.show(event.getException());
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean isPlayServicesAvailable() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            showGooglePlusDialogFragment(resultCode, REQUEST_CODE_GET_GOOGLE_PLAY_SERVICES);
            return false;
        }
        return true;
    }

    private void showGooglePlusDialogFragment(int errorCode, int requestCode) {
        GooglePlusDialogFragment fragment = GooglePlusDialogFragment.newInstance(
                errorCode, requestCode);
        if (fragment != null) {
            fragment.show(getSupportFragmentManager(), TAG_FRAGMENT_GOOGLE_PLUS);
        }
    }

    ////////////////////////////////////////////////////////////
    // Google Plus Connection Callbacks
    ////////////////////////////////////////////////////////////
    @Override
    public void onConnected(Bundle bundle) {
        // We've resolved any connection errors.
        mProgressViewsManager.showProgress(false);
        loadGooglePlusAccessToken();
    }

    @Override
    public void onDisconnected() {
        LogUtils.v("G+ disconnected.");
    }

    /////////////////////////////////////////////////////////////
    // Google Plus Connection Failed Listener
    /////////////////////////////////////////////////////////////
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // The user clicked the sign-in button already. Start to resolve
        // connection errors. Wait until onConnected() to dismiss the
        // connection dialog.
        mProgressViewsManager.showProgress(false);
        if (result.hasResolution()) {
            try {
                result.startResolutionForResult(
                        this,
                        REQUEST_CODE_SIGN_IN_GOOGLE_PLUS);
            } catch (IntentSender.SendIntentException e) {
                connectPlusClient();
            }
        }

        // Save the intent so that we can start an activity when the user clicks
        // the sign-in button.
        mConnectionResult = result;
    }

    @Override
    public void loginGuest(String guestName, String selectedAvatarUrl) {

    }

    @Override
    public void resetPassword(String email) throws EmailValidator.InvalidEmailException {

    }
}
