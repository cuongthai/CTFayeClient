package com.chatwing.whitelabel.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.chatwing.whitelabel.R;
import com.chatwingsdk.fragments.InjectableFragmentDelegate;
import com.chatwingsdk.utils.LogUtils;
import com.soundcloud.android.crop.Crop;
import com.squareup.otto.Bus;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

/**
 * Created by steve on 05/07/2014.
 */
public class PhotoPickerDialogFragment extends DialogFragment {
    private static final int REQUEST_IMAGE_LIBRARY_PICKER_SELECT = 1212;
    private static final int REQUEST_IMAGE_CAMERA_PICKER_SELECT = 1213;
    private InjectableFragmentDelegate mDelegate;
    private TextView mFromLibrary;
    private TextView mFromCamera;
    private Uri photoSourceUri;

    public static PhotoPickerDialogFragment newInstance() {
        PhotoPickerDialogFragment accountDialogFragment = new PhotoPickerDialogFragment();
        return accountDialogFragment;
    }

    @Inject
    Bus mBus;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Activity activity = getActivity();
        LayoutInflater layoutInflater = LayoutInflater.from(activity);
        View promptView = layoutInflater.inflate(R.layout.fragment_dialog_photo_picker, null);

        mFromCamera = (TextView) promptView.findViewById(R.id.from_camera);
        mFromCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File sourceAvatar = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES), "src_avatar");
                photoSourceUri = Uri.fromFile(sourceAvatar);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoSourceUri);
                if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAMERA_PICKER_SELECT);
                }
            }
        });

        mFromLibrary = (TextView) promptView.findViewById(R.id.from_gallery);
        mFromLibrary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, REQUEST_IMAGE_LIBRARY_PICKER_SELECT);
            }
        });

        if (!getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            mFromCamera.setVisibility(View.GONE);
        }


        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setCancelable(false)
                .setTitle(R.string.message_avatar_picker)
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_IMAGE_CAMERA_PICKER_SELECT:
                    startCropActivity(photoSourceUri);
                    break;
                case REQUEST_IMAGE_LIBRARY_PICKER_SELECT:
                    startCropActivity(intent.getData());
                    break;
            }
        }
        dismiss();
    }

    private void startCropActivity(Uri uri) {
        if (uri != null) {
            File cropped = new File(getActivity().getCacheDir(), "cropped");
            Uri outputUri = Uri.fromFile(cropped);
            new Crop(uri).output(outputUri).asSquare().start(getActivity());
        }
    }
}
