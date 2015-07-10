package com.chatwing.whitelabel.interfaces;

import android.os.Handler;

import com.chatwing.whitelabel.modules.ForMainThread;
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
    }
}
