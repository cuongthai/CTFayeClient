package com.chatwing.whitelabel.fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.adapters.EmailsAdapterFactory;
import com.chatwing.whitelabel.events.UserAuthenticationEvent;
import com.chatwing.whitelabel.managers.BuildManager;
import com.chatwing.whitelabel.pojos.oauth.AppOAuthParams;
import com.chatwing.whitelabel.pojos.oauth.ChatwingOAuthParams;
import com.chatwing.whitelabel.pojos.params.oauth.AuthenticationParams;
import com.chatwing.whitelabel.validators.EmailValidator;
import com.chatwing.whitelabel.validators.PasswordValidator;
import com.squareup.otto.Bus;

import javax.inject.Inject;
import javax.inject.Provider;

public class LoginFragment extends BaseFragment implements View.OnClickListener {

    public interface Delegate extends InjectableFragmentDelegate {
        void forgotPassword();
    }

    @Inject
    protected EmailValidator mEmailValidator;
    @Inject
    protected PasswordValidator mPasswordValidator;
    @Inject
    protected Bus mBus;
    @Inject
    protected EmailsAdapterFactory mEmailsAdapterFactory;
    @Inject
    protected BuildManager mBuildManager;

    private Delegate mDelegate;
    private AutoCompleteTextView mEmailEditText;
    private EditText mPasswordEditText;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    protected void onAttachToContext(Context context) {
        if (context instanceof Delegate){
            mDelegate = (Delegate) context;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {
        mEmailEditText = (AutoCompleteTextView) v.findViewById(R.id.email);

        mPasswordEditText = (EditText) v.findViewById(R.id.password);
        mPasswordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    loginChatWing();
                    handled = true;
                }
                return handled;
            }
        });
        Button viewById = (Button) v.findViewById(R.id.btn_login_chatwing);
        viewById.setText(getString(R.string.title_login_chatwing, getString(R.string.app_name)));
        viewById.setOnClickListener(this);

        TextView forgotPasswordTextView = (TextView) v.findViewById(R.id.forgot_password);
        forgotPasswordTextView.setPaintFlags(
                forgotPasswordTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        forgotPasswordTextView.setOnClickListener(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mDelegate.inject(this);
        mEmailEditText.setAdapter(mEmailsAdapterFactory.build());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mDelegate = null;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_login_chatwing:
                loginChatWing();
                break;
            case R.id.forgot_password:
                mDelegate.forgotPassword();
                break;
            default:
                break;
        }
    }

    public void setEmailError(String error) {
        mEmailEditText.setError(error);
    }

    private void loginChatWing() {
        String email = mEmailEditText.getText().toString();
        String password = mPasswordEditText.getText().toString();
        try {
            mEmailValidator.validate(email);
            mPasswordValidator.validate(password);
            AuthenticationParams params = mBuildManager.isCustomLoginType()
                    ? new AppOAuthParams(email, password)
                    : new ChatwingOAuthParams(email, password);
            UserAuthenticationEvent event
                    = UserAuthenticationEvent.succeedEvent("", params);
            mBus.post(event);
        } catch (EmailValidator.InvalidEmailException ex) {
            mEmailEditText.setError(getString(R.string.error_invalid_email));
        } catch (PasswordValidator.InvalidPasswordException ex) {
            mPasswordEditText.setError(getString(R.string.error_invalid_password));
        }
    }
}
