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

import com.chatwing.whitelabel.events.CreateConversationEvent;
import com.chatwing.whitelabel.pojos.params.CreateConversationParams;
import com.chatwing.whitelabel.pojos.responses.CreateConversationResponse;
import com.chatwing.whitelabel.utils.LogUtils;


/**
 * Created by cuongthai on 4/1/14.
 */
public class CreateConversationIntentService extends BaseIntentService {
    public static final String EXTRA_USER = "user";
    public static final String SILENT = "silent";

    public CreateConversationIntentService() {
        super("CreateConversationIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        LogUtils.v("Populate user debug: Creating");
        if (intent == null) return;
        boolean silent = intent.getBooleanExtra(SILENT, false);
        CreateConversationParams.SimpleUser targetUser =
                (CreateConversationParams.SimpleUser) intent.getSerializableExtra(EXTRA_USER);
        CreateConversationEvent event;
        try {
            CreateConversationResponse conversation = mApiManager.createConversation(
                    mUserManager.getCurrentUser(),
                    targetUser.getLoginId(),
                    targetUser.getLoginType());
            event = new CreateConversationEvent(conversation);
            if (!silent) post(event);
            LogUtils.v("Populate user debug: Done " + conversation);
        } catch (Exception e) {
            event = new CreateConversationEvent(e);
            if (!silent) post(event);
        }
    }

    private void post(final CreateConversationEvent event) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mBus.post(event);
            }
        });
    }
}
