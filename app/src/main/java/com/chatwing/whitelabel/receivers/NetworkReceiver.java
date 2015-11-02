package com.chatwing.whitelabel.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.chatwing.whitelabel.ChatWing;
import com.chatwing.whitelabel.events.NetworkStatusEvent;
import com.chatwing.whitelabel.modules.ChatWingModule;
import com.chatwing.whitelabel.utils.LogUtils;
import com.squareup.otto.Bus;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import dagger.ObjectGraph;

/**
 * Created by cuongthai on 10/27/15.
 */
public class NetworkReceiver extends BroadcastReceiver {
    @Inject
    protected Bus mBus;
    private ObjectGraph mObjectGraph;

    @Override
    public void onReceive(Context context, Intent intent) {
        ObjectGraph chatwingGraph = ChatWing.instance(context).getChatwingGraph();
        mObjectGraph = chatwingGraph.plus(getModules(context).toArray());
        // Inject ourselves so subclasses will have dependencies fulfilled when this method returns.
        mObjectGraph.inject(this);

        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobileActiveNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifiActiveNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        boolean isMobileConnected = mobileActiveNetInfo != null && mobileActiveNetInfo.isConnectedOrConnecting();
        boolean isWifiConnected = wifiActiveNetInfo != null && wifiActiveNetInfo.isConnectedOrConnecting();
        LogUtils.v("Network is available? " + isWifiConnected + ":" + isMobileConnected);
        if (isWifiConnected || isMobileConnected) {
            mBus.post(new NetworkStatusEvent(NetworkStatusEvent.Status.ON));
        } else {
            LogUtils.v("No Network");
            mBus.post(new NetworkStatusEvent(NetworkStatusEvent.Status.OFF));
        }
    }

    private List<Object> getModules(Context context) {
        return Collections.<Object>singletonList(new ChatWingModule(context));
    }
}
