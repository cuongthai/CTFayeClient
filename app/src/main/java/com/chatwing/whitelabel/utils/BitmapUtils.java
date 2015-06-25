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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;
import android.widget.ImageView;

import com.chatwing.whitelabel.Constants;


/**
 * Created by steve on 10/06/2014.
 */
public class BitmapUtils {
    public static Bitmap scaleCenterCrop(Bitmap source, int newHeight, int newWidth) {
        int sourceWidth = source.getWidth();
        int sourceHeight = source.getHeight();

        // Compute the scaling factors to fit the new height and width, respectively.
        // To cover the final image, the final scaling will be the bigger
        // of these two.
        float xScale = (float) newWidth / sourceWidth;
        float yScale = (float) newHeight / sourceHeight;
        float scale = Math.max(xScale, yScale);

        // Now get the size of the source bitmap when scaled
        float scaledWidth = scale * sourceWidth;
        float scaledHeight = scale * sourceHeight;

        // Let's find out the upper left coordinates if the scaled bitmap
        // should be centered in the new size give by the parameters
        float left = (newWidth - scaledWidth) / 2;
        float top = (newHeight - scaledHeight) / 2;

        // The target rectangle for the new, scaled version of the source bitmap will now
        // be
        RectF targetRect = new RectF(left, top, left + scaledWidth, top + scaledHeight);

        // Finally, we create a new bitmap of the specified size and draw our new,
        // scaled bitmap onto it.
        Bitmap dest = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(dest);
        canvas.drawBitmap(source, null, targetRect, null);

        return dest;
    }

    public static Bitmap dotBitmap(Context context, Bitmap bitmap, ImageView imageView) {
        RenderScript rs = RenderScript.create(context);
        //Scale up to fit imageView
        bitmap = BitmapUtils.scaleCenterCrop(bitmap, imageView.getMeasuredHeight(), imageView.getMeasuredWidth());

        //this will blur the bitmap with a radius of 8 and save it back to bitmap
        final Allocation input = Allocation.createFromBitmap(rs, bitmap); //use this constructor for best performance, because it uses USAGE_SHARED mode which reuses memory
        final Allocation outputAlloc = Allocation.createTyped(rs, input.getType());
        final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        script.setRadius(8f);
        script.setInput(input);
        script.forEach(outputAlloc);
        outputAlloc.copyTo(bitmap);

        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(),
                Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        paint.setFilterBitmap(false);
        paint.setAntiAlias(true);
        paint.setDither(true);

        //Draw black background
        paint.setColor(0xFF080808);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, 0, bitmap.getWidth(), bitmap.getHeight(), paint);

        int sc = canvas.saveLayer(0, 0, bitmap.getWidth(), bitmap.getHeight(), null,
                Canvas.MATRIX_SAVE_FLAG |
                        Canvas.CLIP_SAVE_FLAG |
                        Canvas.HAS_ALPHA_LAYER_SAVE_FLAG |
                        Canvas.FULL_COLOR_LAYER_SAVE_FLAG |
                        Canvas.CLIP_TO_LAYER_SAVE_FLAG
        );
        //Draw tiny circles
        float circleRadius = 8;
        float padding = 2;
        float centerDiff = 2 * circleRadius + padding;
        float stepW = bitmap.getWidth() / centerDiff;
        float stepH = bitmap.getHeight() / centerDiff;
        for (float i = 0; i <= stepW + 1; i++) {
            for (float j = 0; j <= stepH + 1; j++) {
                canvas.drawCircle(i * centerDiff, j * centerDiff, circleRadius, paint);
            }
        }

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, new Matrix(), paint);
        paint.setXfermode(null);
        canvas.restoreToCount(sc);
        return output;
    }

    /**
     * Detect EXIF info and rotate bitmap to correct orientation
     * @param path
     * @return
     */
    public static Bitmap fixImageEXIF(String path) {
        int orientation;
        try {
            if (path == null) {
                return null;
            }
            Bitmap sample = decodeSampledBitmapFromFile(path, Constants.AVATAR_SIZE, Constants.AVATAR_SIZE);

            ExifInterface exif = new ExifInterface(path);

            orientation = exif
                    .getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);

            Matrix m = new Matrix();
            Bitmap outBitmap = sample;
            if ((orientation == ExifInterface.ORIENTATION_ROTATE_180)) {
                m.postRotate(180);
                outBitmap = Bitmap.createBitmap(sample, 0, 0, sample.getWidth(),
                        sample.getHeight(), m, true);
                return outBitmap;
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                m.postRotate(90);
                outBitmap = Bitmap.createBitmap(sample, 0, 0, sample.getWidth(),
                        sample.getHeight(), m, true);
                return outBitmap;
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                m.postRotate(270);
                outBitmap = Bitmap.createBitmap(sample, 0, 0, sample.getWidth(),
                        sample.getHeight(), m, true);
                return outBitmap;
            }
            return outBitmap;
        } catch (Exception e) {
            return null;
        }
    }

    public static Bitmap decodeSampledBitmapFromFile(String path,
                                                     int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
