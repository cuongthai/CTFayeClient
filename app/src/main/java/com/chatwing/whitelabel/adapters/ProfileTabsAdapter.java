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

package com.chatwing.whitelabel.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;

import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.events.ViewProfileEvent;
import com.chatwing.whitelabel.fragments.ProfileInfoFragment;


/**
 * Created by steve on 3/4/14.
 */
public class ProfileTabsAdapter extends FragmentPagerAdapter {
    private static final int[] ICONS = new int[]{R.drawable.com_facebook_profile_default_icon};
    private static final int[] TITLES = new int[]{R.string.title_profile_tab};
    private final Context mContext;
    private final ViewProfileEvent mProfile;

    public ProfileTabsAdapter(Context context,
                              android.support.v4.app.FragmentManager childFragmentManager,
                              ViewProfileEvent profile) {
        super(childFragmentManager);
        mContext = context;
        mProfile = profile;
    }

    @Override
    public Fragment getItem(int i) {
        switch (i) {
            default:
                return ProfileInfoFragment.newInstance(mProfile);
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getString(TITLES[position]);
    }

    @Override
    public int getCount() {
        return ICONS.length;
    }
}
