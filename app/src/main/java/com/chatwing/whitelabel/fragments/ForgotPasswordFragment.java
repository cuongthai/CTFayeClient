package com.chatwing.whitelabel.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;


import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.adapters.EmailsAdapterFactory;
import com.chatwing.whitelabel.validators.EmailValidator;

import javax.inject.Inject;

/**
 * Author: Huy Nguyen
 * Date: Oct 1st, 2013
 */
public class ForgotPasswordFragment extends Fragment {
    @Inject
    EmailValidator mEmailValidator;
    @Inject
    InputMethodManager mInputMethodManager;
    @Inject
    EmailsAdapterFactory mEmailsAdapterFactory;
    private Delegate mDelegate;
    private AutoCompleteTextView mEmailEditText;

    public void setEmailError(String message) {
        if (mEmailEditText != null) {
            mEmailEditText.setError(message);
        }
    }

    public interface Delegate extends InjectableFragmentDelegate {
        void resetPassword(String email) throws EmailValidator.InvalidEmailException;
    }

    public ForgotPasswordFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mDelegate = (Delegate) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_forgot_password, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mEmailEditText = (AutoCompleteTextView) view.findViewById(R.id.email);
        mEmailEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    resetPassword();
                    handled = true;
                }
                return handled;
            }
        });
        mEmailEditText.requestFocus();

        view.findViewById(R.id.reset_password).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mDelegate.inject(this);
        mEmailEditText.setAdapter(mEmailsAdapterFactory.build());
        mInputMethodManager.showSoftInput(mEmailEditText, InputMethodManager.SHOW_IMPLICIT);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mDelegate = null;
    }

    private void resetPassword() {
        String email = mEmailEditText.getText().toString();
        try {
            mEmailValidator.validate(email);
            mDelegate.resetPassword(email);
        } catch (EmailValidator.InvalidEmailException ex) {
            mEmailEditText.setError(getString(R.string.error_invalid_email));
        }
    }
}
