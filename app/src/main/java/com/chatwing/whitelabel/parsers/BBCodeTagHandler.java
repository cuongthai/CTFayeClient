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

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.text.style.CharacterStyle;
import android.text.style.ImageSpan;
import android.text.style.StrikethroughSpan;

import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.modules.ForApplication;
import com.chatwing.whitelabel.spans.ImageClickableSpan;
import com.chatwing.whitelabel.spans.VideoClickableSpan;
import com.chatwing.whitelabel.spans.VideoSpan;
import com.chatwing.whitelabel.utils.RichTextUtils;

import org.xml.sax.XMLReader;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Created by nguyenthanhhuy on 12/17/13.
 * REFERENCE: http://stackoverflow.com/a/4062318/1136669.
 */
public class BBCodeTagHandler implements Html.TagHandler {
    private static final String HTML_TAG_STRIKE = "strike";
    private static final String HTML_TAG_BG_COLOR = "bgcolor";
    private static final String HTML_TAG_VIDEO = "video";
    private static final String HTML_TAG_IMAGE = "img";

    @Inject
    @ForApplication
    Context mContext;
    @Inject
    Provider<VideoClickableSpan> mVideoClickableSpanProvider;

    @Inject
    Provider<ImageClickableSpan> mImageClickableSpanProvider;

    @Override
    public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
        if (tag.equalsIgnoreCase(HTML_TAG_STRIKE)) {
            processStrike(opening, output);
        } else if (tag.startsWith(HTML_TAG_BG_COLOR)) {
            // Tag looks like this: "bgcolorf94302". Get the color string
            // by replacing "bgcolor" with "#", so the result looks like
            // this: "#f94302".
            String colorString = tag.replaceFirst(HTML_TAG_BG_COLOR, "#");
            int color = Color.parseColor(colorString);
            processBgColor(opening, output, color);
        } else if (tag.equalsIgnoreCase(HTML_TAG_VIDEO)) {
            processVideo(opening, output);
        } else if (tag.equalsIgnoreCase(HTML_TAG_IMAGE)) {
            processImage(opening, output);
        }
    }

    private void processStrike(boolean opening, Editable output) {
        process(opening, output, new StrikethroughSpan());
    }

    private void processBgColor(boolean opening, Editable output, int color) {
        process(opening, output, new BackgroundColorSpan(color));
    }

    private void processVideo(boolean opening, Editable output) {
        process(opening,
                output,
                new VideoSpan(mContext, R.drawable.ic_film),
                mVideoClickableSpanProvider.get());
    }

    private void processImage(boolean opening, Editable output) {
        Object[] spans = output.getSpans(0, output.length(), ImageSpan.class);
        if (spans.length == 0) {
            return;
        }

        ImageSpan imageSpan = (ImageSpan) spans[0];
        String imageUrl = imageSpan.getSource();
        if (TextUtils.isEmpty(imageUrl)
                || !RichTextUtils.isUrl(imageUrl)) {
            return;
        }

        ImageClickableSpan clickableSpan = mImageClickableSpanProvider.get();
        clickableSpan.setImageUrl(imageUrl);
        output.setSpan(clickableSpan, output.getSpanStart(imageSpan), output.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private void process(boolean opening,
                         Editable output,
                         CharacterStyle... spanStyles) {
        int len = output.length();
        if (opening) {
            for (CharacterStyle style : spanStyles) {
                output.setSpan(style, len, len, Spannable.SPAN_MARK_MARK);
            }
        } else {
            for (CharacterStyle style : spanStyles) {
                Object obj = getLast(output, style.getClass());
                int where = output.getSpanStart(obj);

                output.removeSpan(obj);

                if (where != len) {
                    if (style instanceof VideoClickableSpan) {
                        // Need to set the video url to this span.
                        String url = output.subSequence(where, len).toString();
                        ((VideoClickableSpan) style).setVideoUrl(url);
                    }
                    output.setSpan(style, where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }
    }

    private Object getLast(Editable text, Class kind) {
        Object[] objs = text.getSpans(0, text.length(), kind);
        if (objs.length == 0) {
            return null;
        } else {
            for (int i = objs.length; i > 0; i--) {
                if (text.getSpanFlags(objs[i - 1]) == Spannable.SPAN_MARK_MARK) {
                    return objs[i - 1];
                }
            }
            return null;
        }
    }
}
