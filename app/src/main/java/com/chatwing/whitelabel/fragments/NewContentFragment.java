/*
 * Copyright (C) 2014 ChatWing
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chatwing.whitelabel.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.chatwing.whitelabel.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Huy Nguyen
 * Date: 7/8/13
 * Time: 10:41 AM
 */
public class NewContentFragment extends DialogFragment {
    private static final String EXTRA_HAS_BBCODE_OPTION = "bbcodes";

    public enum Item {
        BBCODES
    }

    public interface Listener {
        void onItemClicked(Item item);
    }

    public static NewContentFragment newInstance(boolean hasBBCodeOption) {
        NewContentFragment instance = new NewContentFragment();

        Bundle args = new Bundle();
        args.putBoolean(EXTRA_HAS_BBCODE_OPTION, hasBBCodeOption);
        instance.setArguments(args);

        return instance;
    }

    private Listener mListener;
    private boolean mHasBBCodeOption;

    public NewContentFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (Listener) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHasBBCodeOption = getArguments().getBoolean(EXTRA_HAS_BBCODE_OPTION);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final List<Item> items = new ArrayList<Item>();
        List<String> labels = new ArrayList<String>();
        if (mHasBBCodeOption) {
            items.add(Item.BBCODES);
            labels.add(getString(R.string.title_bbcodes));
        }
        String[] labelsArray = labels.toArray(new String[labels.size()]);
        return new AlertDialog.Builder(getActivity(), R.style.Theme_ChatWing_AlertDialog)
                .setItems(labelsArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onItemClicked(items.get(which));
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                })
                .create();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
