package com.chatwing.whitelabel.fragments;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.view.MenuItem;
import android.view.View;

import com.chatwing.whitelabel.BuildConfig;
import com.chatwing.whitelabel.Constants;
import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.modules.ExtendChatWingModule;
import com.chatwing.whitelabel.modules.PreferenceActivityModule;
import com.chatwing.whitelabel.services.UpdateUserProfileService;
import com.chatwingsdk.ChatWing;
import com.chatwingsdk.events.internal.UpdateUserEvent;
import com.chatwingsdk.fragments.InjectableFragmentDelegate;
import com.chatwingsdk.managers.UserManager;
import com.chatwingsdk.pojos.UserProfile;
import com.chatwingsdk.views.ErrorMessageView;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

/**
 * Created by steve on 17/12/2014.
 */
public class SettingsFragment extends PreferenceFragment
        implements Preference.OnPreferenceClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener,
        Preference.OnPreferenceChangeListener {

    private EditTextPreference mNamePreference;
    private Preference mFeedbackPreference;
    private PreferenceCategory mChatBoxCategoryPreference;
    private boolean mIsProfileChanged;
    private String mVersion;
    private UserProfile mOldUserProfile;
    private Preference mClearPreference;

    @Inject
    Bus mBus;
    @Inject
    UserManager mUserManager;
    @Inject
    ErrorMessageView mErrorMessageView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_main);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ChatWing.instance(activity).getChatwingGraph().plus(new PreferenceActivityModule(activity)).inject(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mNamePreference = (EditTextPreference) findPreference(getString(R.string.preference_name));
        mFeedbackPreference = findPreference(getString(R.string.preference_feedback));
        mChatBoxCategoryPreference = (PreferenceCategory) findPreference(getString(R.string.preference_category_chat_box));

        mClearPreference = findPreference(getString(R.string.preference_clear_previous_style));
        String name = mNamePreference.getSharedPreferences().getString(
                mNamePreference.getKey(),
                getString(R.string.summary_name));
        mNamePreference.setSummary(name);
        mFeedbackPreference.setOnPreferenceClickListener(this);
        mNamePreference.setOnPreferenceChangeListener(this);
        mClearPreference.setOnPreferenceClickListener(this);

        String versionName;
        int versionCode;
        try {
            PackageInfo info = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            versionName = info.versionName;
            versionCode = info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            versionName = BuildConfig.VERSION_NAME;
            versionCode = BuildConfig.VERSION_CODE;
        }
        mVersion = new StringBuilder(versionName)
                .append(" (").append(versionCode).append(")")
                .toString();

        findPreference(getString(R.string.preference_version)).setSummary(mVersion);

        updateUserViews();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference.getKey().equals(mNamePreference.getKey())) {
            int newLength = ((String) newValue).length();
            if (newLength < 3 || newLength > 32) {
                mErrorMessageView.show(R.string.error_invalid_name_length);
                return false;
            }
        }
        mIsProfileChanged = true;
        return true;
    }


    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        if (key.equals(mClearPreference.getKey())) {
            mUserManager.removeStyle();
            return true;
        } else if (key.equals(mFeedbackPreference.getKey())) {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("message/rfc822");
            i.putExtra(Intent.EXTRA_EMAIL, new String[]{Constants.FEEDBACK_EMAIL});
            i.putExtra(Intent.EXTRA_SUBJECT, "Android app feedback: v" + mVersion);
            try {
                startActivity(i);
            } catch (ActivityNotFoundException ex) {
                mErrorMessageView.show(R.string.error_cant_handle_email);
            }
            return true;
        }
        return false;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (mNamePreference.getKey().equals(key)) {
            String newName = sharedPreferences.getString(
                    key,
                    getString(R.string.summary_name));
            mNamePreference.setSummary(newName);
        } else if (key.equals(getString(R.string.preference_remember_previous_style))) {
            boolean checked = sharedPreferences.getBoolean(
                    key,
                    getResources().getBoolean(R.bool.default_remember_previous_style));
            if (!checked) {
                mUserManager.removeStyle();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mIsProfileChanged = false;
        mOldUserProfile = mUserManager.getCurrentUser() == null
                ? null
                : mUserManager.getCurrentUser().getProfile();
    }

    @Override
    public void onResume() {
        super.onResume();
        mBus.register(this);
        mNamePreference.getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mNamePreference.getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
        mBus.unregister(this);
    }


    @Override
    public void onStop() {
        super.onStop();
        if (mOldUserProfile != null && mIsProfileChanged) {
            mUserManager.invalidateUser();
            Intent i = new Intent(getActivity(), UpdateUserProfileService.class);
            i.putExtra(UpdateUserProfileService.EXTRA_OLD_PROFILE, mOldUserProfile);
            getActivity().startService(i);
        }
    }

    @Subscribe
    public void onUpdateUserProfileEvent(UpdateUserEvent event) {
        Exception exception = event.getException();
        if (exception != null) {
            mErrorMessageView.show(exception,
                    getString(R.string.error_failed_to_update_user_profile));
        }

        // Refresh the view. Since PreferenceActivity doesn't provide a way,
        // we need to work around.
        // Also, if user makes new changes recently (steps: he makes changes,
        // goes back to ChatActivity, then quickly opens and makes some more
        // changes), just ignore the new changes because we don't want to
        // recursively call the service and we can't keep track of old user
        // profile.
        mIsProfileChanged = false;
        startActivity(getActivity().getIntent());
        getActivity().finish();
    }

    private void updateUserViews() {
        if (mUserManager.userCanChangeSettings()) {
            mChatBoxCategoryPreference.setEnabled(true);
        } else {
            mChatBoxCategoryPreference.setEnabled(false);
        }
    }

}
