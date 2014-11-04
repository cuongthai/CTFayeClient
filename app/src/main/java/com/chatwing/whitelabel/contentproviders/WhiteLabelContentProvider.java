package com.chatwing.whitelabel.contentproviders;

import com.chatwing.whitelabel.BuildConfig;
import com.chatwingsdk.contentproviders.ChatWingContentProvider;

/**
 * Created by cuongthai on 31/10/2014.
 */
public class WhiteLabelContentProvider extends ChatWingContentProvider {
    public static String AUTHORITY = BuildConfig.APPLICATION_ID+".provider";

}
