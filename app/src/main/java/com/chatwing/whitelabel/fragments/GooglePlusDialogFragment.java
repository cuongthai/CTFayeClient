package com.chatwing.whitelabel.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.chatwing.whitelabel.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

/**
 * Author: Huy Nguyen
 * Date: 9/1/13
 * Time: 11:14 AM
 */
public class GooglePlusDialogFragment extends DialogFragment {
    private static final String EXTRA_ERROR_CODE = "error_code";
    private static final String EXTRA_REQUEST_CODE = "request_code";

    public static GooglePlusDialogFragment newInstance(int errorCode,
                                                       int requestCode) {
        if (errorCode == ConnectionResult.SUCCESS) {
            return null;
        }

        GooglePlusDialogFragment fragment = new GooglePlusDialogFragment();
        Bundle args = new Bundle(1);
        args.putInt(EXTRA_ERROR_CODE, errorCode);
        args.putInt(EXTRA_REQUEST_CODE, requestCode);
        fragment.setArguments(args);
        return fragment;
    }

    public GooglePlusDialogFragment() {
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        int errorCode = args.getInt(EXTRA_ERROR_CODE);
        int requestCode = args.getInt(EXTRA_REQUEST_CODE);

        if (GooglePlayServicesUtil.isUserRecoverableError(errorCode)) {
            return GooglePlayServicesUtil.getErrorDialog(
                    errorCode,
                    getActivity(),
                    requestCode);
        }
        return new AlertDialog.Builder(getActivity())
                .setMessage(R.string.error_unavailable_google_plus_login)
                .setCancelable(true)
                .create();
    }
}
