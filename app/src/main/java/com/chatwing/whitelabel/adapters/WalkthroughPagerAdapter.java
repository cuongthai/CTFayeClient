package com.chatwing.whitelabel.adapters;

import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;


import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.fragments.WalkthroughPageFragment;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by nguyenthanhhuy on 4/3/14.
 */
public class WalkthroughPagerAdapter extends FragmentStatePagerAdapter {
    private Resources mRes;
    private List<WalkthroughPageFragment.Info> mInfos;
    private WalkthroughPageFragment.Padding mFragmentPadding;

    @Inject
    WalkthroughPagerAdapter(FragmentManager fm, Resources res) {
        super(fm);
        mRes = res;
        mInfos = new ArrayList<WalkthroughPageFragment.Info>();
    }

    public void setFragmentPadding(WalkthroughPageFragment.Padding fragmentPadding) {
        mFragmentPadding = fragmentPadding;
        populateInfos();
    }

    @Override
    public Fragment getItem(int position) {
        return WalkthroughPageFragment.newInstance(mInfos.get(position));
    }

    @Override
    public int getCount() {
        return mInfos.size();
    }

    private void populateInfos() {
        mInfos.clear();
        mInfos.add(new WalkthroughPageFragment.Info(
                R.drawable.walkthrough_page_1_background,
                mRes.getColor(R.color.walkthrough_page_1_background_color),
                R.string.message_walkthrough_page_1_main_text,
                R.string.message_walkthrough_page_1_secondary_text,
                0,
                mFragmentPadding));
        mInfos.add(new WalkthroughPageFragment.Info(
                0,
                mRes.getColor(R.color.walkthrough_page_2_background_color),
                R.string.message_walkthrough_page_2_main_text,
                R.string.message_walkthrough_page_2_secondary_text,
                R.drawable.walkthrough_page_2_image,
                mFragmentPadding));
        mInfos.add(new WalkthroughPageFragment.Info(
                0,
                mRes.getColor(R.color.walkthrough_page_3_background_color),
                R.string.message_walkthrough_page_3_main_text,
                R.string.message_walkthrough_page_3_secondary_text,
                R.drawable.walkthrough_page_3_image,
                mFragmentPadding));
        mInfos.add(new WalkthroughPageFragment.Info(
                0,
                mRes.getColor(R.color.walkthrough_page_4_background_color),
                R.string.message_walkthrough_page_4_main_text,
                R.string.message_walkthrough_page_4_secondary_text,
                R.drawable.walkthrough_page_4_image,
                mFragmentPadding));
        mInfos.add(new WalkthroughPageFragment.Info(
                0,
                mRes.getColor(R.color.walkthrough_page_5_background_color),
                R.string.message_walkthrough_page_5_main_text,
                R.string.message_walkthrough_page_5_secondary_text,
                R.drawable.walkthrough_page_5_image,
                mFragmentPadding));
        notifyDataSetChanged();
    }
}
