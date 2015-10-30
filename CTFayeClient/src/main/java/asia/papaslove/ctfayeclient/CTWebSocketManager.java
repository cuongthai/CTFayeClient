package asia.papaslove.ctfayeclient;

import android.util.Log;

import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;

/**
 * Created by cuongthai on 10/23/15.
 */
class CTWebSocketManager {


    interface CTWebSocketListener {

        void onWebSocketReady();

        void onWebSocketReceivedMessage(String message);

        void onWebSocketClosedByError(Exception e);

        void onWebSocketFail(Exception e);
    }

    private static final String TAG = "CTWebSocketManager";
    private WebSocket mWebSocket;
    private CTWebSocketListener mListener;
    private boolean isOpenning = false;

    public CTWebSocketManager(CTWebSocketListener listener) {
        mListener = listener;
    }

    public void openWebSocketConnection(final String url) {
        if (isOpenning) return;
        isOpenning = true;
        //Close current WebSocket Connection
        close();
        AsyncHttpClient.getDefaultInstance().websocket(
                url,
                null,
                new AsyncHttpClient.WebSocketConnectCallback() {
                    @Override
                    public void onCompleted(Exception ex, final WebSocket webSocket) {
                        Log.d(TAG, "onCompleted " + ex + ":" + webSocket);
                        if (ex != null) {
                            isOpenning = false;
                            mListener.onWebSocketClosedByError(ex);
                            ex.printStackTrace();
                            return;
                        }

                        if (webSocket == null) {
                            isOpenning = false;
                            mListener.onWebSocketClosedByError(new Exception("Can't establish connection to server. Probably server closed connection"));
                            return;
                        }
                        mWebSocket = webSocket;
                        isOpenning = false;
                        mListener.onWebSocketReady();

                        mWebSocket.setStringCallback(new WebSocket.StringCallback() {
                            public void onStringAvailable(String s) {
                                mListener.onWebSocketReceivedMessage(s);
                            }
                        });

                        mWebSocket.setClosedCallback(new CompletedCallback() {
                            @Override
                            public void onCompleted(Exception e) {
                                Log.d(TAG, "Connection closed " + webSocket.isOpen() + ":" + webSocket.isPaused());
                                mListener.onWebSocketClosedByError(e);
                            }
                        });
                    }
                });
    }

    public void close() {
        if (mWebSocket != null) {
            mWebSocket.close();
            mWebSocket.setStringCallback(null);
            mWebSocket.setClosedCallback(null);
        }
        mWebSocket = null;
    }

    public void writeToWebSocket(String message) {
        if (!isWebSocketConnectionReady()) return;
        mWebSocket.send(message);
    }

    public boolean isWebSocketConnectionReady() {
        return mWebSocket != null && mWebSocket.isOpen();
    }

    public boolean isWebSocketOpenning() {
        return isOpenning;
    }
}
