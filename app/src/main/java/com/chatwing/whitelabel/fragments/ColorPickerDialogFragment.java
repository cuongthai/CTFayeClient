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
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;

import com.chatwing.whitelabel.R;

import net.margaritov.preference.colorpicker.ColorPickerView;

import java.io.Serializable;

/**
 * Author: Huy Nguyen
 * Date: 6/11/13
 * Time: 5:34 PM
 */
public class ColorPickerDialogFragment extends DialogFragment
        implements DialogInterface.OnClickListener {

    public static final String EXTRA_TAG = "tag";

    public interface Listener {
        void onConfirmColor(Serializable tag, int color);
    }

    public static ColorPickerDialogFragment newInstance(Serializable tag) {
        ColorPickerDialogFragment instance = new ColorPickerDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_TAG, tag);
        instance.setArguments(args);
        return instance;
    }

    private ColorPickerView mColorPickerView;
    private Listener mListener;
    private Serializable mTag;

    public ColorPickerDialogFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (Listener) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTag = getArguments().getSerializable(EXTRA_TAG);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.title_pick_color)
                .setView(inflateContentView())
                .setPositiveButton(android.R.string.ok, this)
                .setNegativeButton(android.R.string.cancel, this);
        return builder.create();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                mListener.onConfirmColor(mTag, mColorPickerView.getColor());
                break;
            case DialogInterface.BUTTON_NEGATIVE:
            default:
                break;
        }
    }

    private View inflateContentView() {
        getActivity().getWindow().setFormat(PixelFormat.RGBA_8888);
        View v = View.inflate(getActivity(),
                R.layout.fragment_dialog_color_picker, null);
        mColorPickerView = (ColorPickerView) v.findViewById(R.id.color_picker_view);
        return v;
    }
}
