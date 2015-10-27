package asia.papaslove.ctfayeclient;

import org.json.JSONObject;

/**
 * Created by cuongthai on 10/26/15.
 */
public class CTFayeMessage {
    static final String FAYE_KEY_MESSAGE_CHANNEL = "channel";
    static final String FAYE_KEY_MESSAGE_ID = "id";
    static final String FAYE_KEY_MESSAGE_CLIENT_ID = "clientId";
    static final String FAYE_KEY_MESSAGE_VERSION = "version";
    static final String FAYE_KEY_MESSAGE_MIN_VERSION = "minimumVersion";
    static final String FAYE_KEY_MESSAGE_SUPPORTED_CONNECTION_TYPES = "supportedConnectionTypes";
    static final String FAYE_KEY_MESSAGE_CONNECTION_TYPE = "connectionType";
    static final String FAYE_KEY_MESSAGE_SUBSCRIPTION = "subscription";
    static final String FAYE_KEY_MESSAGE_EXTENSION = "ext";
    static final String FAYE_KEY_MESSAGE_DATA = "data";

    private final String channel;
    private final String id;
    private final String clientId;
    private final boolean successful;
    private final boolean authSuccessful;
    private final String version;
    private final String minimumVersion;
    private final String supportedConnectionTypes;
    private final String advice;
    private final String error;
    private final String subscription;
    private final String data;
    private final String ext;

    public CTFayeMessage(JSONObject jsonMessage) {
        id = jsonMessage.optString(FAYE_KEY_MESSAGE_ID);
        channel = jsonMessage.optString(FAYE_KEY_MESSAGE_CHANNEL);
        clientId = jsonMessage.optString(FAYE_KEY_MESSAGE_CLIENT_ID);
        successful = jsonMessage.optBoolean("successful");
        authSuccessful = jsonMessage.optBoolean("authSuccessful");
        version = jsonMessage.optString(FAYE_KEY_MESSAGE_VERSION);
        minimumVersion = jsonMessage.optString(FAYE_KEY_MESSAGE_MIN_VERSION);
        supportedConnectionTypes = jsonMessage.optString(FAYE_KEY_MESSAGE_SUPPORTED_CONNECTION_TYPES);
        advice = jsonMessage.optString("advice");
        error = jsonMessage.optString("error");
        subscription = jsonMessage.optString(FAYE_KEY_MESSAGE_SUBSCRIPTION);
        data = jsonMessage.optString(FAYE_KEY_MESSAGE_DATA);
        ext = jsonMessage.optString(FAYE_KEY_MESSAGE_EXTENSION);
    }

    public String getChannel() {
        return channel;
    }

    public String getClientId() {
        return clientId;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public String getSubscription() {
        return subscription;
    }

    public String getData() {
        return data;
    }
}
