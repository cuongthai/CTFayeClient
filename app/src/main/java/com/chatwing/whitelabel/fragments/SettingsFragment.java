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
import android.view.View;

import com.chatwing.whitelabel.BuildConfig;
import com.chatwing.whitelabel.ChatWing;
import com.chatwing.whitelabel.Constants;
import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.events.UpdateUserEvent;
import com.chatwing.whitelabel.events.VerifyEmailEvent;
import com.chatwing.whitelabel.managers.UserManager;
import com.chatwing.whitelabel.modules.PreferenceActivityModule;
import com.chatwing.whitelabel.pojos.UserProfile;
import com.chatwing.whitelabel.services.UpdateUserProfileService;
import com.chatwing.whitelabel.services.VerifyEmailIntentService;
import com.chatwing.whitelabel.views.ErrorMessageView;
import com.chatwing.whitelabel.views.QuickMessageView;
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
    private EditTextPreference mEmailPreference;
    private Preference mFeedbackPreference;
    private PreferenceCategory mChatBoxCategoryPreference;
    private boolean mIsProfileChanged;
    private String mVersion;
    private UserProfile mOldUserProfile;
    private Preference mClearPreference;
    private Preference mSoundPreference;
    private Preference mVerifyPreference;

    @Inject
    Bus mBus;
    @Inject
    UserManager mUserManager;
    @Inject
    ErrorMessageView mErrorMessageView;
    @Inject
    QuickMessageView mQuickMessageView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_main);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ChatWing.instance(activity)
                .getChatwingGraph().plus(new PreferenceActivityModule(activity)).inject(this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mNamePreference = (EditTextPreference) findPreference(getString(R.string.preference_name));
        mEmailPreference = (EditTextPreference) findPreference(getString(R.string.preference_email));
        mFeedbackPreference = findPreference(getString(R.string.preference_feedback));
        mChatBoxCategoryPreference = (PreferenceCategory) findPreference(getString(R.string.preference_category_chat_box));

        mClearPreference = findPreference(getString(R.string.preference_clear_previous_style));
        mSoundPreference = findPreference(getString(R.string.preference_play_new_message_sound));
        mVerifyPreference = findPreference(getString(R.string.preference_verify_email));
        String name = mNamePreference.getSharedPreferences().getString(
                mNamePreference.getKey(),
                getString(R.string.summary_name));
        String email = mEmailPreference.getSharedPreferences().getString(
                mEmailPreference.getKey(),
                getString(R.string.summary_email));
        mNamePreference.setSummary(name);
        mEmailPreference.setSummary(email);
        mEmailPreference.setTitle(getString(R.string.title_preference_email));

        mNamePreference.setOnPreferenceChangeListener(this);
        mEmailPreference.setOnPreferenceChangeListener(this);
        mSoundPreference.setOnPreferenceChangeListener(this);

        mFeedbackPreference.setOnPreferenceClickListener(this);
        mClearPreference.setOnPreferenceClickListener(this);
        mVerifyPreference.setOnPreferenceClickListener(this);
        String versionName;
        int versionCode;
        try {
            PackageInfo info = getActivity().getPackageManager()
                    .getPackageInfo(getActivity().getPackageName(), 0);
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

        updateVerifyButtonState();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference.getKey().equals(mNamePreference.getKey())) {
            int newLength = ((String) newValue).length();
            if (newLength < 3 || newLength > 32) {
                mErrorMessageView.show(R.string.error_invalid_name_length);
                return false;
            }
        } else if (preference.getKey().equals(mEmailPreference.getKey())) {

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
        } else if (key.equals(mVerifyPreference.getKey())) {
            if (!VerifyEmailIntentService.isInProgress()) {
                getActivity().startService(new Intent(getActivity(), VerifyEmailIntentService.class));
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
        } else if (mEmailPreference.getKey().equals(key)) {
            String newName = sharedPreferences.getString(
                    key,
                    getString(R.string.summary_email));
            mEmailPreference.setSummary(newName);
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
        mEmailPreference.getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
        mSoundPreference.getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mNamePreference.getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
        mEmailPreference.getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
        mSoundPreference.getSharedPreferences()
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
        if (event.getState() == UpdateUserEvent.STATE.SUCCESS) {
            mIsProfileChanged = false;
        }
    }

    @Subscribe
    public void onVerifyEmailEvent(VerifyEmailEvent event) {
        if (event.getState() == VerifyEmailEvent.STATE.SUCCESS) {
            mQuickMessageView.show(getString(R.string.message_email_verification_sent));
        }
    }

    private void updateUserViews() {
        if (mUserManager.userCanChangeSettings()) {
            mChatBoxCategoryPreference.setEnabled(true);
        } else {
            mChatBoxCategoryPreference.setEnabled(false);
        }
    }

    private void updateVerifyButtonState() {
        if (mUserManager.getCurrentUser() == null) {
            mVerifyPreference.setEnabled(false);
            mVerifyPreference.setTitle(getString(R.string.email_not_found));
            return;
        }

        if (mUserManager.getCurrentUser().getProfile().isVerified()) {
            mVerifyPreference.setEnabled(false);
            mVerifyPreference.setTitle(getString(R.string.email_verified));
        } else {
            mVerifyPreference.setEnabled(true);
            mVerifyPreference.setTitle(getString(R.string.email_not_verified));
        }
    }
}
