package com.chatwing.whitelabel.activities;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.events.UpdateUserEvent;
import com.chatwing.whitelabel.fragments.SettingsFragment;
import com.chatwing.whitelabel.modules.PreferenceActivityModule;
import com.chatwing.whitelabel.services.DownloadUserDetailIntentService;
import com.squareup.otto.Subscribe;

import java.util.Arrays;
import java.util.List;

/**
 * Author: Huy Nguyen
 * Date: 6/14/13
 * Time: 10:11 AM
 */
public class MainPreferenceActivity
        extends BaseABFragmentActivity {

    private ProgressBar mLoadingView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        mLoadingView = (ProgressBar) findViewById(R.id.progress_bar);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        startSyncUserProfile();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mBus.register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBus.unregister(this);
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


    private void showLoading(boolean show) {
        findViewById(R.id.loading_text).setVisibility(show ? View.VISIBLE : View.GONE);
        mLoadingView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Subscribe
    public void onUpdateUserProfileEvent(UpdateUserEvent event) {
        showLoading(false);
        Exception exception = event.getException();
        if (exception != null) {
            mErrorMessageView.show(exception,
                    getString(R.string.error_failed_to_update_user_profile));
            finish();
            return;
        }

        String settingFragmentTag = getString(R.string.fragment_tag_settings);
        Fragment settingFragment = getFragmentManager()
                .findFragmentByTag(settingFragmentTag);
        if (settingFragment == null) {
            settingFragment = new SettingsFragment();
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.setting_containner, settingFragment, settingFragmentTag)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void startSyncUserProfile() {
        showLoading(true);
        Intent service = new Intent(this, DownloadUserDetailIntentService.class);
        startService(service);
    }
}
