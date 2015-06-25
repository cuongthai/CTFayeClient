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

package com.chatwing.whitelabel.tasks;

import com.chatwing.whitelabel.managers.ApiManager;
import com.chatwing.whitelabel.managers.UserManager;
import com.chatwing.whitelabel.pojos.User;
import com.chatwing.whitelabel.pojos.params.oauth.AuthenticationParams;
import com.chatwing.whitelabel.pojos.responses.AuthenticationResponse;
import com.squareup.otto.Bus;

import javax.inject.Inject;

/**
 * Author: Huy Nguyen
 * Date: 4/21/13
 * Time: 11:12 AM
 */
public class StartSessionTask extends CallbackTask<AuthenticationParams, Void,
        AuthenticationResponse> {

    private ApiManager mApiManager;
    private UserManager mUserManager;
    private AuthenticationParams authenticationParams;

    @Inject
    StartSessionTask(Bus bus, ApiManager apiManager, UserManager userManager) {
        super(bus);
        mApiManager = apiManager;
        mUserManager = userManager;
    }

    public AuthenticationParams getParams() {
        return authenticationParams;
    }

    @Override
    protected AuthenticationResponse run(AuthenticationParams... params) throws Exception {
        authenticationParams = params[0];
        AuthenticationResponse response = mApiManager.authenticate(authenticationParams);
        User user = response.getUser();
        if (user != null) {
            mUserManager.addUser(user);
            mUserManager.activateUser(user.getId());
        }
        return response;
    }
}
