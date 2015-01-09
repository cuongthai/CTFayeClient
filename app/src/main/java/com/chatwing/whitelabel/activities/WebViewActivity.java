package com.chatwing.whitelabel.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.chatwing.whitelabel.R;
import com.chatwingsdk.fragments.WebViewFragment;


/**
 * Author: Huy Nguyen
 * Date: 7/9/13
 * Time: 4:28 PM
 * <p/>
 * An activity that holds a web view ({@link com.chatwingsdk.fragments.WebViewFragment}
 * and provides controls such as loading indicator, refresh,
 * go back and forward buttons on the action bar.
 */
public class WebViewActivity extends ActionBarActivity {

    public static final String EXTRA_URL = "url";

    private String mUrl;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_web_view);

        if (savedInstanceState == null) {
            mUrl = getIntent().getStringExtra(EXTRA_URL);
        } else {
            mUrl = savedInstanceState.getString(EXTRA_URL);
        }
        if (TextUtils.isEmpty(mUrl)) {
            finish();
            return;
        }
        mUrl = mUrl.trim();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        WebView webView = getWebView();
        configWebView(webView);
        webView.loadUrl(mUrl);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(EXTRA_URL, mUrl);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_web_view, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        // Update refresh menu item and progress indicator if the web view is
        // loading.
        WebView webView = getWebView();
        MenuItem refreshItem = menu.findItem(R.id.refresh);
        boolean isLoading = webView.getProgress() != 100;
        setProgressBarIndeterminateVisibility(isLoading);
        refreshItem.setVisible(!isLoading);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                getWebView().reload();
                return true;
            case R.id.open_in_browser:
                try {
                    Uri uri = Uri.parse(mUrl);
                    Intent i = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(i);
                    finish();
                } catch (Exception e) {

                }
                return true;
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void configWebView(WebView webView) {
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                getSupportActionBar().setTitle(url);
                invalidateOptionsMenu();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                getSupportActionBar().setTitle(view.getTitle());
                invalidateOptionsMenu();
            }
        });

        WebSettings webSettings = webView.getSettings();
        webSettings.setAppCacheEnabled(false);
        webSettings.setJavaScriptEnabled(true);
    }

    private WebView getWebView() {
        String tag = getString(R.string.fragment_tag_web_view);
        WebViewFragment fragment
                = (WebViewFragment) getSupportFragmentManager().findFragmentByTag(tag);
        return fragment.getWebView();
    }
}
