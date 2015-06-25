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

package com.chatwing.whitelabel.utils;

import android.graphics.Color;
import android.text.TextUtils;

import com.chatwing.whitelabel.Constants;


/**
 * Author: Huy Nguyen
 * Date: 5/12/13
 * Time: 10:03 AM
 */
public class ColorUtils {
    /**
     * Parse the color string, and return the corresponding color-int.
     * If the string cannot be parsed, throws an IllegalArgumentException
     * exception. Supported formats are:
     * #RRGGBB
     * #AARRGGBB
     * RRGGBB
     * AARRGGBB
     *
     * @return parsed value or default color if failed to parse.
     * Default color is parsed from {@link com.chatwing.Constants#MAIN_COLOR}
     */
    public static int parse(String color) {
        if (TextUtils.isEmpty(color)) {
            return Color.parseColor(Constants.MAIN_COLOR);
        }
        if (!color.startsWith("#")) {
            color = "#" + color;
        }
        try {
            return Color.parseColor(color);
        } catch (IllegalArgumentException exc) {
            LogUtils.e(exc);
            return Color.parseColor(Constants.MAIN_COLOR);
        }
    }
}
