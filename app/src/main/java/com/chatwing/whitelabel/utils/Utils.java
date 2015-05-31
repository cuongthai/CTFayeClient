package com.chatwing.whitelabel.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.pkmmte.pkrss.PkRSS;

/**
 * Created by steve on 15/05/2015.
 */
public class Utils {
    public static void buildSingleton(Context context) {
        new PkRSS.Builder(context).handler(new Handler(Looper.getMainLooper())).buildSingleton();
    }
}
