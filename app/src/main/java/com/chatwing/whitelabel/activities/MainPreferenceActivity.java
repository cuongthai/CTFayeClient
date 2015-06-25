package com.chatwing.whitelabel.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;

import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.fragments.SettingsFragment;
import com.chatwing.whitelabel.modules.PreferenceActivityModule;

import java.util.Arrays;
import java.util.List;

/**
 * Author: Huy Nguyen
 * Date: 6/14/13
 * Time: 10:11 AM
 */
public class MainPreferenceActivity extends BaseABFragmentActivity implements SettingsFragment.SettingDelegate {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_preferences);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    protected List<Object> getModules() {
        return Arrays.<Object>asList(new PreferenceActivityModule(this));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void showLoading(boolean show) {
        setSupportProgressBarIndeterminateVisibility(show);
    }
}
