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


import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;

import com.chatwing.whitelabel.Constants;
import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.fragments.AuthenticateFragment;
import com.chatwing.whitelabel.fragments.ForgotPasswordFragment;
import com.chatwing.whitelabel.fragments.GooglePlusDialogFragment;
import com.chatwing.whitelabel.fragments.GuestLoginFragment;
import com.chatwing.whitelabel.fragments.LoginFragment;
import com.chatwing.whitelabel.fragments.LoginScribeFragment;
import com.chatwing.whitelabel.fragments.LoginTwitterFragment;
import com.chatwing.whitelabel.managers.ApiManager;
import com.chatwing.whitelabel.managers.BuildManager;
import com.chatwing.whitelabel.modules.ExtendChatWingModule;
import com.chatwing.whitelabel.modules.LegacyActivityModule;
import com.chatwing.whitelabel.pojos.responses.ResetPasswordResponse;
import com.chatwing.whitelabel.scribeconfigs.TumblrConfig;
import com.chatwing.whitelabel.scribeconfigs.YahooConfig;
import com.chatwing.whitelabel.tasks.GetGooglePlusAccessTokenTask;
import com.chatwing.whitelabel.tasks.ResetPasswordTask;
import com.chatwing.whitelabel.validators.EmailValidator;
import com.chatwingsdk.activities.AuthenticateActivity;
import com.chatwingsdk.events.internal.TaskFinishedEvent;
import com.chatwingsdk.events.internal.UserAuthenticationEvent;
import com.chatwingsdk.managers.ProgressViewsManager;
import com.chatwingsdk.managers.UserManager;
import com.chatwingsdk.pojos.User;
import com.chatwingsdk.pojos.errors.AuthenticationParamsError;
import com.chatwingsdk.pojos.errors.ChatWingError;
import com.chatwingsdk.pojos.params.oauth.AuthenticationParams;
import com.chatwingsdk.pojos.params.oauth.OAuth2Params;
import com.chatwingsdk.tasks.StartSessionTask;
import com.chatwingsdk.utils.LogUtils;
import com.chatwingsdk.utils.NetworkUtils;
import com.chatwingsdk.views.QuickMessageView;
import com.facebook.Session;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.gson.Gson;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

