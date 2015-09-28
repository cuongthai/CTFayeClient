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

package com.chatwing.whitelabel.interfaces;

import android.content.Context;
import android.os.Handler;

import com.chatwing.whitelabel.events.faye.ChannelSubscriptionChangedEvent;
import com.chatwing.whitelabel.events.faye.MessageReceivedEvent;
import com.chatwing.whitelabel.events.faye.ServerConnectionChangedEvent;
import com.chatwing.whitelabel.modules.ForApplication;
import com.chatwing.whitelabel.modules.ForMainThread;
import com.chatwing.whitelabel.utils.LogUtils;
import com.google.gson.Gson;
import com.squareup.otto.Bus;

import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;

/**
 * Created by steve on 06/12/2014.
 */
public class JSInterfaceImpl implements ChatWingJavaDelegate {
    protected static final String FAYE_CONNECTED_TO_SERVER = "faye:up";
    protected static final String FAYE_DISCONNECTED_TO_SERVER = "faye:down";
    protected static final String FAYE_SUBSCRIBE_TO_CHANNEL = "channel:subscribed";
    protected static final String FAYE_FAIL_SUBSCRIBE_TO_CHANNEL = "channel:error";
    protected static final String FAYE_MESSAGE_RECEIVE = "faye:message";
    private final Gson mGson;
    @Inject
    Bus mBus;
    @Inject
    @ForMainThread
    Handler mHandler;
    @Inject
    @ForApplication
    Context context;

    public JSInterfaceImpl() {
        mGson = new Gson();
    }

    @Override
    public void publish(String event, final String data) {
        if (data == null || event == null) return;
//        LogUtils.v("event " + event + " data " + data);
        if (FAYE_CONNECTED_TO_SERVER.equals(event)) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mBus.post(ServerConnectionChangedEvent.connectedEvent());
                }
            });
        } else if (FAYE_DISCONNECTED_TO_SERVER.equals(event)) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mBus.post(ServerConnectionChangedEvent.disconnectedEvent());
                }
            });
        } else if (FAYE_SUBSCRIBE_TO_CHANNEL.equals(event)) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        mBus.post(ChannelSubscriptionChangedEvent
                                .succeedEvent(new JSONObject(data).getString("channel")));
                    } catch (JSONException e) {
                        LogUtils.e(e);
                    }
                }
            });
        } else if (FAYE_FAIL_SUBSCRIBE_TO_CHANNEL.equals(event)) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject jsonObject = new JSONObject(data);
                        mBus.post(ChannelSubscriptionChangedEvent.failedEvent(
                                jsonObject.getString("channel"),
                                jsonObject.getString("error")));
                    } catch (JSONException e) {
                        LogUtils.e(e);
                    }
                }
            });
        } else if (FAYE_MESSAGE_RECEIVE.equals(event)) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject jsonObject = new JSONObject(data);
                        mBus.post(new MessageReceivedEvent(jsonObject.getString("channel"),
                                data));
                    } catch (JSONException e) {
                        LogUtils.e(e);
                    }
                }
            });
        }
    }

}
