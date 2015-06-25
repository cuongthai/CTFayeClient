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
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.helpers.NetworkViewHelper;
import com.chatwing.whitelabel.utils.BitmapUtils;

/**
 * Created by steve on 3/4/14.
 */
public class RatioImageView extends ImageView implements NetworkViewHelper.Listener {
    private static final float ACCEPTABLE_SCALE_RATIO_THRESHOLD = 0.2f;
    private final int mHeight;
    private final int mWidth;
    private final NetworkViewHelper mHelper;
    private final boolean mIsDotView;

    public RatioImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = getContext().obtainStyledAttributes(
                attrs,
                R.styleable.RatioImageView);
        mHeight = a.getInteger(R.styleable.RatioImageView_height_ratio, 3);
        mWidth = a.getInteger(R.styleable.RatioImageView_width_ratio, 4);
        mIsDotView = a.getBoolean(R.styleable.RatioImageView_dot_view, false);
        mHelper = new NetworkViewHelper(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);

        setMeasuredDimension(width, (int) ((1.0f * mHeight / mWidth) * width));
    }

    public void setImageUrl(String url, ImageLoader imageLoader) {
        mHelper.setImageUrl(url, imageLoader);
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public void onCancelRequest() {
        loadDefaultAvatar();
    }

    @Override
    public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
        Bitmap bitmap = response.getBitmap();
        if (bitmap != null) {
            float scaleRatio = 1.0f * bitmap.getWidth() / getMeasuredWidth();
            if (mIsDotView && scaleRatio < ACCEPTABLE_SCALE_RATIO_THRESHOLD) {
                bitmap = BitmapUtils.dotBitmap(getContext(), bitmap, this);
            } else {
                setScaleType(ScaleType.CENTER_CROP);
            }
            setImageBitmap(bitmap);
        } else {
            loadDefaultAvatar();
        }
    }

    private void loadDefaultAvatar() {
        setImageResource(R.drawable.default_avatar);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        loadDefaultAvatar();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        mHelper.onLayout(changed, l, t, r, b);
    }

    @Override
    protected void onDetachedFromWindow() {
        mHelper.onDetachedFromWindow();
        super.onDetachedFromWindow();
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        mHelper.drawableStateChanged();
    }
}
