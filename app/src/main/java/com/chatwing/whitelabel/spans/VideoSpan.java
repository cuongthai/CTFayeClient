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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.style.ImageSpan;

/**
 * Author: Huy Nguyen
 * Date: 7/10/13
 * Time: 11:54 AM
 */
public class VideoSpan extends ImageSpan {
    @Deprecated
    public VideoSpan(Bitmap b) {
        super(b);
    }

    @Deprecated
    public VideoSpan(Bitmap b, int verticalAlignment) {
        super(b, verticalAlignment);
    }

    public VideoSpan(Context context, Bitmap b) {
        super(context, b);
    }

    public VideoSpan(Context context, Bitmap b, int verticalAlignment) {
        super(context, b, verticalAlignment);
    }

    public VideoSpan(Drawable d) {
        super(d);
    }

    public VideoSpan(Drawable d, int verticalAlignment) {
        super(d, verticalAlignment);
    }

    public VideoSpan(Drawable d, String source) {
        super(d, source);
    }

    public VideoSpan(Drawable d, String source, int verticalAlignment) {
        super(d, source, verticalAlignment);
    }

    public VideoSpan(Context context, Uri uri) {
        super(context, uri);
    }

    public VideoSpan(Context context, Uri uri, int verticalAlignment) {
        super(context, uri, verticalAlignment);
    }

    public VideoSpan(Context context, int resourceId) {
        super(context, resourceId);
    }

    public VideoSpan(Context context, int resourceId, int verticalAlignment) {
        super(context, resourceId, verticalAlignment);
    }
}