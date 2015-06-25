package com.chatwing.whitelabel.tasks;

import com.chatwing.whitelabel.managers.ApiManager;
import com.chatwing.whitelabel.pojos.responses.SearchChatBoxResponse;
import com.squareup.otto.Bus;

import javax.inject.Inject;

/**
 * Created by nguyenthanhhuy on 10/26/13.
 */
public class SearchChatBoxTask extends CallbackTask<Void, Void, SearchChatBoxResponse> {
    private ApiManager mApiManager;
    private String mQuery;
    private int mOffset;
    private int mLimit;

    @Inject
    SearchChatBoxTask(Bus bus, ApiManager apiManager) {
        super(bus);
        mApiManager = apiManager;
    }

    public void setParams(String query, int offset, int limit) {
        mQuery = query;
        mOffset = offset;
        mLimit = limit;
    }

    public int getOffset() {
        return mOffset;
    }

    @Override
    protected SearchChatBoxResponse run(Void... params) throws Exception {
        return mApiManager.searchChatBox(mQuery, mOffset, mLimit);
    }
}
