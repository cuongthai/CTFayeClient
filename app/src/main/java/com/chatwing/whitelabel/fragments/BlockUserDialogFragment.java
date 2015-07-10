package com.chatwing.whitelabel.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.events.BlockedEvent;
import com.chatwing.whitelabel.events.CancelBlockEvent;
import com.chatwing.whitelabel.events.RequestBlockIPEvent;
import com.chatwing.whitelabel.events.RequestBlockTypeEvent;
import com.chatwing.whitelabel.managers.ApiManager;
import com.chatwing.whitelabel.pojos.BaseUser;
import com.chatwing.whitelabel.pojos.Message;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

/**
 * Created by steve on 30/06/2014.
 */
public class BlockUserDialogFragment extends DialogFragment
        implements DialogInterface.OnClickListener {
    private static final String MESSAGE_KEY = "message";
    @Inject
    Bus mBus;
    private EditText mReasonEditText;
    private EditText mDurationEditText;
    private Spinner mDurationTitleEditText;
    private CheckBox mClearMessageCheckBox;
    private boolean mIsDismissingByUser;
    private Message message;
    private ProgressBar mProgressBar;

    public static BlockUserDialogFragment newInstance(Message message) {
        BlockUserDialogFragment blockUserDialogFragment = new BlockUserDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(MESSAGE_KEY, message);
        blockUserDialogFragment.setArguments(bundle);
        return blockUserDialogFragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((InjectableFragmentDelegate) getActivity()).inject(this);
        mIsDismissingByUser = false;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        message = (Message) getArguments().getSerializable(MESSAGE_KEY);
        setCancelable(false);
        Activity activity = getActivity();
        LayoutInflater layoutInflater = LayoutInflater.from(activity);
        View promptView = layoutInflater.inflate(R.layout.fragment_block_user, null);
        mReasonEditText = (EditText) promptView.findViewById(R.id.reason);
        mDurationEditText = (EditText) promptView.findViewById(R.id.duration);
        mDurationTitleEditText = (Spinner) promptView.findViewById(R.id.duration_title);
        mClearMessageCheckBox = (CheckBox) promptView.findViewById(R.id.clear_message);
        mProgressBar = (ProgressBar) promptView.findViewById(R.id.progressBar);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.Theme_ChatWing_AlertDialog)
                .setCancelable(false)
                .setTitle(R.string.message_block_user)
                .setView(promptView)
                .setNeutralButton(R.string.title_block_ip, this)
                .setNegativeButton(R.string.title_cancel, this);
        if (!isFromGuest(message)) {
            builder.setPositiveButton(String.format(getString(R.string.title_block_type),
                    message.getUserType()), this);
        }
        return builder.create();
    }

    private boolean isFromGuest(Message message) {
        return BaseUser.isGuest(message.getUserType());
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        //Leave empty
    }

    private void onClick(int which) {
        if (TextUtils.isEmpty(mReasonEditText.getText())) {
            mReasonEditText.setError(getString(R.string.error_invalid_chat_box_id));
            mProgressBar.setVisibility(View.GONE);
            return;
        }

        if (TextUtils.isEmpty(mDurationEditText.getText())
                || Long.valueOf(mDurationEditText.getText().toString()) <= 0) {
            mProgressBar.setVisibility(View.GONE);
            mDurationEditText.setError(getString(R.string.error_duration_cant_blank));
            return;
        }
        mProgressBar.setVisibility(View.VISIBLE);

        long duration = getDurationInMilliSeconds(Long.valueOf(mDurationEditText.getText().toString()),
                mDurationTitleEditText);
        boolean clearMessage = mClearMessageCheckBox.isChecked();
        mIsDismissingByUser = true;
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                mBus.post(new RequestBlockTypeEvent(message,
                        clearMessage,
                        mReasonEditText.getText().toString(),
                        duration));
                break;
            case DialogInterface.BUTTON_NEUTRAL:
                mBus.post(new RequestBlockIPEvent(message,
                        clearMessage,
                        mReasonEditText.getText().toString(),
                        duration));
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        addListenerForButtons();
    }

    /**
     * The following trick is to prevent "auto dismissing dialog" when clicking buttons.
     * We want to manually handle that dismissing behaviour
     */
    private void addListenerForButtons() {
        AlertDialog d = (AlertDialog) getDialog();
        if (d != null) {
            Button positiveButton = d.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BlockUserDialogFragment.this.onClick(DialogInterface.BUTTON_POSITIVE);
                }
            });

            Button negativeButton = d.getButton(Dialog.BUTTON_NEGATIVE);
            negativeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mBus.post(new CancelBlockEvent());
                    mProgressBar.setVisibility(View.GONE);
                    dismiss();
                }
            });

            Button neutralButton = d.getButton(Dialog.BUTTON_NEUTRAL);
            neutralButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BlockUserDialogFragment.this.onClick(DialogInterface.BUTTON_NEUTRAL);
                }
            });
        }
    }

    private long getDurationInMilliSeconds(long duration, Spinner mDurationTitleEditText) {
        long scale = Long.valueOf(getResources().getStringArray(R.array.block_duration_type_ids)
                [mDurationTitleEditText.getSelectedItemPosition()]);

        return duration * scale * 1000;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        mIsDismissingByUser = true;
        mBus.post(new CancelBlockEvent());
    }

    public boolean isDismissingByUser() {
        return mIsDismissingByUser;
    }


    @Subscribe
    public void onBlockedUser(BlockedEvent event) {
        mProgressBar.setVisibility(View.GONE);
        Exception exception = event.getException();
        if (exception != null) {
            if (exception instanceof ApiManager.ValidationException) {
                mReasonEditText.setError(getString(R.string.error_invalid_chat_box_id));
            }
        }
    }
}
