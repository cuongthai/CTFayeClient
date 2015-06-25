package com.chatwing.whitelabel.fragments;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.chatwing.whitelabel.BuildConfig;
import com.chatwing.whitelabel.Constants;
import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.events.UserAuthenticationEvent;
import com.chatwing.whitelabel.modules.ForMainThread;
import com.chatwing.whitelabel.pojos.oauth.OAuthParams;
import com.chatwing.whitelabel.pojos.params.oauth.AuthenticationParams;
import com.chatwing.whitelabel.utils.LogUtils;
import com.squareup.otto.Bus;

import javax.inject.Inject;

import twitter4j.AsyncTwitter;
import twitter4j.AsyncTwitterFactory;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;


/**
 * Author: Huy Nguyen
 * Date: 4/14/13
 * Time: 4:55 PM
 */
public class LoginTwitterFragment extends Fragment {

    private static final String KEY_DENIED = "denied";
    private static final String KEY_OAUTH_VERIFIER = "oauth_verifier";
    private static final String EXTRA_TAG = "tag";

    public static LoginTwitterFragment newInstance(String tag) {
        Bundle args = new Bundle();
        args.putString(EXTRA_TAG, tag);
        LoginTwitterFragment instance = new LoginTwitterFragment();
        instance.setArguments(args);
        return instance;
    }

    @Inject
    Bus mBus;
    @Inject
    @ForMainThread
    Handler mHandler;
    private WebView mWebView;
    private View mProgressView;
    private TextView mProgressText;
    private View mContentView;
    private AsyncTwitter mAsyncTwitter;
    private RequestToken mRequestToken;
    private String mTag;

    public LoginTwitterFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTag = getArguments().getString(EXTRA_TAG);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mWebView != null) {
            mWebView.destroy();
        }

        View v = inflater.inflate(
                R.layout.fragment_login_webview, container, false);

        mWebView = (WebView) v.findViewById(R.id.webview);
        // Fix #10 (WebView doesn't gain input focus).
        mWebView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_UP:
                        if (!v.hasFocus()) {
                            v.requestFocus();
                        }
                        break;
                }
                return false;
            }
        });
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                LogUtils.v("Load: " + url);
                if (url.contains(BuildConfig.TWITTER_CALLBACK_URL)) {
                    Uri uri = Uri.parse(url);

                    String deniedParam = uri.getQueryParameter(KEY_DENIED);
                    if (!TextUtils.isEmpty(deniedParam)) {
                        // The user taps the Cancel button on WebView.
                        mBus.post(UserAuthenticationEvent.canceledEvent(mTag));
                        return true;
                    }

                    setProgressText(R.string.progress_getting_access_token);
                    getAsyncTwitter().getOAuthAccessTokenAsync(
                            mRequestToken,
                            uri.getQueryParameter(KEY_OAUTH_VERIFIER));

                    return true;
                } else {
                    setProgressText(R.string.progress_loading_url);
                }

                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                setContentShown(true);
            }
        });

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setAppCacheEnabled(false);
        webSettings.setJavaScriptEnabled(true);

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mProgressView = view.findViewById(R.id.progress_container);
        mProgressText
                = (TextView) mProgressView.findViewById(R.id.progress_text);
        mContentView = view.findViewById(R.id.content_container);

        setProgressText(R.string.progress_getting_request_token);
        getAsyncTwitter()
                .getOAuthRequestTokenAsync(BuildConfig.TWITTER_CALLBACK_URL);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        InjectableFragmentDelegate delegate = (InjectableFragmentDelegate) getActivity();
        delegate.inject(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= 11) {
            mWebView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT >= 11) {
            mWebView.onPause();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAsyncTwitter != null) {
            mAsyncTwitter.shutdown();
            mAsyncTwitter = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mWebView != null) {
            mWebView.destroy();
            mWebView = null;
        }
    }

    private AsyncTwitter getAsyncTwitter() {
        if (mAsyncTwitter == null) {
            AsyncTwitterFactory factory = new AsyncTwitterFactory();
            mAsyncTwitter = factory.getInstance();
            LogUtils.v("Consumer key "+BuildConfig.TWITTER_CONSUMER_KEY);
            LogUtils.v("Consumer secret "+BuildConfig.TWITTER_CONSUMER_SECRET);
            mAsyncTwitter.setOAuthConsumer(BuildConfig.TWITTER_CONSUMER_KEY,
                    BuildConfig.TWITTER_CONSUMER_SECRET);
            mAsyncTwitter.addListener(new TwitterListener());
        }

        return mAsyncTwitter;
    }

    ////////////////////////////////////////////////////////
    // Handle Twitter events
    ////////////////////////////////////////////////////////
    private class TwitterListener extends TwitterAdapter {
        @Override
        public void gotOAuthRequestToken(final RequestToken token) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    mRequestToken = token;
                    if (mWebView != null) {
                        mWebView.loadUrl(mRequestToken.getAuthenticationURL());
                    }
                }
            };
            runOnUiThread(runnable);
        }

        @Override
        public void gotOAuthAccessToken(final AccessToken token) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    AuthenticationParams params = new OAuthParams(
                            Constants.TYPE_TWITTER,
                            token.getToken(),
                            token.getTokenSecret(),
                            new TwitterJsonParams(
                                    token.getUserId(),
                                    token.getScreenName())
                    );
                     mBus.post(UserAuthenticationEvent.succeedEvent(mTag, params));
                }
            };
            runOnUiThread(runnable);
        }

        @Override
        public void onException(final TwitterException te,
                                TwitterMethod method) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    mBus.post(UserAuthenticationEvent.failedEvent(mTag, te));
                }
            };
            runOnUiThread(runnable);
        }
    }

    private void runOnUiThread(Runnable runnable) {
        if (mHandler == null) {
            return;
        }
        mHandler.post(runnable);
    }

    ///////////////////////////////////////////////////////////
    // Manage progress view
    ///////////////////////////////////////////////////////////
    private boolean isShowingContent() {
        return mContentView.getVisibility() == View.VISIBLE;
    }

    private void setContentShown(boolean shown) {
        if (isShowingContent() == shown) {
            return;
        }
        if (shown) {
            mProgressView.startAnimation(AnimationUtils.loadAnimation(getActivity(),
                    android.R.anim.fade_out));
            mContentView.startAnimation(AnimationUtils.loadAnimation(getActivity(),
                    android.R.anim.fade_in));
            mProgressView.setVisibility(View.GONE);
            mContentView.setVisibility(View.VISIBLE);
        } else {
            mProgressView.startAnimation(AnimationUtils.loadAnimation(getActivity(),
                    android.R.anim.fade_in));
            mContentView.startAnimation(AnimationUtils.loadAnimation(getActivity(),
                    android.R.anim.fade_out));
            mProgressView.setVisibility(View.VISIBLE);
            mContentView.setVisibility(View.GONE);
        }
    }

    private void setProgressText(int resId) {
        setContentShown(false);
        mProgressText.setText(resId);
    }
}
