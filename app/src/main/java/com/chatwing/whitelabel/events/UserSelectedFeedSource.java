package com.chatwing.whitelabel.events;

import com.pkmmte.pkrss.Category;

/**
 * Created by steve on 21/05/2015.
 */
public class UserSelectedFeedSource {
    private final Category mCategory;

    public UserSelectedFeedSource(Category category) {
        mCategory = category;
    }

    public Category getCategory() {
        return mCategory;
    }
}
