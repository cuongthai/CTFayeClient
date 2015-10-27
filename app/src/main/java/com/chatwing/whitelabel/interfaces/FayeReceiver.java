package com.chatwing.whitelabel.interfaces;

import android.os.Handler;

import com.chatwing.whitelabel.events.faye.ChannelSubscriptionChangedEvent;
import com.chatwing.whitelabel.events.faye.FayeFailedEvent;
import com.chatwing.whitelabel.events.faye.MessageReceivedEvent;
import com.chatwing.whitelabel.events.faye.ServerConnectionChangedEvent;
import com.chatwing.whitelabel.utils.LogUtils;
import com.squareup.otto.Bus;

import asia.papaslove.ctfayeclient.CTFayeClient;

/**
 * Created by cuongthai on 10/27/15.
 */
public class FayeReceiver implements CTFayeClient.CTFayeClientInterface {
    private Bus mBus;
    private Handler mHandler;
    private final CTFayeClient mFayeClient;

    public FayeReceiver(CTFayeClient fayeClient, Handler handler, Bus bus) {
        mBus = bus;
        mHandler = handler;
        mFayeClient = fayeClient;
        mFayeClient.setFayeListener(this);
    }

    @Override
    public void onFayeFailedWithError(Exception exception) {
        if (!mFayeClient.isConnected() && clientWillRetry()) {
            LogUtils.v("Failed when transferring with faye but WILL RETRY");
        } else {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mBus.post(new FayeFailedEvent());
                }
            });
        }
    }

    private boolean clientWillRetry() {
        boolean willRetry = mFayeClient.shouldRetryConnection() && mFayeClient.getRetry() < mFayeClient.getMaxRetry();
        LogUtils.v("Client will retry =" + willRetry + ": Count = " + mFayeClient.getRetry() + " MAX = " + mFayeClient.getMaxRetry());
        return willRetry;
    }

    @Override
    public void onFayeClosedWithError(Exception exception) {
        if (clientWillRetry()) {
            LogUtils.v("Disconnected with faye but WILL RETRY");
        } else {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mBus.post(ServerConnectionChangedEvent.disconnectedEvent());
                }
            });
        }
    }

    @Override
    public void onConnectedToServer(String url) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mBus.post(ServerConnectionChangedEvent.connectedEvent());
            }
        });
    }

    @Override
    public void onSubscribeToChannel(final String subscriptionChannel) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mBus.post(ChannelSubscriptionChangedEvent
                        .succeedEvent(subscriptionChannel));
            }
        });
    }

    @Override
    public void onUnSubscribeFromChannel(String subscriptionChannel) {
    }

    @Override
    public void onMessageReceived(final String channel, final String data) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mBus.post(new MessageReceivedEvent(channel, data));
            }
        });
    }

    public void disconnect() {
        mFayeClient.disconnect();
    }

    public void connect(String url) {
        mFayeClient.connect(url);
    }

    public void subscribeToChannel(String channel) {
        mFayeClient.subscribeToChannel(channel);
    }

    public void unsubscribeToChannel(String channel) {
        mFayeClient.unsubscribeToChannel(channel);
    }
}
