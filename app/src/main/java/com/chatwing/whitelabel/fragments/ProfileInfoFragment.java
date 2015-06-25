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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.chatwing.whitelabel.events.ViewProfileEvent;


/**
 * Created by steve on 3/6/14.
 */
public class ProfileInfoFragment extends WebViewFragment {
    private static final String PROFILE_KEY = "PROFILE_KEY";

    public static Fragment newInstance(ViewProfileEvent profile) {
        ProfileInfoFragment profileInfoFragment = new ProfileInfoFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(PROFILE_KEY, profile);
        profileInfoFragment.setArguments(bundle);
        return profileInfoFragment;
    }

    public ProfileInfoFragment() {
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewProfileEvent profile = (ViewProfileEvent) getArguments().getSerializable(PROFILE_KEY);
        configWebView(getWebView());
        getWebView().loadUrl(profile.getUrl());
    }

    private void configWebView(WebView webView) {
        WebSettings webSettings = webView.getSettings();
        webSettings.setAppCacheEnabled(false);
        webSettings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
    }
}
