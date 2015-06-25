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
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.adapters.ProfileTabsAdapter;
import com.chatwing.whitelabel.events.ViewProfileEvent;
import com.chatwing.whitelabel.managers.UserManager;
import com.chatwing.whitelabel.managers.VolleyManager;
import com.chatwing.whitelabel.pojos.params.CreateConversationParams;
import com.chatwing.whitelabel.views.RatioImageView;
import com.squareup.otto.Bus;
import com.viewpagerindicator.TabPageIndicator;

import javax.inject.Inject;

/**
 * Created by steve on 3/4/14.
 */
public class ProfileFragment extends DialogFragment {
    private static final String PROFILE_KEY = "PROFILE_KEY";
    private static final String TAG = "ProfileFragment";
    private RatioImageView mSmallAvatar;

    public interface Listener extends InjectableFragmentDelegate {
        void showConversation(CreateConversationParams.SimpleUser simpleUser);
    }

    public static ProfileFragment newInstance(ViewProfileEvent profile) {
        ProfileFragment profileFragment = new ProfileFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(PROFILE_KEY, profile);
        profileFragment.setArguments(bundle);
        return profileFragment;
    }

    @Inject
    VolleyManager mVolleyManager;
    @Inject
    UserManager mUserManager;
    @Inject
    Bus mBus;

    private RatioImageView mAvatar;
    private TextView mName;
    private ViewPager mViewPager;
    private ImageButton mReplyButton;
    private Listener mDelegate;

    public ProfileFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mDelegate = (Listener) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDelegate.inject(this);
        Bundle arguments = getArguments();
        if (arguments == null) {
            dismiss();
            return;
        }

        final ViewProfileEvent profile = (ViewProfileEvent) arguments.getSerializable(PROFILE_KEY);

        mAvatar = (RatioImageView) getView().findViewById(R.id.profile_photo);
        mSmallAvatar = (RatioImageView) getView().findViewById(R.id.profile_small_photo);
        mName = (TextView) getView().findViewById(R.id.profile_name);
        mViewPager = (ViewPager) getView().findViewById(R.id.pager);
        mReplyButton = (ImageButton) getView().findViewById(R.id.profile_reply);

        if (profile.isDenyReply()) {
            mReplyButton.setVisibility(View.GONE);
        } else {
            mReplyButton.setVisibility(View.VISIBLE);
        }

        mAvatar.setImageUrl(profile.getAvatarUrl(), mVolleyManager.getImageLoader());
        mSmallAvatar.setImageUrl(profile.getAvatarUrl(), mVolleyManager.getImageLoader());
        //Load fragments
        PagerAdapter adapter = buildAdapter(profile);
        mViewPager.setAdapter(adapter);

        TabPageIndicator indicator = (TabPageIndicator) getView().findViewById(R.id.indicator);
        indicator.setViewPager(mViewPager);

        mName.setText(profile.getUserName());

        //Reply listener
        mReplyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDelegate.showConversation(new CreateConversationParams.SimpleUser(profile.getLoginId(), profile.getUserType()));
                dismiss();
            }
        });

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mDelegate = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        mBus.register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mBus.unregister(this);
    }

    private PagerAdapter buildAdapter(ViewProfileEvent profile) {
        return new ProfileTabsAdapter(getActivity(), getChildFragmentManager(), profile);
    }
}
