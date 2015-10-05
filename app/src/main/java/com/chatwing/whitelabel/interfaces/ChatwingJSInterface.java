package com.chatwing.whitelabel.interfaces;

import android.webkit.JavascriptInterface;

/**
 * Created by cuongthai on 10/5/15.
 */
public class ChatwingJSInterface {
    public static final String CHATWING_JS_NAME = "ChatwingMobile";
    private final ChatWingJavaDelegate mDelegate;

    public ChatwingJSInterface(ChatWingJavaDelegate delegate) {
        mDelegate = delegate;
    }

    @JavascriptInterface
    public void publish(String event,String data){
        mDelegate.publish(event, data);
    }
}
