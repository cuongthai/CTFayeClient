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

import com.chatwing.whitelabel.events.LoadChatBoxDetailsEvent;
import com.chatwing.whitelabel.pojos.User;
import com.chatwing.whitelabel.pojos.responses.ChatBoxDetailsResponse;


/**
 * Author: Huy Nguyen
 * Date: 5/31/13
 * Time: 2:24 PM
 */
public class LoadChatBoxDetailsService extends BaseIntentService {
    public static final String EXTRA_CHAT_BOX_ID = "chat_box_id";

    public LoadChatBoxDetailsService() {
        super("LoadChatBoxDetailsService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        User user = mUserManager.getCurrentUser();

        try {
            ChatBoxDetailsResponse response = null;
            if (intent.hasExtra(EXTRA_CHAT_BOX_ID)) {
                response = mApiManager.loadChatBoxDetails(
                        user,
                        intent.getIntExtra(EXTRA_CHAT_BOX_ID, 0));
            }

            post(new LoadChatBoxDetailsEvent(response));
        } catch (final Exception exc) {
            post(new LoadChatBoxDetailsEvent(exc));
        }
    }

    private void post(final LoadChatBoxDetailsEvent event) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mBus.post(event);
            }
        });
    }
}
