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
import android.text.Html;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;

import com.chatwing.whitelabel.managers.ApiManager;
import com.chatwing.whitelabel.managers.EmoticonsManager;
import com.chatwing.whitelabel.modules.ForActivity;
import com.chatwing.whitelabel.pojos.BBCodeTags;
import com.chatwing.whitelabel.spans.ImageClickableSpan;
import com.chatwing.whitelabel.spans.SafeURLSpan;
import com.chatwing.whitelabel.spans.VideoClickableSpan;
import com.chatwing.whitelabel.spans.VideoSpan;
import com.chatwing.whitelabel.utils.LogUtils;
import com.chatwing.whitelabel.utils.RichTextUtils;
import com.squareup.otto.Bus;

import org.kefirsf.bb.BBProcessorFactory;
import org.kefirsf.bb.TextProcessor;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Author: Huy Nguyen
 * Date: 6/7/13
 * Time: 6:01 PM
 */
public class BBCodeParserImpl implements BBCodeParser {

    // This class can't be private since Dagger need to generate binding for it.
    static class URLSpanConverter implements RichTextUtils.SpanConverter<URLSpan, SafeURLSpan> {
        @Inject
        Bus mBus;

        @Override
        public SafeURLSpan convert(URLSpan span) {
            return new SafeURLSpan(span.getURL(), mBus);
        }
    }

    @Inject
    @ForActivity
    Context mContext;
    @Inject
    ApiManager mApiManager;
    @Inject
    EmoticonsManager mEmoticonsManager;
    @Inject
    Provider<BBCodeTagHandler> mTagHandlerProvider;
    @Inject
    Provider<URLSpanConverter> mURLSpanConverterProvider;
    private Map<String, TextProcessor> mProcessors;

    private TextProcessor getProcessor(String configFile) throws IOException {
        if (mProcessors == null) {
            mProcessors = new HashMap<String, TextProcessor>();
        }
        if (!mProcessors.containsKey(configFile)) {
            InputStream stream = mContext.getAssets().open(configFile);
            TextProcessor processor = BBProcessorFactory.getInstance().create(stream);
            mProcessors.put(configFile, processor);
        }
        return mProcessors.get(configFile);
    }

    /**
     * {@inheritDoc}
     */
    public Spanned parseFull(String source,
                             Html.ImageGetter imageGetter,
                             Map<String, String> emoticons)
            throws IOException {
        return parse(CONFIG_FILE_FULL, source, imageGetter, emoticons);
    }

    public Spanned parseTextFormatting(String source) throws IOException {
        return parse(CONFIG_FILE_TEXT_FORMATTING, source, null, null);
    }

    private Spanned parse(String configFile,
                          String source,
                          Html.ImageGetter imageGetter,
                          Map<String, String> emoticons)
            throws IOException {
        //We replace all video tag to img tag so that they can be load as preview photos
        if (source.contains(BBCode.VIDEO.toString())){
            source = source.replaceAll("\\[video\\](.+?)\\[/video\\]", "[img]"+VIDEO_URL_PREFIX+"$1[/img]");
        }

        // Replace emoticons in the text with [img] BBCode tags so that they
        // will be loaded as images later.
        if (emoticons != null) {
            BBCodeTags imgTags = new BBCodePair(BBCode.IMAGE).build();
            for (String emoticon : emoticons.keySet()) {
                if (source.contains(emoticon)) {
                    String emoticonPath;
                    EmoticonsManager.EmoticonInfo emoticonInfo
                            = mEmoticonsManager.getEmoticonInfo(emoticon);
                    if (emoticonInfo != null) {
                        // There is a bundled drawable for this emoticon. Use it.
                        emoticonPath = RESOURCE_ID_PREFIX + emoticonInfo.resId;
                    } else {
                        // The emoticon is missing from resources,
                        // let's fall back to server's version.
                        emoticonPath = mApiManager.getFullEmoticonUrl(emoticons.get(emoticon));
                    }

                    String bbcode = imgTags.getOpenTag() + emoticonPath + imgTags.getCloseTag();
                    source = source.replace(emoticon, bbcode);
                }
            }
        }

        TextProcessor processor = getProcessor(configFile);
        String htmlSource = processor.process(source);
        try {
            Spanned spanned = Html.fromHtml(
                    htmlSource,
                    imageGetter,
                    mTagHandlerProvider.get());
            // Replace all URLSpan with our SafeURLSpan to make sure there is
            // not crash when no Activity can handle an URL #56.
            return RichTextUtils.replaceAll(
                    spanned,
                    URLSpan.class,
                    mURLSpanConverterProvider.get());
        } catch (IllegalArgumentException exc) {
            LogUtils.e(exc);
            return Html.fromHtml(htmlSource);
        }
    }

    public Class[] getSpanClasses(BBCode code) {
        switch (code) {
            case BOLD:
            case ITALIC:
                return new Class[]{StyleSpan.class};
            case UNDERLINE:
                return new Class[]{UnderlineSpan.class};
            case STRIKE_THROUGH:
                return new Class[]{StrikethroughSpan.class};
            case COLOR:
                return new Class[]{ForegroundColorSpan.class};
            case BACKGROUND_COLOR:
                return new Class[]{BackgroundColorSpan.class};
            case IMAGE:
                return new Class[]{ImageSpan.class, ImageClickableSpan.class};
            case URL:
            case EMAIL:
                return new Class[]{URLSpan.class};
            case VIDEO:
                return new Class[]{VideoSpan.class, VideoClickableSpan.class};
            default:
                return null;
        }
    }
}
