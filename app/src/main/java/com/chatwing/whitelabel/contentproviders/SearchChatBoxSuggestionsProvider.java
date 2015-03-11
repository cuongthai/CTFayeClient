package com.chatwing.whitelabel.contentproviders;

import android.content.SearchRecentSuggestionsProvider;

/**
 * Created by nguyenthanhhuy on 10/26/13.
 */
public class SearchChatBoxSuggestionsProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "com.chatwing.SearchChatBoxSuggestionsProvider";
    public final static int MODE = DATABASE_MODE_QUERIES;

    public SearchChatBoxSuggestionsProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}
