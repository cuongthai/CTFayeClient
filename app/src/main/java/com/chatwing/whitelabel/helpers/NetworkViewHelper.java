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

package com.chatwing.whitelabel.helpers;

import android.text.TextUtils;
import android.view.View;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;

/**
 * Author: Huy Nguyen
 * Date: 5/30/13
 * Time: 1:28 AM
 * <p/>
 * A helper class that is convenient for a view to handle image loading using
 * Volley. The idea is borrowed from {@link com.android.volley.toolbox
 * .NetworkImageView}.
 * <p/>
 * User of this class should override {@link View#onLayout(boolean, int,
 * int, int, int)}, {@link View#onDetachedFromWindow()},
 * {@link View#drawableStateChanged()} and call
 * corresponding methods of this helper.
 */
public class NetworkViewHelper {
    /**
     * The URL of the network image to load
     */
    private String mUrl;

    /**
     * Local copy of the ImageLoader.
     */
    private ImageLoader mImageLoader;

    /**
     * Current ImageContainer. (either in-flight or finished)
     */
    private ImageLoader.ImageContainer mImageContainer;

    private Listener mListener;

    public interface Listener extends ImageLoader.ImageListener {
        View getView();

        void onCancelRequest();
    }

    public NetworkViewHelper(Listener mListener) {
        this.mListener = mListener;
    }

    /**
     * Sets URL of the image that should be loaded into this view. Note that calling this will
     * immediately either set the cached image (if available) or the default image specified by
     * {@link com.android.volley.toolbox.NetworkImageView#setDefaultImageResId(int)} on the view.
     * <p/>
     * NOTE: If applicable, {@link com.android.volley.toolbox.NetworkImageView#setDefaultImageResId(int)} and
     * {@link com.android.volley.toolbox.NetworkImageView#setErrorImageResId(int)} should be called prior to calling
     * this function.
     *
     * @param url
     *         The URL that should be loaded into this ImageView.
     * @param imageLoader
     *         ImageLoader that will be used to make the request.
     */
    public void setImageUrl(String url, ImageLoader imageLoader) {
        mUrl = url;
        mImageLoader = imageLoader;
        // The URL has potentially changed. See if we need to load it.
        loadImageIfNecessary(false);
    }

    /**
     * Loads the image for the view if it isn't already loaded.
     *
     * @param isInLayoutPass
     *         True if this was invoked from a layout pass, false otherwise.
     */
    private void loadImageIfNecessary(final boolean isInLayoutPass) {
        int width = mListener.getView().getWidth();
        int height = mListener.getView().getHeight();

        // if the view's bounds aren't known yet, hold off on loading the image.
        if (width == 0 && height == 0) {
            return;
        }

        // if the URL to be loaded in this view is empty, cancel any old requests and clear the
        // currently loaded image.
        if (TextUtils.isEmpty(mUrl)) {
            if (mImageContainer != null) {
                mImageContainer.cancelRequest();
                mImageContainer = null;
            }
            mListener.onCancelRequest();
            return;
        }

        // if there was an old request in this view, check if it needs to be canceled.
        if (mImageContainer != null && mImageContainer.getRequestUrl() != null) {
            if (mImageContainer.getRequestUrl().equals(mUrl)) {
                // if the request is from the same URL, return.
                return;
            } else {
                // if there is a pre-existing request, cancel it if it's fetching a different URL.
                mImageContainer.cancelRequest();
                mListener.onCancelRequest();
            }
        }

        // The pre-existing content of this view didn't match the current URL. Load the new image
        // from the network.
        ImageLoader.ImageContainer newContainer = mImageLoader.get(mUrl,
                new ImageLoader.ImageListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mListener.onErrorResponse(error);
                    }

                    @Override
                    public void onResponse(final ImageLoader.ImageContainer response, boolean isImmediate) {
                        // If this was an immediate response that was delivered inside of a layout
                        // pass do not set the image immediately as it will trigger a requestLayout
                        // inside of a layout. Instead, defer setting the image by posting back to
                        // the main thread.
                        if (isImmediate && isInLayoutPass) {
                            mListener.getView().post(new Runnable() {
                                @Override
                                public void run() {
                                    onResponse(response, false);
                                }
                            });
                            return;
                        }

                        mListener.onResponse(response, isImmediate);
                    }
                });

        // update the ImageContainer to be the new bitmap container.
        mImageContainer = newContainer;
    }

    public void onLayout(boolean changed, int left, int top, int right,
                         int bottom) {
        loadImageIfNecessary(true);
    }

    public void onDetachedFromWindow() {
        if (mImageContainer != null) {
            // If the view was bound to an image request, cancel it and clear
            // out the image from the view.
            mImageContainer.cancelRequest();
            mListener.onCancelRequest();
            // also clear out the container so we can reload the image if necessary.
            mImageContainer = null;
        }
    }

    public void drawableStateChanged() {
        mListener.getView().invalidate();
    }
}
