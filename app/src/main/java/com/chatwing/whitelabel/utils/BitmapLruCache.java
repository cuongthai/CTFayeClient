package com.chatwing.whitelabel.utils;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.util.LruCache;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageCache;

/**
 * Author: Huy Nguyen
 * Date: 5/30/13
 * Time: 2:16 AM
 */
public class BitmapLruCache extends LruCache<String, Bitmap> implements ImageCache {

    /**
     * Constructs a new cache
     *
     * @param maxSize
     *         in kilobytes
     */
    public BitmapLruCache(int maxSize) {
        super(maxSize);
    }

    @Override
    @TargetApi(12)
    protected int sizeOf(String key, Bitmap value) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            return value.getByteCount() / 1024;
        } else {
            return (value.getRowBytes() * value.getHeight()) / 1024;
        }
    }

    @Override
    public Bitmap getBitmap(String url) {
        return get(url);
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        put(url, bitmap);
    }

}