public class LegacyLoginActivity extends AuthenticateActivity
        implements AuthenticateFragment.Delegate,
        LoginFragment.Delegate,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GuestLoginFragment.Delegate,
        ForgotPasswordFragment.Delegate {
    private static final String TAG_FRAGMENT_LOGIN_TWITTER =
            "login_twitter_fragment";
    private static final String TAG_FRAGMENT_LOGIN_GUEST =
            "login_guest_fragment";
    protected static final String TAG_FRAGMENT_MAIN_AUTHENTICATE = "main_authenticate_fragment";
    protected static final String TAG_FRAGMENT_GOOGLE_PLUS =
            "google_plus_dialog";
    private static final String TAG_FRAGMENT_LOGIN_YAHOO =
            "login_yahoo_fragment";
    private static final String TAG_FRAGMENT_LOGIN_TUMBLR =
            "login_tumblr_fragment";

    protected static final String TAG_FRAGMENT_FORGOT_PASSWORD =
            "forgot_password_fragment";

    protected static final String BACK_TACK_FORGOT_PASSWORD =
            "forgot_password_back_stack_name";
    private static final String BACK_STACK_NAME_LOGIN_YAHOO =
            "login_yahoo_back_stack_name";
    private static final String BACK_STACK_NAME_LOGIN_TUMBLR =
            "login_tumblr_back_stack_name";
    @Inject
    LoginFragment mLoginFragment;
    @Inject
    AuthenticateFragment mSecondAuthenticationFragment;
    @Inject
    NetworkUtils mNetworkUtils;
    @Inject
    GoogleApiClient mGoogleApiClient;
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
    @Inject
    Provider<ResetPasswordTask> mResetPasswordTaskProvider;
    @Inject
    EmailValidator mEmailValidator;
    @Inject
    BuildManager mBuildManager;

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
        if (getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT_MAIN_AUTHENTICATE) == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.main_auth_fragment_container,
                            mLoginFragment,
                            TAG_FRAGMENT_MAIN_AUTHENTICATE)
                    .commit();
        }

        renderAppropriateUIForAppType();
    }

    private void renderAppropriateUIForAppType() {
        if (mBuildManager.isOfficialChatWingApp()) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(
                            R.id.second_auth_fragment_container,
                            mSecondAuthenticationFragment,
                            getString(R.string.fragment_tag_authenticate))
                    .commit();
        }
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
        } else if (task instanceof GetGooglePlusAccessTokenTask) {
            onTaskFinished(event, (GetGooglePlusAccessTokenTask) task);
        }
    }

    @Override
    protected List<Object> getModules() {
        List<Object> modules = new ArrayList<Object>(super.getModules());
        modules.add(new ExtendChatWingModule(this));
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

        if (mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            mGoogleApiClient.disconnect();
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

            if (!mGoogleApiClient.isConnected()) {
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
        } else if (accountType.equals(Constants.TYPE_YAHOO)) {
            fragmentTag = TAG_FRAGMENT_LOGIN_YAHOO;
            backStackName = BACK_STACK_NAME_LOGIN_YAHOO;
            fragment = LoginScribeFragment.newInstance(
                    YahooConfig.class,
                    backStackName);
        } else if (accountType.equals(Constants.TYPE_TUMBLR)) {
            fragmentTag = TAG_FRAGMENT_LOGIN_TUMBLR;
            backStackName = BACK_STACK_NAME_LOGIN_TUMBLR;
            fragment = LoginScribeFragment.newInstance(
                    TumblrConfig.class,
                    backStackName);
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
        menu.findItem(R.id.register).setVisible(mBuildManager.isSupportedRegister());
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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
        LogUtils.v("Google Authenticate: onActivityResult");

        if (requestCode == REQUEST_CODE_SIGN_IN_GOOGLE_PLUS
                || requestCode == REQUEST_CODE_GET_GOOGLE_PLAY_SERVICES) {
            // Google+ resolve error
            if (resultCode == Activity.RESULT_OK
                    && !mGoogleApiClient.isConnected()
                    && !mGoogleApiClient.isConnecting()) {
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
        mGoogleApiClient.connect();
    }

    private void loadGooglePlusAccessToken() {
        LogUtils.v("Google Authenticate: loadGooglePlusAccessToken");

        mProgressViewsManager.showProgress(true, R.string.progress_getting_access_token);
        GetGooglePlusAccessTokenTask task = new GetGooglePlusAccessTokenTask(
                mBus,
                this,
                Plus.AccountApi.getAccountName(mGoogleApiClient),
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

    @Override
    protected void onTaskFinished(TaskFinishedEvent event, StartSessionTask task) {
        AuthenticationParams params = task.getParams();
        LogUtils.v("Google Authenticate: onTaskFinished");

        if (event.getStatus() == TaskFinishedEvent.Status.FAILED) {
            LogUtils.v("Google Authenticate: onTaskFinished FAILED");
            // Handle error when login using ChatWing account
            if (Constants.TYPE_CHATWING.equals(params.getType())
                    && event.getException() instanceof ApiManager.ValidationException
                    && ChatWingError.hasValidationError(((ApiManager.ValidationException) event.getException()).getError())
                    && mLoginFragment.isAdded()) {
                mLoginFragment.setEmailError(getString(R.string.error_invalid_username_password));
                return;
            }

            if(Constants.TYPE_APP.equals(params.getType())
                    && event.getException() instanceof ApiManager.ValidationException
                    && ChatWingError.hasValidationError(((ApiManager.ValidationException) event.getException()).getError())
                    && mLoginFragment.isAdded()) {
                mLoginFragment.setEmailError(getString(R.string.error_app_invalid_username_password));
                return;
            }

            //Error Google
            if (params.getType().equals(com.chatwingsdk.Constants.TYPE_GOOGLE)
                    && event.getException() instanceof com.chatwingsdk.managers.ApiManager.InvalidExternalAccessTokenException) {
                ChatWingError error = ((com.chatwingsdk.managers.ApiManager.InvalidExternalAccessTokenException) event.getException()).getError();
                AuthenticationParamsError errorDetail = new Gson().fromJson(
                        error.getParams(),
                        AuthenticationParamsError.class);

                if (AuthenticationParamsError.NAME_INTERNAL_OAUTH_ERROR.equals(errorDetail.getName())) {
                    // In case of Google Plus authentication, if server indicates token
                    // is invalid, invalidate the token that we found is bad so that
                    // GoogleAuthUtil won't return it next time (it
                    // may have cached it).
                    // Also, retry getting access token once more.
                    GoogleAuthUtil.invalidateToken(this, ((OAuth2Params) params).getToken());
                    loadGooglePlusAccessToken();
                }
                return;
            }
        }

        super.onTaskFinished(event, task);
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
        LogUtils.v("Google Authenticate: showGooglePlusDialogFragment");

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
        LogUtils.v("Google Authenticate: onConnected");

        // We've resolved any connection errors.
        mProgressViewsManager.showProgress(false);
        loadGooglePlusAccessToken();
    }

    /////////////////////////////////////////////////////////////
    // Google Plus Connection Failed Listener
    /////////////////////////////////////////////////////////////
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        LogUtils.v("Google Authenticate: onConnectionFailed");

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
        mEmailValidator.validate(email);
        ResetPasswordTask task = mResetPasswordTaskProvider.get();
        startTask(task.execute(email));
        mProgressViewsManager.showProgress(true, R.string.progress_sending);
    }

    ////////////////////////////////////////////////////////////
    // Google Api Clients Callbacks
    ////////////////////////////////////////////////////////////
    @Override
    public void onConnectionSuspended(int i) {

    }

    private void onTaskFinished(TaskFinishedEvent event,
                                @SuppressWarnings("UnusedParameters") GetGooglePlusAccessTokenTask task) {
        if (event.getStatus() == TaskFinishedEvent.Status.SUCCEED) {
            String token = (String) event.getResult();
            UserAuthenticationEvent e = UserAuthenticationEvent.succeedEvent(
                    null,
                    new OAuth2Params(Constants.TYPE_GOOGLE, token));
            onUserInfoChanged(e);
            return;
        }

        Exception exception = event.getException();
        if (exception instanceof GooglePlayServicesAvailabilityException) {
            int errorCode = ((GooglePlayServicesAvailabilityException) exception)
                    .getConnectionStatusCode();
            showGooglePlusDialogFragment(
                    errorCode,
                    REQUEST_CODE_USER_RECOVERABLE_AUTH);
            return;
        }

        if (exception instanceof UserRecoverableAuthException) {
            // Start the user recoverable action using the intent returned by
            // getIntent()
            startActivityForResult(
                    ((UserRecoverableAuthException) exception).getIntent(),
                    REQUEST_CODE_USER_RECOVERABLE_AUTH);
            return;
        }

        if (exception instanceof GoogleAuthException) {
            // Failure. The call is not expected to ever succeed so it should not be
            // retried.
            mErrorMessageView.show(R.string.error_unknown);
            LogUtils.e(exception);
            return;
        }

        if (exception instanceof IOException) {
            // network or server error, the call is expected to
            // succeed if user try again later.
            mErrorMessageView.show(R.string.error_unknown);
            LogUtils.e(exception);
        }
    }
}
