package com.chatwing.whitelabel.services;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.media.SoundPool;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.chatwing.whitelabel.ChatWing;
import com.chatwing.whitelabel.Constants;
import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.contentproviders.ChatWingContentProvider;
import com.chatwing.whitelabel.events.ChatServiceEvent;
import com.chatwing.whitelabel.events.NetworkAvaialbleEvent;
import com.chatwing.whitelabel.events.faye.MessageReceivedEvent;
import com.chatwing.whitelabel.events.faye.ServerConnectionChangedEvent;
import com.chatwing.whitelabel.interfaces.FayeReceiver;
import com.chatwing.whitelabel.managers.UserManager;
import com.chatwing.whitelabel.modules.ChatWingModule;
import com.chatwing.whitelabel.parsers.EventParser;
import com.chatwing.whitelabel.pojos.Event;
import com.chatwing.whitelabel.tables.ChatBoxTable;
import com.chatwing.whitelabel.utils.LogUtils;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import dagger.ObjectGraph;

/**
 * Created by cuongthai on 10/29/15.
 */
public class ChatWingChatService extends Service {
    @Inject
    protected FayeReceiver mFayeReceiver;
    @Inject
    protected Bus mBus;
    @Inject
    protected EventParser mEventParser;
    @Inject
    protected UserManager mUserManager;
    @Inject
    protected SoundPool mSoundEffectsPool;

    private ChatWing chatWing;
    private int newMessageSoundId;

    /**
     * This is called when system hate us or user hate our app and kill it. This service is killed too
     * When it happen, the whole ObjectGraph + Chatwing is recreated.
     * We will reconnect and subscribe to all known channels
     */
    @Override
    public void onCreate() {
        super.onCreate();

        chatWing = ChatWing.instance(getApplicationContext());
        ObjectGraph mObjectGraph = chatWing.getChatwingGraph().plus(getModules().toArray());
        // Inject ourselves so subclasses will have dependencies fulfilled when this method returns.
        mObjectGraph.inject(this);

        /**
         * We re-inject here because the whole objectgraph can be desployed by desploying the activity
         *
         */
        mBus.register(this);
        LogUtils.v("Create service +" + chatWing);

        newMessageSoundId = mSoundEffectsPool.load(this, R.raw.new_message, 1);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mFayeReceiver.connect(Constants.FAYE_URL);
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mFayeReceiver.disconnect();
    }

    /**
     * Everytime the service is connected to faye. We subscribe all known channels
     * @param event
     */
    @Subscribe
    public void onServerConnectionChangedEvent(ServerConnectionChangedEvent event) {
        if (event.getStatus() == ServerConnectionChangedEvent.Status.CONNECTED) {
            //After we connected, make sure all known channels are subscribed
            ensureChannelsAreSubscribed();
        }
    }

    @Subscribe
    public void onMessageReceived(MessageReceivedEvent event) throws IOException {
        //Faye
        try {
            Event message = mEventParser.parse(event.getMessage());
            String name = message.getName();
            if (name.equals(EventParser.EVENT_NEW_MESSAGE)
                    || name.equals(EventParser.EVENT_NETWORK_NEW_MESSAGE)) {

                LogUtils.v("Message here = " + event.getMessage());
                if (mUserManager.isSoundEnabled() && !chatWing.isAppVisible()) {
                    mSoundEffectsPool.play(newMessageSoundId, 1.0f, 1.0f, 0, 0, 1);
                }
            }
        } catch (JSONException ex) {
            LogUtils.e(ex);
        }
    }

    /**
     * Event from activity
     *
     * @param event
     */
    @Subscribe
    public void onChatServiceEvent(ChatServiceEvent event) {
        if (event.getStatus().equals(ChatServiceEvent.Status.CONNECT)) {
            //Request connect to faye. If it's is connected, does nothing
            mFayeReceiver.connect(Constants.FAYE_URL);
            //So ... This will ensure latest channels are subscribed
            ensureChannelsAreSubscribed();
        } else if (event.getStatus().equals(ChatServiceEvent.Status.DISCONNECT)) {
            mFayeReceiver.disconnect();
        } else if (event.getStatus().equals(ChatServiceEvent.Status.SUBSCRIBE_CHANNEL)) {
            mFayeReceiver.subscribeToChannel(event.getFayeChannel());
        } else if (event.getStatus().equals(ChatServiceEvent.Status.UNSUBSCRIBE_ALL_CHANNELS)) {
            unsubscribeToChatBoxChannels();
            unsubscribeToConversationChannels();
        }
    }

    @Subscribe
    public void onNetworkAvaialbleEvent(NetworkAvaialbleEvent event) {
        mFayeReceiver.connect(Constants.FAYE_URL);
    }

    private List<Object> getModules() {
        return Collections.<Object>singletonList(new ChatWingModule(this));
    }

    private void ensureChannelsAreSubscribed() {
        subscribeToChatBoxChannels();
        subscribeToConversationChannels();
    }

    private void subscribeToChatBoxChannels() {
        ArrayList<String> fayeChannels = getChatboxFayeChannels();

        for (String channel : fayeChannels) {
            mFayeReceiver.subscribeToChannel(channel);
        }
    }

    private void subscribeToConversationChannels() {
        if (mUserManager.getCurrentUser() == null) {
            return;
        }
        mFayeReceiver.subscribeToChannel(String.format("/user/%s", mUserManager.getCurrentUser().getId()));
    }

    private void unsubscribeToChatBoxChannels() {
        // Subscribe to all of them
        ArrayList<String> fayeChannels = getChatboxFayeChannels();

        for (String channel : fayeChannels) {
            mFayeReceiver.unsubscribeToChannel(channel);
        }
    }

    private void unsubscribeToConversationChannels() {
        if (mUserManager.getCurrentUser() == null) {
            return;
        }
        mFayeReceiver.unsubscribeToChannel(String.format("/user/%s", mUserManager.getCurrentUser().getId()));
    }

    private ArrayList<String> getChatboxFayeChannels() {
        // Query for chat box keys
        Uri chatBoxesUri = ChatWingContentProvider.getChatBoxesUri();
        ArrayList<String> fayeChannels = new ArrayList<String>();
        Cursor c = null;
        try {
            c = getContentResolver().query(
                    chatBoxesUri,
                    new String[]{ChatBoxTable.FAYE_CHANNEL},
                    null, null, null);
            if (c.getCount() > 0 && c.moveToFirst()) {
                int fayeChannelIndex = c.getColumnIndex(ChatBoxTable.FAYE_CHANNEL);
                do {
                    // We can subscribe to each chat box here,
                    // but it can take a lot of time and holding the Cursor for
                    // that long is not a good idea. So, to be safe,
                    // let's get all chat box keys first and subscribe later.
                    String fayeChannel = c.getString(fayeChannelIndex);
                    fayeChannels.add(String.format("/%s", fayeChannel));
                } while (c.moveToNext());
            }
        } finally {
            if (c != null && !c.isClosed()) {
                c.close();
            }
        }
        return fayeChannels;
    }
}
