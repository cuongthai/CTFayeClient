package com.chatwing.whitelabel.fragments;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.events.TaskFinishedEvent;
import com.chatwing.whitelabel.events.UserAuthenticationEvent;
import com.chatwing.whitelabel.pojos.params.oauth.AuthenticationParams;
import com.chatwing.whitelabel.scribeconfigs.ScribeConfig;
import com.chatwing.whitelabel.scribeconfigs.YahooConfig;
import com.chatwing.whitelabel.tasks.GetScribeAccessTokenTask;
import com.chatwing.whitelabel.tasks.GetScribeRequestTokenTask;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.scribe.builder.ServiceBuilder;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import javax.inject.Inject;

/**
 * Author: Huy Nguyen
 * Date: 8/30/13
 * Time: 10:50 AM
 */
public class LoginScribeFragment extends Fragment {
    protected static final String EXTRA_TAG = "tag";
    protected static final String EXTRA_DELEGATE_CLASS_NAME = "delegate_class_name";

    public static LoginScribeFragment newInstance(Class<? extends ScribeConfig> scribeConfigClass,
                                                  String tag) {
        Bundle args = new Bundle(2);
        args.putString(EXTRA_DELEGATE_CLASS_NAME, scribeConfigClass.getName());
        args.putString(EXTRA_TAG, tag);
        LoginScribeFragment instance = new LoginScribeFragment();
        instance.setArguments(args);
        return instance;
    }

    private OAuthService mOAuthService;
    private Token mRequestToken;
    private String mTag;
    private ScribeConfig mScribeConfig;
    private AsyncTask<?, ?, ?> mCurrentTask;

    private WebView mWebView;
    private View mProgressView;
    private TextView mProgressText;
    private View mContentView;
    @Inject
    Bus mBus;

    public LoginScribeFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mTag = args.getString(EXTRA_TAG);

        // Construct the delegate from class name
        String delegateClassName = args.getString(EXTRA_DELEGATE_CLASS_NAME);
        try {
            Class delegateClass = Class.forName(delegateClassName);
            mScribeConfig = (ScribeConfig) delegateClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mWebView != null) {
            mWebView.destroy();
        }

        View v = inflater.inflate(R.layout.fragment_login_webview, container, false);

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
                if (url.contains(mScribeConfig.getCallbackURL())) {
                    setProgressText(R.string.progress_getting_access_token);

                    Uri uri = Uri.parse(url);
                    String oauthVerifier = uri.getQueryParameter("oauth_verifier");
                    Verifier verifier = new Verifier(oauthVerifier);
                    GetScribeAccessTokenTask task = new GetScribeAccessTokenTask(
                            mBus, mRequestToken, verifier);
                    task.execute(getOAuthService());
                    startTask(task);

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

        //This fix yahoo login to reasonable ui
        if (YahooConfig.class.getName().equalsIgnoreCase(mScribeConfig.getClass().getName())) {
            mWebView.clearCache(true);
            webSettings.setAppCacheEnabled(false);
            webSettings.setBuiltInZoomControls(true);
            webSettings.setLoadWithOverviewMode(true);
            webSettings.setUseWideViewPort(true);
        }

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mProgressView = view.findViewById(R.id.progress_container);
        mProgressText
                = (TextView) mProgressView.findViewById(R.id.progress_text);
        mContentView = view.findViewById(R.id.content_container);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        InjectableFragmentDelegate delegate = (InjectableFragmentDelegate) getActivity();
        delegate.inject(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        setProgressText(R.string.progress_getting_request_token);
        startTask(new GetScribeRequestTokenTask(mBus).execute(getOAuthService()));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= 11) {
            mWebView.onResume();
        }
        mBus.register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT >= 11) {
            mWebView.onPause();
        }
        mBus.unregister(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        stopCurrentTask();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mWebView != null) {
            mWebView.destroy();
            mWebView = null;
        }
    }

    ///////////////////////////////////////////////////////////
    // Manage current async task
    //////////////////////////////////////////////////////////
    private void startTask(AsyncTask<?, ?, ?> task) {
        stopCurrentTask();
        mCurrentTask = task;
    }

    private void stopCurrentTask() {
        if (mCurrentTask != null) {
            if (mCurrentTask.getStatus() != AsyncTask.Status.FINISHED) {
                mCurrentTask.cancel(true);
            }
            mCurrentTask = null;
        }
    }

    //////////////////////////////////////////////////////
    // Handle task events
    //////////////////////////////////////////////////////
    @Subscribe
    public void onTaskFinished(TaskFinishedEvent event) {
        AsyncTask<?, ?, ?> task = event.getTask();
        if (task != mCurrentTask) {
            return;
        }

        mCurrentTask = null;

        Exception exception = event.getException();
        if (exception != null) {
            mBus.post(UserAuthenticationEvent.failedEvent(mTag, exception));
            return;
        }

        Token token = (Token) event.getResult();
        if (task instanceof GetScribeRequestTokenTask) {
            mRequestToken = token;
            if (mWebView != null) {
                mWebView.loadUrl(getOAuthService().getAuthorizationUrl(mRequestToken));
            }
        } else if (task instanceof GetScribeAccessTokenTask) {
            AuthenticationParams params = mScribeConfig.getAuthenticationParams(token);
            mBus.post(UserAuthenticationEvent.succeedEvent(mTag, params));
        }
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

    //////////////////////////////////////////////////////////////
    // Other instance methods
    //////////////////////////////////////////////////////////////
    private OAuthService getOAuthService() {
        if (mOAuthService == null) {
            mOAuthService = new ServiceBuilder()
                    .provider(mScribeConfig.getProvider())
                    .apiKey(mScribeConfig.getApiKey())
                    .apiSecret(mScribeConfig.getApiSecret())
                    .callback(mScribeConfig.getCallbackURL())
                    .build();
        }
        return mOAuthService;
    }
}
