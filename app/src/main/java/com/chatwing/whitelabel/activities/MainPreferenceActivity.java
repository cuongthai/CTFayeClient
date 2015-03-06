package com.chatwing.whitelabel.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.support.v4.app.Fragment;
import android.view.MenuItem;
import android.view.Window;

import com.chatwing.whitelabel.ChatWingApplication;
import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.fragments.SettingsFragment;
import com.chatwing.whitelabel.modules.ExtendCommunicationActivityModule;
import com.chatwing.whitelabel.modules.PreferenceActivityModule;
import com.chatwingsdk.activities.BaseABFragmentActivity;
import com.chatwingsdk.fragments.InjectableFragmentDelegate;
import com.chatwingsdk.managers.UserManager;
import com.chatwingsdk.modules.ChatWingModule;
import com.chatwingsdk.modules.CommunicationActivityModule;
import com.chatwingsdk.pojos.UserProfile;
import com.chatwingsdk.views.ErrorMessageView;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

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
