    package com.chatwing.whitelabel;

import com.chatwingsdk.ChatWing;

import java.util.Arrays;
import java.util.List;

/**
 * Created by cuongthai on 26/10/2014.
 */
public interface Constants extends com.chatwingsdk.Constants {

    String FILTER_REPLACE_SEQUENCE = "*";

    /**
     * FB constants
     */
    List<String> FB_READ_PERMISSIONS = Arrays.asList("user_about_me");

    /**
     * Google+ constants
     */
    String GOOGLE_PLUS_SCOPES = "oauth2:https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/userinfo.email";


    String FEEDBACK_EMAIL = "info@chatwing.com";
    boolean ALLOW_SHARE_CHATBOX = false;

    String BUILD_LOGIN_TYPE = "custom";

    String FLURRY_API_KEY = "TJXB56MR5DYMQY5HN85P";

}
