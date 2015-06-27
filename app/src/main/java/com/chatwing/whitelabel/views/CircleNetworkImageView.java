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
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.chatwing.whitelabel.R;


/**
 * Created by steve on 04/07/2014.
 * Reference: https://github.com/Pkmmte/CircularImageView/blob/master/CircularImageView/src/com/pkmmte/circularimageview/CircularImageView.java
 */
public class CircleNetworkImageView extends ImageView {
    // Border & Selector configuration variables
    private boolean hasBorder;
    private boolean hasSelector;
    private boolean isSelected;
    private int borderWidth;
    private int canvasSize;
    private int selectorStrokeWidth;

    // Objects used for the actual drawing
    private BitmapShader shader;
    private Bitmap image;
    private Paint paint;
    private Paint paintBorder;
    private Paint paintSelectorBorder;
    private ColorFilter selectorFilter;

    public CircleNetworkImageView(Context context) {
        this(context, null);
    }

    public CircleNetworkImageView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.circularImageViewStyle);
    }

    public CircleNetworkImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    /**
     * Initializes paint objects and sets desired attributes.
     *
     * @param context
     * @param attrs
     * @param defStyle
     */
    private void init(Context context, AttributeSet attrs, int defStyle) {
        // Initialize paint objects
        paint = new Paint();
        paint.setAntiAlias(true);
        paintBorder = new Paint();
        paintBorder.setAntiAlias(true);
        paintSelectorBorder = new Paint();
        paintSelectorBorder.setAntiAlias(true);

        // load the styled attributes and set their properties
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.CircularImageView, defStyle, 0);

        // Check if border and/or border is enabled
        hasBorder = attributes.getBoolean(R.styleable.CircularImageView_border, true);
        hasSelector = attributes.getBoolean(R.styleable.CircularImageView_selector, true);

        // Set border properties if enabled
        if (hasBorder) {
            int defaultBorderSize = (int) (2 * context.getResources().getDisplayMetrics().density + 0.5f);
            setBorderWidth(attributes.getDimensionPixelOffset(R.styleable.CircularImageView_border_width, defaultBorderSize));
            setBorderColor(attributes.getColor(R.styleable.CircularImageView_border_color, Color.WHITE));
        }

        // Set selector properties if enabled
        if (hasSelector) {
            int defaultSelectorSize = (int) (2 * context.getResources().getDisplayMetrics().density + 0.5f);
            setSelectorColor(attributes.getColor(R.styleable.CircularImageView_selector_color, Color.TRANSPARENT));
            setSelectorStrokeWidth(attributes.getDimensionPixelOffset(R.styleable.CircularImageView_selector_stroke_width, defaultSelectorSize));
            setSelectorStrokeColor(attributes.getColor(R.styleable.CircularImageView_selector_stroke_color, context.getResources().getColor(R.color.accent)));
        }

        // Add shadow if enabled
        if (attributes.getBoolean(R.styleable.CircularImageView_shadow, false))
            addShadow();

        // We no longer need our attributes TypedArray, give it back to cache
        attributes.recycle();
    }

    /**
     * Sets the CircularImageView's border width in pixels.
     *
     * @param borderWidth
     */
    public void setBorderWidth(int borderWidth) {
        this.borderWidth = borderWidth;
        this.requestLayout();
        this.invalidate();
    }

    /**
     * Sets the CircularImageView's basic border color.
     *
     * @param borderColor
     */
    public void setBorderColor(int borderColor) {
        if (paintBorder != null)
            paintBorder.setColor(borderColor);
        this.invalidate();
    }

    /**
     * Sets the color of the selector to be draw over the
     * CircularImageView. Be sure to provide some opacity.
     *
     * @param selectorColor
     */
    public void setSelectorColor(int selectorColor) {
        this.selectorFilter = new PorterDuffColorFilter(selectorColor, PorterDuff.Mode.SRC_ATOP);
        this.invalidate();
    }

    /**
     * Sets the stroke width to be drawn around the CircularImageView
     * during click events when the selector is enabled.
     *
     * @param selectorStrokeWidth
     */
    public void setSelectorStrokeWidth(int selectorStrokeWidth) {
        this.selectorStrokeWidth = selectorStrokeWidth;
        this.requestLayout();
        this.invalidate();
    }

    /**
     * Sets the stroke color to be drawn around the CircularImageView
     * during click events when the selector is enabled.
     *
     * @param selectorStrokeColor
     */
    public void setSelectorStrokeColor(int selectorStrokeColor) {
        if (paintSelectorBorder != null)
            paintSelectorBorder.setColor(selectorStrokeColor);
        this.invalidate();
    }

    /**
     * Adds a dark shadow to this CircularImageView.
     */
    public void addShadow() {
        setLayerType(LAYER_TYPE_SOFTWARE, paintBorder);
        paintBorder.setShadowLayer(4.0f, 0.0f, 2.0f, Color.BLACK);
    }


    @Override
    public void onDraw(Canvas canvas) {
        // Don't draw anything without an image
        if (image == null)
            return;

        // Nothing to draw (Empty bounds)
        if (image.getHeight() == 0 || image.getWidth() == 0)
            return;

        // Compare canvas sizes
        int oldCanvasSize = canvasSize;

        canvasSize = canvas.getWidth();
        if (canvas.getHeight() < canvasSize)
            canvasSize = canvas.getHeight();

        // Reinitialize shader, if necessary
        if (oldCanvasSize != canvasSize)
            refreshBitmapShader();

        // Apply shader to paint
        paint.setShader(shader);

        // Keep track of selectorStroke/border width
        int outerWidth = 0;

        // Get the exact X/Y axis of the view
        int center = canvasSize / 2;


        if (hasSelector && isSelected) { // Draw the selector stroke & apply the selector filter, if applicable
            outerWidth = selectorStrokeWidth;
            center = (canvasSize - (outerWidth * 2)) / 2;

            paint.setColorFilter(selectorFilter);
            canvas.drawCircle(center + outerWidth, center + outerWidth, ((canvasSize - (outerWidth * 2)) / 2) + outerWidth - 4.0f, paintSelectorBorder);
        } else if (hasBorder) { // If no selector was drawn, draw a border and clear the filter instead... if enabled
            outerWidth = borderWidth;
            center = (canvasSize - (outerWidth * 2)) / 2;

            paint.setColorFilter(null);
            canvas.drawCircle(center + outerWidth, center + outerWidth, ((canvasSize - (outerWidth * 2)) / 2) + outerWidth - 4.0f, paintBorder);
        } else // Clear the color filter if no selector nor border were drawn
            paint.setColorFilter(null);
        // Draw the circular image itself
        canvas.drawCircle(center + outerWidth, center + outerWidth, ((canvasSize - (outerWidth * 2)) / 2) - 4.0f, paint);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        // Check for clickable state and do nothing if disabled
        if (!this.isClickable()) {
            this.isSelected = false;
            return super.onTouchEvent(event);
        }

        // Set selected state based on Motion Event
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                this.isSelected = true;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_SCROLL:
            case MotionEvent.ACTION_OUTSIDE:
            case MotionEvent.ACTION_CANCEL:
                this.isSelected = false;
                break;
        }

        // Redraw image and return super type
        this.invalidate();
        return super.dispatchTouchEvent(event);
    }

    public void invalidate(Rect dirty) {
        super.invalidate(dirty);
        image = drawableToBitmap(getDrawable());
    }

    public void invalidate(int l, int t, int r, int b) {
        super.invalidate(l, t, r, b);
        image = drawableToBitmap(getDrawable());
    }

    @Override
    public void invalidate() {
        super.invalidate();
        image = drawableToBitmap(getDrawable());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = measureHeight(heightMeasureSpec);
        //Make container square
        setMeasuredDimension(height, height);
    }

    private int measureHeight(int measureSpecHeight) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpecHeight);
        int specSize = MeasureSpec.getSize(measureSpecHeight);

        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        } else if (specMode == MeasureSpec.AT_MOST) {
            // The child can be as large as it wants up to the specified size.
            result = specSize;
        } else {
            // Measure the text (beware: ascent is a negative number)
            result = canvasSize;
        }

        return (result + 2);
    }

    /**
     * Convert a drawable object into a Bitmap
     *
     * @param drawable
     * @return
     */
    public Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable == null) { // Don't do anything without a proper drawable
            return null;
        } else if (drawable instanceof BitmapDrawable) { // Use the getBitmap() method instead if BitmapDrawable
            return ((BitmapDrawable) drawable).getBitmap();
        }

        // Create Bitmap object out of the drawable
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    /**
     * Reinitializes the shader texture used to fill in
     * the Circle upon drawing.
     */
    public void refreshBitmapShader() {
        if (image != null && canvasSize > 0) {
            shader = new BitmapShader(
                    Bitmap.createScaledBitmap(image, canvasSize, canvasSize, true),
                    Shader.TileMode.CLAMP,
                    Shader.TileMode.CLAMP);
        }
    }

    public boolean isSelected() {
        return this.isSelected;
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        refreshBitmapShader();
    }

}
