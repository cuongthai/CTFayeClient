package com.chatwing.whitelabel.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.activities.WalkthroughActivity;
import com.chatwing.whitelabel.adapters.AccountPickerAdapter;
import com.chatwing.whitelabel.events.AccountSwitchEvent;
import com.chatwing.whitelabel.events.UpdateUserEvent;
import com.chatwing.whitelabel.managers.UserManager;
import com.chatwing.whitelabel.pojos.User;
import com.chatwing.whitelabel.utils.LogUtils;
import com.chatwing.whitelabel.views.ErrorMessageView;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.Collection;

import javax.inject.Inject;

/**
 * Created by steve on 05/07/2014.
 */
public class AccountDialogFragment extends DialogFragment {
    public static final int REQUEST_ADD_NEW_AUTHENTICATION = 69;
    private static final String MESSAGE = "message";
    private InjectableFragmentDelegate mDelegate;
    private TextView mMessageView;

    public static AccountDialogFragment newInstance(String message) {
        Bundle bundle = new Bundle();
        bundle.putString(MESSAGE, message);
        AccountDialogFragment accountDialogFragment = new AccountDialogFragment();
        accountDialogFragment.setArguments(bundle);
        return accountDialogFragment;
    }

    @Inject
    AccountPickerAdapter adapter;
    @Inject
    UserManager mUserManager;
    @Inject
    ErrorMessageView mErrorView;
    @Inject
    Bus mBus;
    private ListView mListView;
    private TextView mNewAccount;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        String message = arguments.getString(MESSAGE);
        Activity activity = getActivity();
        LayoutInflater layoutInflater = LayoutInflater.from(activity);
        View promptView = layoutInflater.inflate(R.layout.fragment_dialog_authentication, null);
        mListView = (ListView) promptView.findViewById(R.id.list);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                User selectedUser = adapter.getItem(i);
                if (selectedUser.equals(mUserManager.getCurrentUser())) {
                    return;
                }
                try {
                    mUserManager.activateUser(selectedUser.getId());
                    mBus.post(new AccountSwitchEvent(selectedUser));
                    Toast.makeText(getActivity(),
                            getString(R.string.message_account_switched),
                            Toast.LENGTH_LONG).show();
                    dismiss();
                } catch (UserManager.UserAccountNotFoundException e) {
                    mErrorView.show(R.string.error_account_not_found);
                    LogUtils.e(e);
                }
            }
        });
        mNewAccount = (TextView) promptView.findViewById(R.id.add_new_account);
        mNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), WalkthroughActivity.class);
                //We need to startActivityForResult from host activity not this fragment
                getActivity().startActivityForResult(intent, REQUEST_ADD_NEW_AUTHENTICATION);
            }
        });

        mMessageView = (TextView) promptView.findViewById(R.id.message);
        if (TextUtils.isEmpty(message)) {
            mMessageView.setVisibility(View.GONE);
        } else {
            mMessageView.setVisibility(View.VISIBLE);
            mMessageView.setText(message);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.Theme_ChatWing_AlertDialog)
                .setCancelable(false)
                .setTitle(R.string.message_account_picker)
                .setNegativeButton(getString(R.string.title_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dismiss();
                    }
                })
                .setView(promptView);
        return builder.create();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mDelegate.inject(this);
        mListView.setAdapter(adapter);
        reloadData();
    }

    private void reloadData() {
        adapter.clear();
        Collection<User> allUsers = mUserManager.getAllUsers();
        //Collapse listview
        if (allUsers.size() == 0) {
            mListView.setVisibility(View.GONE);
        } else {
            mListView.setVisibility(View.VISIBLE);
            adapter.addAllData(allUsers);
        }
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mDelegate = (InjectableFragmentDelegate) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mDelegate = null;
    }

    @Subscribe
    public void onUpdateUserProfileEvent(UpdateUserEvent event) {
        if (event.getException() == null) {
            reloadData();
        }
    }

}
