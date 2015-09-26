package com.chatwing.whitelabel.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.events.TaskFinishedEvent;
import com.chatwing.whitelabel.events.UserAuthenticationEvent;
import com.chatwing.whitelabel.fragments.RegisterFragment;
import com.chatwing.whitelabel.managers.ApiManager;
import com.chatwing.whitelabel.managers.BuildManager;
import com.chatwing.whitelabel.modules.RegisterActivityModule;
import com.chatwing.whitelabel.pojos.oauth.AppOAuthParams;
import com.chatwing.whitelabel.pojos.oauth.ChatwingOAuthParams;
import com.chatwing.whitelabel.pojos.params.oauth.AuthenticationParams;
import com.chatwing.whitelabel.tasks.RegisterTask;
import com.chatwing.whitelabel.validators.EmailValidator;
import com.chatwing.whitelabel.validators.PasswordValidator;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

public class RegisterActivity extends AuthenticateActivity implements RegisterFragment.Delegate {

    private static final String TAG_FRAGMENT_REGISTER = "register_fragment";

    @Inject
    protected RegisterFragment mRegisterFragment;
    @Inject
    protected Provider<RegisterTask> mRegisterTaskProvider;
    @Inject
    protected BuildManager mBuildManager;

    @Override
    protected List<Object> getModules() {
        List modules = new ArrayList(super.getModules());
        modules.add(new RegisterActivityModule(this));
        return modules;
    }

    @Override
    protected int getAuthenticationLayout() {
        return R.layout.activity_register;
    }

    ////////////////////////////////
    ///    RegisterFragment Delegate
    ////////////////////////////////
    @Override
    public void inject(Fragment fragment) {
        super.inject(fragment);
    }

    @Override
    public void register(String email,
                         String password,
                         boolean agreeConditions,
                         boolean autoCreateChatbox) {
        mProgressViewsManager.showProgress(true, R.string.progress_registering);
        RegisterTask task = mRegisterTaskProvider.get();
        task.setParams(email, password, agreeConditions, autoCreateChatbox);
        startTask(task.execute());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getRegisterFragment() == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(
                            R.id.register_fragment_container,
                            mRegisterFragment,
                            TAG_FRAGMENT_REGISTER)
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
        AsyncTask<?, ?, ?> task = event.getTask();
        if (task instanceof RegisterTask) {
            handleRegisterTaskFinished(event, (RegisterTask) task);
        } else {
            super.onTaskFinished(event);
        }
    }

    private void handleRegisterTaskFinished(TaskFinishedEvent event, RegisterTask task) {
        if (event.getStatus() == TaskFinishedEvent.Status.FAILED) {
            mProgressViewsManager.showProgress(false);

            Exception exception = event.getException();
            if (exception instanceof EmailValidator.InvalidEmailException) {
                showEmailError(getString(R.string.error_invalid_email));
                return;
            }
            if (exception instanceof PasswordValidator.InvalidPasswordException) {
                showPasswordError(getString(R.string.error_invalid_password));
                return;
            }
            if (exception instanceof ApiManager.ValidationException) {
                //Throws by server error code, we can have more detail error from server but let's me it confusing here
                showEmailError(getString(R.string.error_invalid_username_password));
                return;
            }

            if (exception instanceof ApiManager.OtherApplicationException) {
                mErrorMessageView.show(((ApiManager.OtherApplicationException) exception).getError().getMessage());
                return;
            }

            //Other exceptions/error let boss handles it
            mErrorMessageView.show(exception);
            return;
        }

        // Registered successfully, should start session to get authenticated
        // session now.
        AuthenticationParams params = mBuildManager.isCustomLoginType()
                ? new AppOAuthParams(task.getEmail(), task.getPassword())
                : new ChatwingOAuthParams(task.getEmail(), task.getPassword());
        startSession(params);
    }

    private void showPasswordError(String message) {
        RegisterFragment fragment = getRegisterFragment();
        if (fragment != null) {
            fragment.setPasswordError(message);
        }
    }

    private void showEmailError(String message) {
        RegisterFragment fragment = getRegisterFragment();
        if (fragment != null) {
            fragment.setEmailError(message);
        }
    }

    private RegisterFragment getRegisterFragment() {
        return (RegisterFragment) getSupportFragmentManager()
                .findFragmentByTag(TAG_FRAGMENT_REGISTER);
    }
}
