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

package com.chatwing.whitelabel.views;

import android.content.Context;
import android.text.Editable;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.EditText;

import com.chatwing.whitelabel.parsers.BBCodePair;
import com.chatwing.whitelabel.parsers.BBCodeParser;
import com.chatwing.whitelabel.pojos.BBCodeTags;
import com.chatwing.whitelabel.utils.LogUtils;
import com.chatwing.whitelabel.utils.RichTextUtils;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

import javax.inject.Inject;

/**
 * Author: Huy Nguyen
 * Date: 6/19/13
 * Time: 6:17 PM
 * <p/>
 * A subclass of EditText that parses and applies given BBCodes to its content.
 * BBCodes are applied to the whole text. Thus, they can't be duplicated.
 * If a color BBCode is added, the existing one is removed regardless of actual
 * values.
 * Note: dependencies must be injected before any text processing.
 * Thus, the ideal place to do it is right after the view being creating or inflating.
 */
public class BBCodeEditText extends EditText {

    private static final Set<BBCodeParser.BBCode> SUPPORTED_CODES;

    static {
        SUPPORTED_CODES = new HashSet<BBCodeParser.BBCode>();
        SUPPORTED_CODES.add(BBCodeParser.BBCode.BOLD);
        SUPPORTED_CODES.add(BBCodeParser.BBCode.ITALIC);
        SUPPORTED_CODES.add(BBCodeParser.BBCode.UNDERLINE);
        SUPPORTED_CODES.add(BBCodeParser.BBCode.STRIKE_THROUGH);
        SUPPORTED_CODES.add(BBCodeParser.BBCode.COLOR);
        SUPPORTED_CODES.add(BBCodeParser.BBCode.BACKGROUND_COLOR);
    }

    @Inject
    BBCodeParser mBBCodeParser;
    /**
     * BBCodes are applied to the whole text. Thus, they can't be duplicated.
     * If a color BBCode is added, the existing one is removed regardless of actual
     * values.
     */
    private TreeMap<BBCodeParser.BBCode, BBCodePair> mBBCodePairs;

    public BBCodeEditText(Context context) {
        super(context);
        init();
    }

    public BBCodeEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BBCodeEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mBBCodePairs = new TreeMap<BBCodeParser.BBCode, BBCodePair>();
    }

    public void setBBCodeParser(BBCodeParser bbCodeParser) {
        this.mBBCodeParser = bbCodeParser;
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        buildBBCodeSpans(false);
    }

    /**
     * Appends the given {@link BBCodePair} to the list of BBCodes.
     *
     * @return true if appended. false otherwise.
     */
    public boolean append(BBCodePair pair) {
        if (pair == null) {
            return false;
        }
        if (SUPPORTED_CODES.contains(pair.getCode())) {
            mBBCodePairs.put(pair.getCode(), pair);
            buildBBCodeSpans(true);
        } else {
            int selectionStart = getSelectionStart();
            int selectionEnd = getSelectionEnd();
            BBCodeTags tags = pair.build();
            String openTag = tags.getOpenTag();

            StringBuilder builder = new StringBuilder(getText())
                    .insert(selectionEnd, tags.getCloseTag())
                    .insert(selectionStart, openTag);
            // setText will trigger onTextChanged and then buildBBCodeTags.
            // So there is no need to call buildBBCodeTags again in this case.
            setText(builder.toString());

            setSelection(
                    selectionStart + openTag.length(),
                    selectionEnd + openTag.length());
        }
        requestFocus();
        return true;
    }

    /**
     * Removes the given {@link BBCodeParser.BBCode} from the list of BBCodes.
     * Since BBCodes can't be duplicated, an instance of {@link BBCodeParser.BBCode}
     * is sufficient, instead of {@link BBCodePair}.
     *
     * @return true if the code existed in the list and has been removed.
     * false otherwise.
     */
    public boolean remove(BBCodeParser.BBCode code) {
        if (!mBBCodePairs.isEmpty() && mBBCodePairs.remove(code) != null) {
            buildBBCodeSpans(true);
            return true;
        }
        return false;
    }

    public void clearAllBBCodes() {
        mBBCodePairs.clear();
    }

    public boolean contains(BBCodeParser.BBCode code) {
        return mBBCodePairs.containsKey(code);
    }

    public Collection<BBCodePair> getAllBBCodes() {
        return mBBCodePairs.values();
    }

    /**
     * Builds BBCode Spans and apply them to the text,
     * so it is drawn correctly next time.
     *
     * @param isBBCodeListChanged
     *         If true, BBCode list has been changed and this method will
     *         continue building even when the list list is empty. It is useful
     *         after removing the last BBCode out of the list.
     *         If false, BBCode list is not changed and this method will stop
     *         building if BBCode list is empty.
     */
    private void buildBBCodeSpans(boolean isBBCodeListChanged) {
        Editable text = super.getText();
        if (TextUtils.isEmpty(text)) {
            return;
        }
        if (!isBBCodeListChanged && mBBCodePairs.isEmpty()) {
            return;
        }

        try {
            Spanned spanned = mBBCodeParser.parseTextFormatting(getFullText());

            // Apply spanned to the text
            for (BBCodeParser.BBCode code : SUPPORTED_CODES) {
                Class[] spanClasses = mBBCodeParser.getSpanClasses(code);
                if (spanClasses == null || spanClasses.length == 0) {
                    continue;
                }

                for (Class cls : spanClasses) {
                    // Clear existing spans first
                    Object[] spans = text.getSpans(0, text.length(), cls);
                    for (Object span : spans) {
                        text.removeSpan(span);
                    }
                    // Then copy
                    TextUtils.copySpansFrom(spanned, 0, text.length(), cls, text, 0);
                }
            }
        } catch (IOException e) {
            LogUtils.e(e);
        }
    }

    /**
     * Gets full text which contains BBCodes and the actual content.
     *
     * @return empty string if the content is empty.
     * Otherwise, the content wrapped by BBCodes is returned.
     */
    public String getFullText() {
        Editable text = super.getText();
        if (TextUtils.isEmpty(text)) {
            return "";
        }
        if (mBBCodePairs.isEmpty()) {
            return RichTextUtils.autoInsertBBCode(text.toString());
        }
        StringBuilder builder = new StringBuilder();
        builder.append(text);
        for (BBCodeParser.BBCode key : mBBCodePairs.keySet()) {
            BBCodePair pair = mBBCodePairs.get(key);
            BBCodeTags tags = pair.build();
            builder.insert(0, tags.getOpenTag());
            builder.insert(builder.length(), tags.getCloseTag());
        }
        return builder.toString();
    }
}
