package com.chatwing.whitelabel.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.fragments.EmoticonsFragment;
import com.chatwing.whitelabel.pojos.Emoticon;
import com.viewpagerindicator.IconPagerAdapter;

import java.util.Map;


/**
 * Created by steve on 05/03/2015.
 */
public class EmoticonPackagesAdapter extends FragmentStatePagerAdapter implements IconPagerAdapter {
    private static String[] CONTENT;

    private final Map<String, Emoticon[]> packages;

    public EmoticonPackagesAdapter(FragmentManager fm, Map<String, Emoticon[]> packages) {
        super(fm);
        this.packages = packages;
        CONTENT = packages.keySet().toArray(new String[packages.size()]);
    }

    @Override
    public Fragment getItem(int position) {
        return EmoticonsFragment.newInstance(packages.get(CONTENT[position]));
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "";
    }

    @Override
    public int getIconResId(int index) {
        return R.drawable.ic_emoticons;
    }

    @Override
    public int getCount() {
        return packages.size();
    }


}
