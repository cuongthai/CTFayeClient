package com.chatwing.whitelabel.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.events.PasswordEnteredEvent;
import com.chatwing.whitelabel.events.PasswordRefusedEvent;
import com.squareup.otto.Bus;

import javax.inject.Inject;

/**
 * Created by nguyenthanhhuy on 6/18/14.
 */
public class PasswordDialogFragment extends DialogFragment
        implements DialogInterface.OnClickListener {

    @Inject
    Bus mBus;
    private EditText mPasswordEditText;
    private CheckBox mRememberPasswordCheckBox;
    private boolean mIsDismissingByUser;

    public boolean isDismissingByUser() {
        return mIsDismissingByUser;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((InjectableFragmentDelegate) getActivity()).inject(this);
        mIsDismissingByUser = false;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Activity activity = getActivity();
        LayoutInflater layoutInflater = LayoutInflater.from(activity);
        View promptView = layoutInflater.inflate(R.layout.fragment_dialog_password_chatbox, null);
        mPasswordEditText = (EditText) promptView.findViewById(R.id.password);
        mRememberPasswordCheckBox = (CheckBox) promptView.findViewById(R.id.remember);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setCancelable(false)
                .setTitle(R.string.message_input_password_chatbox)
                .setView(promptView)
                .setPositiveButton(R.string.title_ok, this)
                .setNegativeButton(R.string.title_cancel, this);
        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        mIsDismissingByUser = true;
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                mBus.post(new PasswordEnteredEvent(
                        mPasswordEditText.getText().toString(),
                        mRememberPasswordCheckBox.isChecked()));
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                mBus.post(new PasswordRefusedEvent());
                break;
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        mIsDismissingByUser = true;
        mBus.post(new PasswordRefusedEvent());
    }
}
