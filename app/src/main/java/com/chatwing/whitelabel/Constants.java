package com.chatwing.whitelabel;

import java.util.Arrays;
import java.util.List;

/**
 * Created by cuongthai on 26/10/2014.
 */
public interface Constants extends com.chatwingsdk.Constants {
    /**
     * FB constants
     */
    List<String> FB_READ_PERMISSIONS = Arrays.asList("user_about_me");

    /**
     * Google+ constants
     */
    String GOOGLE_PLUS_SCOPES = "oauth2:https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/userinfo.email";

    /**
     * Twitter constants
     */
    String TWITTER_CALLBACK_URL = "chatwing://abc";
    String TWITTER_CONSUMER_KEY = "UjGTlYTd1U6r0E4EoGUWHw";
    String TWITTER_CONSUMER_SECRET = "3nww1xsLJBlpya9fiCQ1hgqMWPMQTivyHzEmktkMw";
}
