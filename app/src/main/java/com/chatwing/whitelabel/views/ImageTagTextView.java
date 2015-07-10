package com.chatwing.whitelabel.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.chatwing.whitelabel.Constants;
import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.managers.VolleyManager;
import com.chatwing.whitelabel.parsers.BBCodeParser;
import com.chatwing.whitelabel.utils.LogUtils;
import com.chatwing.whitelabel.utils.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Author: Huy Nguyen
 * Date: 6/10/13
 * Time: 11:27 AM
 * <p/>
 * ImageTagTextView allows asynchronous image loading for image tags in its
 * content.
 */
public class ImageTagTextView extends TextView implements Html.ImageGetter {
    private String mBBCode;
    private Map<String, String> mEmoticons;
    private int mImageMaxWidth;
    private int mImageMaxHeight;
    private Map<String, ImageLoader.ImageContainer> mContainers;
    private List<String> mFilters;
    private VolleyManager mVolleyManager;
    private BBCodeParser mBBCodeParser;

    public ImageTagTextView(Context context) {
        super(context);
        setup();
    }

    public ImageTagTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    public ImageTagTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setup();
    }

    private void setup() {
        mContainers = new TreeMap<String, ImageLoader.ImageContainer>();
    }

    public void setEmoticons(Map<String, String> emoticons) {
        mEmoticons = emoticons;
    }

    public void setImageMaxWidth(int imageMaxWidth) {
        mImageMaxWidth = imageMaxWidth;
    }

    public void setImageMaxHeight(int imageMaxHeight) {
        mImageMaxHeight = imageMaxHeight;
    }

    public void setFilters(List<String> filters) {
        mFilters = filters;
    }

    public void setVolleyManager(VolleyManager volleyManager) {
        mVolleyManager = volleyManager;
    }

    public void setBBCodeParser(BBCodeParser bbCodeParser) {
        this.mBBCodeParser = bbCodeParser;
    }

    public void setBBCodeText(String text) {
        mBBCode = text;
        cancelRequests();
        loadBBCode();
    }

    public String getBBCode() {
        return mBBCode;
    }

    private void loadBBCode() {
        if (TextUtils.isEmpty(mBBCode)) {
            return;
        }
        Spanned result = new SpannableStringBuilder(mBBCode);
        try {
            result = mBBCodeParser.parseFull(mBBCode, this, mEmoticons);

            // Apply filters
            if (mFilters != null && mFilters.size() > 0) {
                // First, replace occurrences of filtered words in the text
                // of "result" with asterisks.
                String filteredString = StringUtils.applyFilters(
                        result.toString(),
                        mFilters,
                        Constants.FILTER_REPLACE_SEQUENCE);

                // Create a new clone of "result" with same spans but the text
                // is "filteredString".
                SpannableStringBuilder filteredSpanned = new SpannableStringBuilder(filteredString);
                Object[] spans = result.getSpans(0, result.length(), Object.class);
                if (spans != null) {
                    for (Object span : spans) {
                        filteredSpanned.setSpan(
                                span,
                                result.getSpanStart(span),
                                result.getSpanEnd(span),
                                result.getSpanFlags(span));
                    }
                }

                result = filteredSpanned;
            }

        } catch (IOException e) {
            LogUtils.e(e);
        }
        setText(result);
    }

    @Override
    public Drawable getDrawable(String source) {
        final String url = source.trim();

        if (TextUtils.isEmpty(url)) {
            // Nothing to load.
            return getDefaultDrawable();
        }

        // The source can be the id of an resource image. So handle that case first.
        if (url.startsWith(BBCodeParser.RESOURCE_ID_PREFIX)) {
            try {
                String idString = url.replace(BBCodeParser.RESOURCE_ID_PREFIX, "");
                int id = Integer.parseInt(idString);
                return getDrawable(id);
            } catch (NumberFormatException exc) {
                LogUtils.e("Failed to load img tag with source: " + source);
                return null;
            }
        }

        if (Uri.parse(url).getHost() == null) {
            // Looks like the url is malformed. Can't load anything from
            // network, so let's stop here with the default image.
            // If we continue, Volley will assume that the url is valid and
            // try to get its host and NPE will be thrown (#127).
            return getDefaultDrawable();
        }

        if (mContainers.containsKey(url)) {
            // A request is running. We should wait for it instead of
            // creating a new one.
            //
            // In fact, there won't be any problem if we create a new one.
            // However, it is inefficient because there will be 2 image
            // containers for the same request and listeners of those containers
            // do exactly the same thing.
            return getDefaultDrawable();
        }

        ImageLoader imageLoader = mVolleyManager.getImageLoader();
        ImageLoader.ImageContainer imageContainer = imageLoader.get(url,
                new ImageLoader.ImageListener() {
                    @Override
                    public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                        // This method can be called immediately with a cached bitmap or to
                        // indicate that we should use the default bitmap (ImageLoader.java:193).
                        // Since we checked for cached bitmap beforehand and
                        // the TextView itself handles default bitmap,
                        // we only care about response from network here.
                        // In this case, we need to reload the BBCode which
                        // will trigger another request but it will be returned
                        // immediately with a cached bitmap.
                        if (!isImmediate) {
                            mContainers.remove(url);
                            loadBBCode();
                        }
                    }

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mContainers.remove(url);
                        LogUtils.e(error);
                    }
                },
                mImageMaxWidth,
                mImageMaxHeight);

        Bitmap bitmap = imageContainer.getBitmap();
        if (bitmap != null) {
            // There is a cached bitmap, so this image container is returned
            // immediately. Use it then.
            Drawable drawable = new BitmapDrawable(getResources(), bitmap);
            drawable.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
            return drawable;
        }

        // There is no running request nor cached bitmap for this url. Let's
        // wait for the new request to finish.
        mContainers.put(url, imageContainer);
        // Meanwhile, show default loading image
        return getDefaultDrawable();
    }

    private Drawable getDefaultDrawable() {
        return getDrawable(R.drawable.ic_spinner2);
    }

    private Drawable getDrawable(int resId) {
        Drawable drawable = getResources().getDrawable(resId);
        drawable.setBounds(new Rect(
                0,
                0,
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight()
        ));
        return drawable;
    }

    @Override
    protected void onDetachedFromWindow() {
        cancelRequests();
        super.onDetachedFromWindow();
    }

    private void cancelRequests() {
        if (!mContainers.isEmpty()) {
            for (ImageLoader.ImageContainer c : mContainers.values()) {
                c.cancelRequest();
            }
            mContainers.clear();
        }
    }
}
