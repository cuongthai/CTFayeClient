package com.chatwing.whitelabel;

import com.pkmmte.pkrss.Category;

import java.util.Arrays;
import java.util.List;

/**
 * Created by cuongthai on 26/10/2014.
 */
public interface Constants {
    boolean DEBUG = ChatWing.isDebugging();
    String CHATWING_SDK_TAG = "ChatWingSDK";
    String CHATWING_BASE_URL =  "http://cloud.chatwing.com";
    String FAYE_URL = CHATWING_BASE_URL + "/comet/1";

    String MAIN_COLOR = "#05b0ff";

    int MAX_NUMBER_OF_CONVERSATIONS = 100;

    int MAX_NUMBER_OF_DEFAULT_USERS= 100;

    /**
     * Authentication types
     */
    String TYPE_APP = "app"; //Predefined user list from dashboard
    String TYPE_CHATWING = "chatwing";
    String TYPE_GUEST = "guest";
    String TYPE_FACEBOOK = "facebook";
    String TYPE_TWITTER = "twitter";
    String TYPE_GOOGLE = "google";
    String TYPE_YAHOO = "yahoo";
    String TYPE_TUMBLR = "tumblr";
    String TYPE_ENTERPRISE = "enterprise";

    int AVATAR_SIZE = 64;
    boolean SHOW_CHAT_BOX_URL = false;

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

    String BUILD_CUSTOM_LOGIN_TYPE = "custom";

    public static final List<Category> CATEGORIES = new Category.ListBuilder()
            .add("Android Central", "http://www.mobilenations.com/rss/mb.xml")
            .add("Wildcat", "http://wildcatsociety.com/category/kentucky-wildcats/feed/")
            .build();
    String YOUTUBE_DEVELOPER_KEY = "341431874860-i09qq280hqvr7lg0qjjo0h6tj57ptldg.apps.googleusercontent.com";
}
