package asia.papaslove.ctfayeclient;

import android.os.Handler;
import android.os.Looper;
import android.util.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by cuongthai on 10/26/15.
 */
public class CTFayeClient implements CTWebSocketManager.CTWebSocketListener {

    private final String TAG = "CTFayeClient";

    private static final String FAYE_HANDSHAKE_CHANNEL = "/meta/handshake";
    private static final String FAYE_CONNECT_CHANNEL = "/meta/connect";
    private static final String FAYE_DISCONNECT_CHANNEL = "/meta/disconnect";
    private static final String FAYE_SUBSCRIBE_CHANNEL = "/meta/subscribe";
    private static final String FAYE_UNSUBSCRIBE_CHANNEL = "/meta/unsubscribe";


    private static final String FAYE_VALUE_VERSION = "1.0";
    private static final String FAYE_VALUE_MIN_VERSION = "1.0beta";
    private static final String FAYE_VALUE_CONNECTION_TYPE = "websocket";

    private static final int MAX_RETRY_ATTEMPT = 20;
    private static final int RETRY_INTERVAL = 2000;
    private String mUrl;

    public interface CTFayeClientInterface {

        void onFayeFailedWithError(Exception exception);

        void onFayeClosedWithError(Exception exception);

        void onConnectedToServer(String url);

        void onSubscribeToChannel(String subscriptionChannel);

        void onUnSubscribeFromChannel(String subscriptionChannel);

        void onMessageReceived(String channel, String data);
    }

    private final CTWebSocketManager mCTWebSocketManager;
    private CTFayeClientInterface listener;
    private int mSentMessageCount;
    private String mFayeClientId;
    private Map<String, JSONObject> channelExtensions;
    private Set<String> subscribedChannels;
    private Set<String> pendingChannelSubscriptions;
    private Set<String> openChannelSubscriptions;
    private boolean isConnected;
    private int retry;
    private boolean shouldRetryConnection;
    private int maxRetry;
    private int retryInterval;
    private Handler handler;
    private Runnable reconnectRunnale;

    public CTFayeClient() {
        this.mSentMessageCount = 0;
        shouldRetryConnection = true;
        maxRetry = MAX_RETRY_ATTEMPT;
        retryInterval = RETRY_INTERVAL;
        channelExtensions = new HashMap<>();
        pendingChannelSubscriptions = new HashSet<>();
        openChannelSubscriptions = new HashSet<>();
        subscribedChannels = new HashSet<>();
        mCTWebSocketManager = new CTWebSocketManager(this);
    }

    public void setFayeListener(CTFayeClientInterface listener) {
        this.listener = listener;
    }

    public synchronized boolean connect(String url) {
        if (isConnected || mCTWebSocketManager.isWebSocketOpenning()) {
            return false;
        }
        mUrl = url;
        mCTWebSocketManager.openWebSocketConnection(url);
        return true;
    }

    public void disconnect() {
        doFayeDisconnect();
    }

    /**
     * This helper method to help terminating the websocket. Force calling ClosedCallback
     * A dirty workaround for note 5 issue when client network drop but no ClosedCallback call
     */
    public void terminateWebSocket() {
        mCTWebSocketManager.close();
    }

    public void subscribeToChannel(String channel) {
        if (channel == null || subscribedChannels.contains(channel)) {
            return;
        }

        subscribedChannels.add(channel);
        if (isConnected) {
            doSendFayeSubscribeMessageWithChannel(channel);
        }
    }

    public void unsubscribeToChannel(String channel) {
        if (channel == null || !subscribedChannels.contains(channel)) {
            return;
        }

        subscribedChannels.remove(channel);
        pendingChannelSubscriptions.remove(channel);
        if (isConnected) {
            doSendFayeUnSubscribeMessageWithChannel(channel);
        }
    }

    public void sendMessage(JSONObject message, String channel) {
        doSendFayePublishMessage(message, channel, null);
    }

    public void sendMessage(JSONObject message, String channel, JSONObject extension) {
        doSendFayePublishMessage(message, channel, extension);
    }

    public void setExtension(JSONObject extension, String channel) {
        channelExtensions.put(channel, extension);
    }

    public void removeExtension(String channel) {
        channelExtensions.remove(channel);
    }

    public Map<String, JSONObject> getChannelExtensions() {
        return channelExtensions;
    }

    public Set<String> getPendingChannelSubscriptions() {
        return pendingChannelSubscriptions;
    }

