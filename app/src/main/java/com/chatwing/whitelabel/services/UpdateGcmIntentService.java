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

package com.chatwing.whitelabel.services;

import android.content.Intent;
import android.text.TextUtils;

import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.managers.ApiManager;
import com.chatwing.whitelabel.managers.GcmManager;
import com.chatwing.whitelabel.pojos.User;
import com.chatwing.whitelabel.pojos.responses.UpdateGcmResponse;
import com.chatwing.whitelabel.utils.LogUtils;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import javax.inject.Inject;


/**
 * Author: Huy Nguyen
 * Date: 9/11/13
 * Time: 11:50 AM
 */
public class UpdateGcmIntentService extends BaseIntentService {

    /**
     * Key for a optional user extra. If the extra is not supplied,
     * it will be obtained via {@link com.chatwing.whitelabel.managers.UserManager#getCurrentUser()}.
     * <br/>
     * This extra is useful when {@link com.chatwing.whitelabel.managers.UserManager#mCurrentUser}
     * is invalidated before {@link #onHandleIntent(Intent)} is called.
     * <br/>
     * For example, when user logs out, it might be a good idea to update GCM
     * reggistration id with our server. To do so, the callee would start this
     * service and remove {@link com.chatwing.whitelabel.managers.UserManager#mCurrentUser}
     * right after that, on main thread. Thus, in {@link #onHandleIntent(Intent)},
     * {@link com.chatwing.whitelabel.managers.UserManager#mCurrentUser} is unavailable
     * and should be supplied manually.
     */
    public static final String EXTRA_USER = "user";

    @Inject
    GcmManager mGcmManager;

    public UpdateGcmIntentService() {
        super("UpdateGcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        if (action == null) return;
        User user = (User) intent.getSerializableExtra(EXTRA_USER);
        if (user == null) {
            user = mUserManager.getCurrentUser();
        }
        if (action.equals(ApiManager.GCM_ACTION_ADD)) {
            addGcmRegistrationIdToBackend(user);
        } else if (action.equals(ApiManager.GCM_ACTION_REMOVE)) {
            removeGcmRegistrationIdFromBackend(user);
        }
    }

    private void addGcmRegistrationIdToBackend(User user) {
        try {
            InstanceID instanceID = InstanceID.getInstance(this);
            String regId = instanceID.getToken(getString(R.string.gcm_sender_id),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            LogUtils.v("GCM registration id: " + regId);

            boolean sent = updateGcmRegistrationIdWithBackend(
                    user, regId, ApiManager.GCM_ACTION_ADD);
            if (sent) {
                mGcmManager.setRegistrationId(regId);
            }
        } catch (Exception exception) {
            // If there is an error, don't just keep trying to register.
            // Require the user to click a button again, or perform
            // exponential back-off.
        }
    }

    private void removeGcmRegistrationIdFromBackend(User user) {
        String regId = mGcmManager.getRegistrationId();
        if (TextUtils.isEmpty(regId)) {
            return;
        }
        try {
            updateGcmRegistrationIdWithBackend(user, regId, ApiManager.GCM_ACTION_REMOVE);
        } catch (Exception exception) {
            // Do nothing for now. Since we are going to clear the id in
            // GcmManager, it must be registered again.
            LogUtils.e(exception);
        }
        mGcmManager.clearRegistrationId();
    }

    /**
     * Sends GCM registration ID to our server
     * using {@link ApiManager#updateGcm(com.chatwing.whitelabel.pojos.User, String, String)}
     *
     * @return true if the ID was sent successfully. false if server returned
     * error message.
     * @throws org.json.JSONException
     * @throws ApiManager.ApiException
     * @throws ApiManager.UserUnauthenticatedException
     */
    private boolean updateGcmRegistrationIdWithBackend(User user, String regId,
                                                       String action)
            throws ApiManager.ApiException,
            ApiManager.UserUnauthenticatedException,
            ApiManager.InvalidAccessTokenException,
            ApiManager.NotVerifiedEmailException,
            ApiManager.OtherApplicationException {
        UpdateGcmResponse updateGcmResponse = mApiManager.updateGcm(user, regId, action);
        if (updateGcmResponse.getError() == null)
            return true;
        else
            return false;
    }
}
