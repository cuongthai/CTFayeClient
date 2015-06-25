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

import android.text.Html;
import android.text.Spanned;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

/**
 * Created by nguyenthanhhuy on 12/17/13.
 */
public interface BBCodeParser {
    public static final String RESOURCE_ID_PREFIX = "resId://";
    public static final String CONFIG_FILE_FULL = "bbcode_full.xsl";
    public static final String CONFIG_FILE_TEXT_FORMATTING = "bbcode_text_formatting.xsl";

    public enum BBCode implements Serializable {
        BOLD {
            @Override
            public String toString() {
                return "b";
            }
        },
        ITALIC {
            @Override
            public String toString() {
                return "i";
            }
        },
        UNDERLINE {
            @Override
            public String toString() {
                return "u";
            }
        },
        STRIKE_THROUGH {
            @Override
            public String toString() {
                return "s";
            }
        },
        COLOR {
            @Override
            public String toString() {
                return "color";
            }
        },
        BACKGROUND_COLOR {
            @Override
            public String toString() {
                return "bgcolor";
            }
        },
        IMAGE {
            @Override
            public String toString() {
                return "img";
            }
        },
        URL {
            @Override
            public String toString() {
                return "url";
            }
        },
        EMAIL {
            @Override
            public String toString() {
                return "email";
            }
        },
        VIDEO {
            @Override
            public String toString() {
                return "video";
            }
        }

    }

    /**
     * Parses a text (@param source) with a given config file (@param
     * configFile).
     * Note that for [img] BBCode, there are some cases that the id of
     * a resource bitmap is put inside an img tag, after the
     * {@link #RESOURCE_ID_PREFIX} (when paring bundled emoticons, for example).
     * Thus, the image getter will still be notified and it should decode a
     * resource bitmap instead of trying to load images over network.
     * File path of the resource bitmap can be returned, but id is more efficient
     * to look up and decode since it is cached by
     * {@link android.content.res.Resources} instances.
     */
    Spanned parseFull(String source,
                      Html.ImageGetter imageGetter,
                      Map<String, String> emoticons)
            throws IOException;

    Spanned parseTextFormatting(String source) throws IOException;

    Class[] getSpanClasses(BBCode code);
}
