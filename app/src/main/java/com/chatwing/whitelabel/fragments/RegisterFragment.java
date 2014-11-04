package com.chatwing.whitelabel.fragments;

import android.app.Activity;
import android.graphics.Typeface;
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
import com.chatwingsdk.fragments.InjectableFragmentDelegate;
import com.squareup.otto.Bus;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Created by nguyenthanhhuy on 4/14/14.
 */
public class RegisterFragment extends Fragment {
    public interface Delegate extends InjectableFragmentDelegate {
        public void register(String email, String password,
                             boolean agreeConditions, boolean autoCreateChatbox);
    }

    @Inject
    EmailValidator emailValidator;
    @Inject
    PasswordValidator passwordValidator;
    @Inject
    Bus mBus;
    @Inject
    EmailsAdapterFactory mEmailsAdapterFactory;
    @Inject
    Provider<Typeface> mIconTypefaceProvider;
    private Delegate mDelegate;
    private AutoCompleteTextView mEmailEditText;
    private EditText mPasswordEditText;

    public RegisterFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mDelegate = (Delegate) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mEmailEditText = (AutoCompleteTextView) view.findViewById(R.id.email);
        mPasswordEditText = (EditText) view.findViewById(R.id.password);
        CheckBox showPasswordCheckBox = (CheckBox) view.findViewById(R.id.show_password);
        final CheckBox autoCreateChatBoxCheckBox = (CheckBox) view.findViewById(R.id.auto_create_chatbox);
        TextView agreeConditionsTextView = (TextView) view.findViewById(R.id.agree_conditions);
        Button registerButton = (Button) view.findViewById(R.id.register);
        registerButton.setText(getString(R.string.title_register_chatwing, getString(R.string.app_name)));
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

        agreeConditionsTextView.setMovementMethod(LinkMovementMethod.getInstance());

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

    public void setEmailError(String error) {
        mEmailEditText.setError(error);
    }

    public void setPasswordError(String error) {
        mPasswordEditText.setError(error);
    }
}