    public Set<String> getOpenChannelSubscriptions() {
        return openChannelSubscriptions;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public boolean shouldRetryConnection() {
        return shouldRetryConnection;
    }

    public int getMaxRetry() {
        return maxRetry;
    }

    public int getRetry() {
        return retry;
    }

    ////////////////////////////////////////////////////////////////
    //              CTWebSocketListener
    ////////////////////////////////////////////////////////////////
    @Override
    public void onWebSocketReady() {
        doFayeHandShake();
    }

    @Override
    public void onWebSocketReceivedMessage(String messageStr) {
        try {
            JSONArray messages = new JSONArray(messageStr);
            for (int i = 0; i < messages.length(); i++) {
                JSONObject jsonMessage = messages.optJSONObject(i);
                if (jsonMessage == null) continue;

                CTFayeMessage message = new CTFayeMessage(jsonMessage);

                if (message.getChannel().equals(FAYE_HANDSHAKE_CHANNEL)) {
                    if (message.isSuccessful()) {
                        retry = 0;

                        mFayeClientId = message.getClientId();
                        isConnected = true;
                        shouldRetryConnection = true;
                        if (listener != null) listener.onConnectedToServer(mUrl);
                        doFayeConnect();
                        subscribePendingSubscriptions();
                    } else {
                        if (listener != null)
                            listener.onFayeFailedWithError(new Exception("Failed to handshake"));
                    }
                } else if (message.getChannel().equals(FAYE_CONNECT_CHANNEL)) {
                    if (message.isSuccessful()) {
                        isConnected = true;
                        doFayeConnect();
                    } else {
                        if (listener != null)
                            listener.onFayeFailedWithError(new Exception("Faye could not connect to server"));
                    }
                } else if (message.getChannel().equals(FAYE_DISCONNECT_CHANNEL)) {
                    if (message.isSuccessful()) {
                        mCTWebSocketManager.close();
                        isConnected = false;
                        clearSubscriptions();

                        if (listener != null) listener.onFayeClosedWithError(null);
                    } else {
                        if (listener != null)
                            listener.onFayeFailedWithError(new Exception("Faye could not disconnect from server"));
                    }
                } else if (message.getChannel().equals(FAYE_SUBSCRIBE_CHANNEL)) {
                    pendingChannelSubscriptions.remove(message.getSubscription());
                    if (message.isSuccessful()) {
                        openChannelSubscriptions.add(message.getSubscription());
                        if (listener != null)
                            listener.onSubscribeToChannel(message.getSubscription());
                    } else {
                        if (listener != null)
                            listener.onFayeFailedWithError(new Exception("Faye could not subscribe to channel " + message.getSubscription()));
                    }
                } else if (message.getChannel().equals(FAYE_UNSUBSCRIBE_CHANNEL)) {
                    if (message.isSuccessful()) {
                        subscribedChannels.remove(message.getSubscription());
                        pendingChannelSubscriptions.remove(message.getSubscription());
                        openChannelSubscriptions.remove(message.getSubscription());
                    } else {
                        if (listener != null)
                            listener.onUnSubscribeFromChannel(message.getSubscription());
                    }
                } else if (openChannelSubscriptions.contains(message.getChannel())) {
                    if (listener != null)
                        listener.onMessageReceived(message.getChannel(), message.getData());
                }
            }
        } catch (JSONException e) {
            if (listener != null) listener.onFayeFailedWithError(e);
        }
    }

    @Override
    public void onWebSocketClosedByError(Exception e) {
        isConnected = false;
        clearSubscriptions();
        if (listener != null) listener.onFayeClosedWithError(e);
        reconnect();
    }

    @Override
    public void onWebSocketFail(Exception e) {
        isConnected = false;
        clearSubscriptions();
        if (listener != null) listener.onFayeFailedWithError(e);
        reconnect();
    }

    ////////////////////////////////////////////////////////////////
    //              Bayeux Protocol
    ////////////////////////////////////////////////////////////////
    private void doFayeHandShake() {
        JSONArray supportedConnectionTypes = new JSONArray();
        supportedConnectionTypes.put("long-polling");
        supportedConnectionTypes.put("callback-polling");
        supportedConnectionTypes.put("iframe");
        supportedConnectionTypes.put("websocket");

        JSONObject json = new JSONObject();
        try {
            json.put(CTFayeMessage.FAYE_KEY_MESSAGE_CHANNEL, FAYE_HANDSHAKE_CHANNEL);
            json.put(CTFayeMessage.FAYE_KEY_MESSAGE_VERSION, FAYE_VALUE_VERSION);
            json.put(CTFayeMessage.FAYE_KEY_MESSAGE_MIN_VERSION, FAYE_VALUE_MIN_VERSION);
            json.put(CTFayeMessage.FAYE_KEY_MESSAGE_SUPPORTED_CONNECTION_TYPES, supportedConnectionTypes);

            mCTWebSocketManager.writeToWebSocket(json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void doFayeConnect() {
        JSONObject json = new JSONObject();
        try {
            json.put(CTFayeMessage.FAYE_KEY_MESSAGE_CHANNEL, FAYE_CONNECT_CHANNEL);
            json.put(CTFayeMessage.FAYE_KEY_MESSAGE_CLIENT_ID, mFayeClientId);
            json.put(CTFayeMessage.FAYE_KEY_MESSAGE_CONNECTION_TYPE, FAYE_VALUE_CONNECTION_TYPE);

            mCTWebSocketManager.writeToWebSocket(json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void doFayeDisconnect() {
        JSONObject json = new JSONObject();
        try {
            json.put(CTFayeMessage.FAYE_KEY_MESSAGE_CHANNEL, FAYE_DISCONNECT_CHANNEL);
            json.put(CTFayeMessage.FAYE_KEY_MESSAGE_CLIENT_ID, mFayeClientId);

            mCTWebSocketManager.writeToWebSocket(json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void doSendFayeSubscribeMessageWithChannel(String channel) {
        JSONObject json = new JSONObject();
        try {
            json.put(CTFayeMessage.FAYE_KEY_MESSAGE_CHANNEL, FAYE_SUBSCRIBE_CHANNEL);
            json.put(CTFayeMessage.FAYE_KEY_MESSAGE_CLIENT_ID, mFayeClientId);
            json.put(CTFayeMessage.FAYE_KEY_MESSAGE_SUBSCRIPTION, channel);

            JSONObject extension = channelExtensions.get(channel);
            if (extension != null) {
                json.put(CTFayeMessage.FAYE_KEY_MESSAGE_EXTENSION, extension);
            }

            mCTWebSocketManager.writeToWebSocket(json.toString());
            pendingChannelSubscriptions.add(channel);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void doSendFayeUnSubscribeMessageWithChannel(String channel) {
        JSONObject json = new JSONObject();
        try {
            json.put(CTFayeMessage.FAYE_KEY_MESSAGE_CHANNEL, FAYE_UNSUBSCRIBE_CHANNEL);
            json.put(CTFayeMessage.FAYE_KEY_MESSAGE_CLIENT_ID, mFayeClientId);
            json.put(CTFayeMessage.FAYE_KEY_MESSAGE_SUBSCRIPTION, channel);

            mCTWebSocketManager.writeToWebSocket(json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void doSendFayePublishMessage(JSONObject message, String channel, JSONObject extension) {
        if (!(isConnected && mCTWebSocketManager.isWebSocketConnectionReady())) {
            if (listener != null)
                listener.onFayeFailedWithError(new Exception("FayeClient not connected to server."));
            return;
        }

        String messageID = generateUniqueMessageId();

        JSONObject json = new JSONObject();
        try {
            json.put(CTFayeMessage.FAYE_KEY_MESSAGE_CHANNEL, channel);
            json.put(CTFayeMessage.FAYE_KEY_MESSAGE_CLIENT_ID, mFayeClientId);
            json.put(CTFayeMessage.FAYE_KEY_MESSAGE_DATA, message);
            json.put(CTFayeMessage.FAYE_KEY_MESSAGE_ID, messageID);

            if (extension != null) {
                json.put(CTFayeMessage.FAYE_KEY_MESSAGE_EXTENSION, extension);
            } else {
                JSONObject channelExtension = channelExtensions.get(channel);
                if (extension != null) {
                    json.put(CTFayeMessage.FAYE_KEY_MESSAGE_EXTENSION, channelExtension);
                }
            }

            mCTWebSocketManager.writeToWebSocket(json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    ////////////////////////////////////////////////////////////////
    //              Instance Methods
    ////////////////////////////////////////////////////////////////

    private void clearSubscriptions() {
        pendingChannelSubscriptions.clear();
        openChannelSubscriptions.clear();
    }

    private void subscribePendingSubscriptions() {
        for (String channel : subscribedChannels) {
            if ((!pendingChannelSubscriptions.contains(channel))
                    && (!openChannelSubscriptions.contains(channel))) {
                doSendFayeSubscribeMessageWithChannel(channel);
            }
        }
    }

    private String generateUniqueMessageId() {
        mSentMessageCount++;
        return Base64.encodeToString(String.valueOf(mSentMessageCount).getBytes(), Base64.DEFAULT);
    }

    private void invalidateReconnectTimer() {
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
            reconnectRunnale = null;
        }
    }


    private void reconnect() {
        if (shouldRetryConnection && retry < maxRetry) {
            invalidateReconnectTimer();
            handler = new Handler(Looper.getMainLooper());
            reconnectRunnale = new Runnable() {
                @Override
                public void run() {
                    if (isConnected) {
                        invalidateReconnectTimer();
                    } else {
                        if (shouldRetryConnection && retry < maxRetry) {
                            retry++;
                            connect(mUrl);
                        } else {
                            invalidateReconnectTimer();
                        }
                    }
                }
            };

            handler.postDelayed(reconnectRunnale, retryInterval);
        }
    }

}
