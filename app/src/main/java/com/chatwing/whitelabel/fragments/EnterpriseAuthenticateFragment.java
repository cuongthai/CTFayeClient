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
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.events.UserAuthenticationEvent;
import com.chatwing.whitelabel.managers.ApiManager;
import com.chatwing.whitelabel.pojos.params.oauth.EnterpriseOAuthParams;
import com.chatwing.whitelabel.utils.Utils;
import com.squareup.otto.Bus;

import javax.inject.Inject;

/**
 * Created by cuongthai on 22/09/2014.
 */
public class EnterpriseAuthenticateFragment extends Fragment {
    private static final String CUSTOM_LOGIN_ENCRYPTED_SESSION_KEY = "custom_session";
    private static final String EXTRA_TAG = "tag";
    private String mTag;
    private WebView mWebView;
    private View mLoadingView;
    private ProgressBar mProgressView;
    private InjectableFragmentDelegate mDelegate;

    @Inject
    Bus mBus;

    private WebViewClient mWebviewClient = new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            if (ApiManager.CUSTOM_LOGIN_URL_PATTERN.matcher(url).find()) {
                String encryptedSession = null;
                try {
                    encryptedSession = Utils.getQueryParams(url)
                            .get(CUSTOM_LOGIN_ENCRYPTED_SESSION_KEY)
                            .get(0);
                } catch (Exception e) {
                    //error, do nothing
                }

                if (encryptedSession != null) {
                    EnterpriseOAuthParams params = new EnterpriseOAuthParams(encryptedSession);
                    mBus.post(UserAuthenticationEvent.succeedEvent(mTag, params));
                    showContent(false);
                }
            }
            return false;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            mProgressView.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            mProgressView.setVisibility(View.GONE);
        }
    };

    public static Fragment newInstance(String tag) {
        Bundle args = new Bundle();
        args.putString(EXTRA_TAG, tag);
        EnterpriseAuthenticateFragment instance = new EnterpriseAuthenticateFragment();
        instance.setArguments(args);
        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTag = getArguments().getString(EXTRA_TAG);
    }

    /**
     * Called to instantiate the view. Creates and returns the WebView.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_enterprise_authenticate, container, false);
        mWebView = (WebView) view.findViewById(R.id.webView);
        mLoadingView = view.findViewById(R.id.loading_view);
        mProgressView = (ProgressBar) view.findViewById(R.id.progress_view);
        mProgressView.setMax(100);
        setup(mWebView);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadConversation();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mDelegate = ((InjectableFragmentDelegate)activity);
        mDelegate.inject(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void setup(WebView mWebView) {
        mWebView.setWebViewClient(mWebviewClient);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                mProgressView.setProgress(newProgress);
                if (newProgress == 100) {
                    mProgressView.setVisibility(View.GONE);
                }
            }
        });
    }

    private void showContent(boolean show) {
        if (show) {
            mLoadingView.setVisibility(View.GONE);
            mWebView.setVisibility(View.VISIBLE);
        } else {
            mLoadingView.setVisibility(View.VISIBLE);
            mWebView.setVisibility(View.GONE);
        }
    }

    private void loadConversation() {
        showContent(true);
        mWebView.loadUrl(String.format(ApiManager.CUSTOM_LOGIN_URL));
    }
}
