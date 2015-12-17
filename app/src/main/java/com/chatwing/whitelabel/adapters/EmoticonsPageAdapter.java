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

package com.chatwing.whitelabel.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.events.AppendEmoticonEvent;
import com.chatwing.whitelabel.managers.ApiManager;
import com.chatwing.whitelabel.modules.ForActivity;
import com.chatwing.whitelabel.pojos.Emoticon;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.squareup.otto.Bus;

import javax.inject.Inject;

/**
 * Author: Huy Nguyen
 * Date: 7/8/13
 * Time: 3:59 PM
 */
public class EmoticonsPageAdapter extends BaseAdapter {
    @Inject
    Bus mBus;
    @Inject
    @ForActivity
    LayoutInflater mInflater;

    @Inject
    ApiManager mApiManager;

    @Inject
    @ForActivity
    Context context;

    private final DisplayImageOptions defaultOptions;


    private Emoticon[] mEmoticons;

    public EmoticonsPageAdapter() {
        defaultOptions =
                new DisplayImageOptions.Builder()
                        .cacheInMemory(true)
                        .build();
    }

    public void setEmoticons(Emoticon[] emoticons) {
        mEmoticons = emoticons;
    }

    @Override
    public int getCount() {
        return mEmoticons.length;
    }

    @Override
    public Emoticon getItem(int position) {
        return mEmoticons[position];
    }

    @Override
    public long getItemId(int position) {
        return mEmoticons[position].hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        pl.droidsonroids.gif.GifImageView imageView;
        if (convertView == null) {
            imageView = (pl.droidsonroids.gif.GifImageView) mInflater.inflate(R.layout.view_emoticon, parent, false);
        } else {
            imageView = (pl.droidsonroids.gif.GifImageView) convertView;
        }

        final Emoticon emoticon = getItem(position);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Notify about this event. If we want to use listener,
                // it will need to go up multiple levels.
                // Thus, broadcasting an event like this is more efficient
                // and helps to decouple components.
                mBus.post(new AppendEmoticonEvent(emoticon.getSymbol()));
            }
        });

        String fullEmoticonUrl = mApiManager.getFullEmoticonUrl(emoticon.getImage());

        if (fullEmoticonUrl.endsWith(".gif")) {
            Glide.with(context).load(fullEmoticonUrl).diskCacheStrategy(DiskCacheStrategy.ALL).into(imageView);
        } else {
            ImageLoader.getInstance().displayImage(fullEmoticonUrl, imageView, defaultOptions);
        }

        return imageView;
    }
}
