package com.chatwing.whitelabel.interfaces;

import android.os.Handler;

import com.chatwing.whitelabel.events.MessageEditEvent;
import com.chatwing.whitelabel.pojos.jspojos.MessageResponse;
import com.chatwingsdk.interfaces.JSInterfaceImpl;
import com.chatwingsdk.modules.ForMainThread;
import com.chatwingsdk.utils.LogUtils;
import com.google.gson.Gson;
import com.squareup.otto.Bus;

import javax.inject.Inject;

/**
 * Created by steve on 30/12/2014.
 */
public class WLJSInterfaceImpl extends JSInterfaceImpl {
    @Inject
    Bus mBus;
    @Inject
    @ForMainThread
    Handler mHandler;

    @Inject
    public WLJSInterfaceImpl() {
        super();
    }

    @Override
    public void publish(String event, final String data) {
        super.publish(event, data);
        if (LONG_CLICK_MESSAGE_EVENT.equals(event)) {
            LogUtils.v("Publish " + event);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    MessageResponse messageResponse = new Gson().fromJson(data, MessageResponse.class);
                    if (messageResponse != null && messageResponse.getMessages().length != 0) {
                        mBus.post(new MessageEditEvent(messageResponse.getMessages()));
                    }
                }
            });
        }
    }
}
