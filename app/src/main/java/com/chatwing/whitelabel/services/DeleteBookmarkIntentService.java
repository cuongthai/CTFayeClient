package com.chatwing.whitelabel.services;

import android.content.Context;
import android.content.Intent;

import com.chatwing.whitelabel.events.DeleteBookmarkEvent;
import com.chatwing.whitelabel.managers.ApiManager;
import com.chatwing.whitelabel.pojos.responses.DeleteBookmarkResponse;
import com.chatwingsdk.pojos.User;
import com.chatwingsdk.services.BaseIntentService;

import java.util.ArrayList;

import javax.inject.Inject;

/**
 * Created by steve on 23/06/2014.
 */
public class DeleteBookmarkIntentService extends ExtendBaseIntentService {
    public static final String BOOKMARK_IDS_KEY = "bookmark_ids";
    private DeleteBookmarkEvent mDeleteBookmarkEvent;
    @Inject
    ApiManager mApiManager;

    public DeleteBookmarkIntentService() {
        super("DeleteBookmarkIntentService");
    }

    public static void start(Context context, ArrayList<Integer> bookmarkIds) {
        Intent intent = new Intent(context, DeleteBookmarkIntentService.class);
        intent.putIntegerArrayListExtra(BOOKMARK_IDS_KEY, bookmarkIds);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        User user = mUserManager.getCurrentUser();
        if (user == null || !mUserManager.userCanBookmark()) {
            post(new DeleteBookmarkEvent(new Exception()));
            return;
        }
        ArrayList<Integer> bookmarkIds = intent.getIntegerArrayListExtra(BOOKMARK_IDS_KEY);
        for (Integer bookmarkId : bookmarkIds) {
            try {
                DeleteBookmarkResponse deleteBookmarkResponse = mApiManager.deleteBookmark(
                        user,
                        bookmarkId);
                mDeleteBookmarkEvent = new DeleteBookmarkEvent(deleteBookmarkResponse);
            } catch (Exception e) {
                mDeleteBookmarkEvent = new DeleteBookmarkEvent(e);
            }
            post(mDeleteBookmarkEvent);
        }
    }

    private void post(final DeleteBookmarkEvent event) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mBus.post(event);
            }
        });
    }

}
