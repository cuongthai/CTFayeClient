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

package com.chatwing.whitelabel.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.chatwing.whitelabel.ChatWingApplication;
import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.adapters.EmoticonsPageAdapter;
import com.chatwing.whitelabel.pojos.Emoticon;
import com.chatwing.whitelabel.utils.JsonConstantsProvider;

import java.io.Serializable;

import javax.inject.Inject;

/**
 * Author: Huy Nguyen
 * Date: 7/8/13
 * Time: 3:14 PM
 */
public class EmoticonsFragment extends Fragment {
    @Inject
    EmoticonsPageAdapter adapter;

    private static final String EXTRA_EMOTICONS = "emoticons";
    private InjectableFragmentDelegate mDelegate;

    public static EmoticonsFragment newInstance(Emoticon[] emoticons) {
        EmoticonsFragment instance = new EmoticonsFragment();

        Bundle args = new Bundle();
        args.putSerializable(EXTRA_EMOTICONS, emoticons);
        instance.setArguments(args);

        return instance;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mDelegate = ((InjectableFragmentDelegate) activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mDelegate = null;
    }

    public EmoticonsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_emoticons, container, false);
        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDelegate.inject(this);

        Serializable serializable = getArguments().getSerializable(EXTRA_EMOTICONS);
        Emoticon[] emoticons;
        if (serializable instanceof Emoticon[]){
            emoticons = (Emoticon[]) serializable;
        }else{
            emoticons = JsonConstantsProvider.emoticonObject; //FIXME How can serializable be null?
        }

        adapter.setEmoticons(emoticons);
        GridView viewPager = (GridView) view.findViewById(R.id.gridview);
        viewPager.setAdapter(adapter);
    }
}
