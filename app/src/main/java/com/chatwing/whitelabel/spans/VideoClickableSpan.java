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

import android.text.style.ClickableSpan;
import android.view.View;

import com.chatwing.whitelabel.events.ViewVideoEvent;
import com.chatwing.whitelabel.utils.LogUtils;
import com.squareup.otto.Bus;

import javax.inject.Inject;

/**
 * Author: Huy Nguyen
 * Date: 7/10/13
 * Time: 11:57 AM
 */
public class VideoClickableSpan extends ClickableSpan {
    @Inject
    Bus mBus;
    private String videoUrl;

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    @Override
    public void onClick(View widget) {
        mBus.post(new ViewVideoEvent(videoUrl));
    }
}
