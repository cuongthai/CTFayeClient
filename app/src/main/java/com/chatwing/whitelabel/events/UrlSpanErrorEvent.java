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

package com.chatwing.whitelabel.events;

import android.text.style.URLSpan;

/**
 * Author: Huy Nguyen
 * Date: 7/16/13
 * Time: 11:49 AM
 */
public class UrlSpanErrorEvent {
    private Exception exception;
    private URLSpan span;

    public UrlSpanErrorEvent(Exception exception, URLSpan span) {
        this.exception = exception;
        this.span = span;
    }

    public Exception getException() {
        return exception;
    }

    public URLSpan getSpan() {
        return span;
    }
}
