package com.chatwing.whitelabel.fragments;

import android.app.Activity;
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
import android.widget.EditText;
import android.widget.TextView;

import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.adapters.EmailsAdapterFactory;
import com.chatwing.whitelabel.pojos.oauth.ChatwingOAuthParams;
import com.chatwing.whitelabel.validators.EmailValidator;
import com.chatwing.whitelabel.validators.PasswordValidator;
import com.chatwingsdk.events.internal.UserAuthenticationEvent;
import com.chatwingsdk.fragments.InjectableFragmentDelegate;
import com.squareup.otto.Bus;

import javax.inject.Inject;
import javax.inject.Provider;

public class LoginFragment extends Fragment implements View.OnClickListener {

    public interface Delegate extends InjectableFragmentDelegate {
        void forgotPassword();
    }

    @Inject
    EmailValidator mEmailValidator;
    @Inject
    PasswordValidator mPasswordValidator;
    @Inject
    Bus mBus;
    @Inject
    EmailsAdapterFactory mEmailsAdapterFactory;
    @Inject
    Provider<Typeface> mIconTypefaceProvider;
    private Delegate mDelegate;
    private AutoCompleteTextView mEmailEditText;
    private EditText mPasswordEditText;

    public LoginFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mDelegate = (Delegate) activity;
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

        v.findViewById(R.id.btn_login_chatwing).setOnClickListener(this);

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
        ((TextView) getView().findViewById(R.id.ic_chatwing)).setTypeface(mIconTypefaceProvider.get());
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

    public void setPasswordError(String error) {
        mPasswordEditText.setError(error);
    }

    private void loginChatWing() {
        String email = mEmailEditText.getText().toString();
        String password = mPasswordEditText.getText().toString();
        try {
            mEmailValidator.validate(email);
            mPasswordValidator.validate(password);
            ChatwingOAuthParams params = new ChatwingOAuthParams(email, password);
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
