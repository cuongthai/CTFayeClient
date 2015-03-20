package com.chatwing.whitelabel.contentproviders;

import com.chatwing.whitelabel.BuildConfig;
import com.chatwingsdk.contentproviders.ChatWingContentProvider;

/**
 * Created by steve on 12/03/2015.
 */
public class WhiteLabelContentProvider extends ChatWingContentProvider{
    public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".provider";

}
