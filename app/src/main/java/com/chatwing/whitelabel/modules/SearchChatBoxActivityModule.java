package com.chatwing.whitelabel.modules;

import android.content.Context;
import android.view.LayoutInflater;


import com.chatwing.whitelabel.activities.SearchChatBoxActivity;
import com.chatwingsdk.modules.ForActivity;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * This module represents objects which exist only for the scope of {@link
 * com.chatwing.whitelabel.activities.SearchChatBoxActivity}. We can safely create singletons
 * using the activity instance because ths entire object graph will only
 * ever exist inside of that activity.<br/>
 * This module is incomplete, since there are dependencies which should be
 * fullfilled by {@link com.chatwingsdk.modules.ChatWingModule}.
 * Thus,object graph of this module must be initiated by adding to object
 * graph of {@link com.chatwingsdk.modules.ChatWingModule}. It can be done
 * in {@link com.chatwing.whitelabel.activities.SearchChatBoxActivity#onCreate(android.os.Bundle)}.
 */
@Module(
        injects = {
                SearchChatBoxActivity.class,
        },
        complete = false
)
public class SearchChatBoxActivityModule {
    private final SearchChatBoxActivity mActivity;

    public SearchChatBoxActivityModule(SearchChatBoxActivity activity) {
        this.mActivity = activity;
    }

    @Provides
    @Singleton
    @ForActivity
    Context provideActivityContext() {
        return mActivity;
    }

    @Provides
    @Singleton
    @ForActivity
    LayoutInflater provideLayoutInflater() {
        return (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
}
