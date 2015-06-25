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

package com.chatwing.whitelabel.parsers;

import android.text.TextUtils;

import com.chatwing.whitelabel.pojos.BBCodeTags;


/**
 * Created by nguyenthanhhuy on 12/17/13.
 */
public class BBCodePair implements Comparable<BBCodePair> {
    private BBCodeParser.BBCode code;
    private String value;

    public BBCodePair(BBCodeParser.BBCode code, String value) {
        this.code = code;
        this.value = value;
    }

    public BBCodePair(BBCodeParser.BBCode code) {
        this(code, null);
    }

    public BBCodePair(BBCodeParser.BBCode code, int value) {
        if (code == BBCodeParser.BBCode.COLOR || code == BBCodeParser.BBCode.BACKGROUND_COLOR) {
            // Alpha channel is not supported, so ignore the first 2 chars in the hex.
            this.code = code;
            this.value = "#" + Integer.toHexString(value).substring(2);
        } else {
            throw new IllegalArgumentException("Integer value is only " +
                    "applied for color BBCodes.");
        }
    }

    /**
     * Constructs a new instance from its string form, which was returned
     * from {@link #toString()}.
     */
    public BBCodePair(String stringForm) {
        if (!stringForm.contains("=")) {
            throw new IllegalArgumentException(
                    "String form (" + stringForm + ") is invalid.");
        }

        String[] trunks = stringForm.split("=");
        if (trunks.length == 0 || trunks.length > 2
                || TextUtils.isEmpty(trunks[0])) {
            throw new IllegalArgumentException(
                    "String form (" + stringForm + ") is invalid.");
        }

        // Find the BBCode
        boolean gotIt = false;
        for (BBCodeParser.BBCode code : BBCodeParser.BBCode.values()) {
            if (code.toString().equals(trunks[0])) {
                this.code = code;
                gotIt = true;
                break;
            }
        }
        if (!gotIt) {
            throw new IllegalArgumentException(
                    "String form (" + stringForm + ") is invalid.");
        }

        if (trunks.length == 2) {
            this.value = trunks[1];
        }
    }

    public BBCodeParser.BBCode getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return code + "=" + (TextUtils.isEmpty(value) ? "" : value);
    }

    @Override
    public int compareTo(BBCodePair another) {
        int result = code.compareTo(another.code);
        if (result != 0) {
            return result;
        }
        return value.compareTo(another.value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return (o instanceof BBCodePair) && compareTo((BBCodePair) o) == 0;
    }

    public BBCodeTags build() {
        StringBuilder builder = new StringBuilder();

        builder.append("[").append(code);
        if (!TextUtils.isEmpty(value)) {
            builder.append("=").append(value);
        }
        builder.append("]");
        String openTag = builder.toString();
        builder.delete(0, builder.length());

        String closeTag = builder.append("[/").append(code).append("]").toString();
        builder.delete(0, builder.length());

        return new BBCodeTags(openTag, closeTag);
    }
}
