package com.chatwing.whitelabel.activities;


import android.view.Menu;

/**
 * Created by steve on 02/07/2014.
 */
public class NoMenuWebViewActivity extends WebViewActivity{
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return false;
    }
}
