package com.chatwing.whitelabel.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.chatwing.whitelabel.R;
import com.chatwingsdk.fragments.CommunicationDrawerFragment;

/**
 * Created by steve on 17/12/2014.
 */
public class ExtendCommunicationDrawerFragment extends CommunicationDrawerFragment {
    private Listener mListener;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.showSettings();
            }
        });
        mUserAvatarView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.updateAvatar();
            }
        });
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (Listener) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @com.squareup.otto.Subscribe
    public void onUpdateUserEvent(com.chatwingsdk.events.internal.UpdateUserEvent event) {
        super.onUpdateUserEvent(event);
    }

    public static interface Listener extends CommunicationDrawerFragment.Listener {
        void showSettings();

        void updateAvatar();
    }

}
