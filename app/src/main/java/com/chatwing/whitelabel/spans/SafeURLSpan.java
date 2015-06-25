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

package com.chatwing.whitelabel.spans;

import android.content.ActivityNotFoundException;
import android.text.style.URLSpan;
import android.view.View;

import com.chatwing.whitelabel.events.UrlSpanErrorEvent;
import com.squareup.otto.Bus;

/**
 * Created by nguyenthanhhuy on 12/17/13.
 */
public class SafeURLSpan extends URLSpan {
    private Bus mBus;

    public SafeURLSpan(String url, Bus bus) {
        super(url);
        mBus = bus;
    }

    @Override
    public void onClick(View widget) {
        try {
            super.onClick(widget);
        } catch (ActivityNotFoundException exc) {
            // There is no app that can handle this link. Let's notify it
            // so other components can handle.
            if (mBus != null) {
                mBus.post(new UrlSpanErrorEvent(exc, this));
            }
        }
    }
}
