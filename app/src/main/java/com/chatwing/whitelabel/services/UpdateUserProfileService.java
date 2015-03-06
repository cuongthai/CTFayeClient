package com.chatwing.whitelabel.services;

import android.content.Intent;

import com.chatwing.whitelabel.managers.ApiManager;
import com.chatwing.whitelabel.managers.WLApiManagerImpl;
import com.chatwing.whitelabel.modules.ExtendChatWingModule;
import com.chatwing.whitelabel.pojos.responses.UpdateUserProfileResponse;
import com.chatwingsdk.ChatWing;
import com.chatwingsdk.events.internal.UpdateUserEvent;
import com.chatwingsdk.modules.ChatWingModule;
import com.chatwingsdk.pojos.User;
import com.chatwingsdk.pojos.UserProfile;
import com.chatwingsdk.services.BaseIntentService;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

/**
 * Author: Huy Nguyen
 * Date: 8/6/13
 * Time: 4:14 AM
 */
public class UpdateUserProfileService extends ExtendBaseIntentService {
    public static final String EXTRA_OLD_PROFILE = "old_profile";

    @Inject
    ApiManager mApiManager;
    @Override
    public void onCreate() {
        super.onCreate();
    }

    public UpdateUserProfileService() {
        super("UpdateUserProfileService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        User user = mUserManager.getCurrentUser();
        UserProfile profile = (UserProfile) intent.getSerializableExtra(EXTRA_OLD_PROFILE);
        try {
            post(UpdateUserEvent.started());
            UpdateUserProfileResponse response = mApiManager.updateUserProfile(user);
            profile = response.getUserProfile();
            post(UpdateUserEvent.success());
        } catch (Exception e) {
            post(new UpdateUserEvent(e));
        }

        user.setProfile(profile);
        mUserManager.addUser(user);
    }

    private void post(final UpdateUserEvent event) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mBus.post(event);
            }
        });
    }
}
