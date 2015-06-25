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

import android.webkit.JavascriptInterface;

/**
 * Created by cuongthai on 26/08/2014.
 */
public class ChatWingJSInterface {
    public static final String CHATWING_JS_NAME = "ChatwingMobile";
    private final ChatWingJavaDelegate mDelegate;

    public ChatWingJSInterface(ChatWingJavaDelegate delegate) {
        mDelegate = delegate;
    }

    @JavascriptInterface
    public void publish(String event,String data){
//        LogUtils.v("From js "+event+":"+data);
        mDelegate.publish(event, data);
    }

}
