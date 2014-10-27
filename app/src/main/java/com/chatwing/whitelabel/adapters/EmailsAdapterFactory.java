package com.chatwing.whitelabel.adapters;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.util.Patterns;
import android.widget.ArrayAdapter;


import com.chatwingsdk.modules.ForActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

/**
 * Created by nguyenthanhhuy on 3/19/14.
 */
public class EmailsAdapterFactory {
    @Inject
    @ForActivity
    Context mContext;
    @Inject
    @ForActivity
    AccountManager mAccountManager;

    public ArrayAdapter<String> build() {
        Account[] accounts = mAccountManager.getAccounts();
        Set<String> emails = new HashSet<String>();
        for (Account account : accounts) {
            if (Patterns.EMAIL_ADDRESS.matcher(account.name).matches()) {
                emails.add(account.name);
            }
        }
        ArrayAdapter<String> emailsAdapter = new ArrayAdapter<String>(
                mContext,
                android.R.layout.simple_list_item_1,
                new ArrayList<String>(emails));
        return emailsAdapter;
    }
}
