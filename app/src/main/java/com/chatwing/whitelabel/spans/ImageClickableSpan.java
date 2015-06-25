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

import com.chatwing.whitelabel.events.ViewMessageContentImageEvent;
import com.squareup.otto.Bus;

import javax.inject.Inject;

/**
 * Created by cuongthai on 15/07/2014.
 */
public class ImageClickableSpan extends ClickableSpan {
    @Inject
    Bus mBus;
    private String imageUrl;

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public void onClick(View view) {
        mBus.post(new ViewMessageContentImageEvent(imageUrl));
    }
}
