package com.chatwing.whitelabel.fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.adapters.EmailsAdapterFactory;
import com.chatwing.whitelabel.validators.EmailValidator;
import com.chatwing.whitelabel.validators.PasswordValidator;
import com.squareup.otto.Bus;

import javax.inject.Inject;

/**
 * Created by nguyenthanhhuy on 4/14/14.
 */
public class RegisterFragment extends BaseFragment {

    public interface Delegate extends InjectableFragmentDelegate {
        void register(String email,
                      String password,
                      boolean agreeConditions,
                      boolean autoCreateChatbox);
    }

    @Inject
    protected EmailValidator emailValidator;
    @Inject
    protected PasswordValidator passwordValidator;
    @Inject
    protected Bus mBus;
    @Inject
    protected EmailsAdapterFactory mEmailsAdapterFactory;

    private Delegate mDelegate;
    private AutoCompleteTextView mEmailEditText;
    private EditText mPasswordEditText;
    private CheckBox autoCreateChatBoxCheckBox;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mEmailEditText = (AutoCompleteTextView) view.findViewById(R.id.email);
        mPasswordEditText = (EditText) view.findViewById(R.id.password);
        autoCreateChatBoxCheckBox = (CheckBox) view.findViewById(R.id.auto_create_chatbox);
        CheckBox showPasswordCheckBox = (CheckBox) view.findViewById(R.id.show_password);
        TextView agreeConditionsTextView = (TextView) view.findViewById(R.id.agree_conditions);
        Button registerButton = (Button) view.findViewById(R.id.register);

        showPasswordCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT
                            | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                } else {
                    mPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT
                            | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
                // Place the cursor to the end of current text
                mPasswordEditText.setSelection(mPasswordEditText.getText().length());
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = mEmailEditText.getText().toString();
                String password = mPasswordEditText.getText().toString();

                try {
                    emailValidator.validate(email);
                    passwordValidator.validate(password);
                    mDelegate.register(email, password, true,
                            autoCreateChatBoxCheckBox.isChecked());
                } catch (EmailValidator.InvalidEmailException ex) {
                    mEmailEditText.setError(getString(R.string.error_invalid_email));
                } catch (PasswordValidator.InvalidPasswordException ex) {
                    mPasswordEditText.setError(getString(R.string.error_invalid_password));
                }
            }
        });

        agreeConditionsTextView.setMovementMethod(LinkMovementMethod.getInstance());
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
    protected void onAttachToContext(Context context) {
        if (context instanceof Delegate) {
            mDelegate = (Delegate) context;
        }
    }

    public void setEmailError(String error) {
        mEmailEditText.setError(error);
    }

    public void setPasswordError(String error) {
        mPasswordEditText.setError(error);
    }

}
