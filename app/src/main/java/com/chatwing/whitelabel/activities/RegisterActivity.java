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
import com.chatwing.whitelabel.pojos.errors.ChatWingError;
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

public class RegisterActivity extends AuthenticateActivity
        implements RegisterFragment.Delegate {
    protected static final String TAG_FRAGMENT_MAIN_AUTHENTICATE = "main_authenticate_fragment";

    @Inject
    RegisterFragment mRegisterFragment;
    @Inject
    Provider<RegisterTask> mRegisterTaskProvider;
    @Inject
    BuildManager mBuildManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager()
                .beginTransaction()
                .add(
                        R.id.main_auth_fragment_container,
                        mRegisterFragment,
                        TAG_FRAGMENT_MAIN_AUTHENTICATE)
                .commit();
    }

    @Override
    protected List<Object> getModules() {
        List<Object> modules = new ArrayList<Object>(super.getModules());
        modules.add(new RegisterActivityModule(this));
        return modules;
    }


    @Override
    public void register(String email, String password, boolean agreeConditions,
                         boolean autoCreateChatbox) {
        mProgressViewsManager.showProgress(true, R.string.progress_registering);
        RegisterTask task = mRegisterTaskProvider.get();
        task.setParams(email, password, agreeConditions, autoCreateChatbox);
        startTask(task.execute());
    }

    @Override
    protected int getAuthenticationLayout() {
         return R.layout.activity_register;
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
            onTaskFinished(event, (RegisterTask) task);
        } else {
            super.onTaskFinished(event);
        }
    }

    private void onTaskFinished(TaskFinishedEvent event, RegisterTask task) {
        if (event.getStatus() == TaskFinishedEvent.Status.FAILED) {
            mProgressViewsManager.showProgress(false);

            Exception exception = event.getException();
            if (exception instanceof EmailValidator.InvalidEmailException) {
                mRegisterFragment.setEmailError(getString(R.string.error_invalid_email));
                return;
            }
            if (exception instanceof PasswordValidator.InvalidPasswordException) {
                mRegisterFragment.setEmailError(getString(R.string.error_invalid_password));
                return;
            }
            if (exception instanceof ApiManager.ValidationException
                    && ChatWingError.hasValidationError(((ApiManager.ValidationException) exception).getError())) {
                //Throws by server error code, we can have more detail error from server but let's me it confusing here
                mRegisterFragment.setEmailError(getString(R.string.error_invalid_username_password));
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

    @Override
    public void inject(Fragment fragment) {
        super.inject(fragment);
    }
}
