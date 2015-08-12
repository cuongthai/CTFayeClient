/*
 * Copyright (C) 2014 ChatWing
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chatwing.whitelabel.fragments;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.CursorTreeAdapter;

import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.adapters.CategoryCursorTreeAdapter;
import com.chatwing.whitelabel.contentproviders.ChatWingContentProvider;
import com.chatwing.whitelabel.events.ChatBoxUnreadCountChangedEvent;
import com.chatwing.whitelabel.events.SyncUnreadEvent;
import com.chatwing.whitelabel.events.UserSelectedChatBoxEvent;
import com.chatwing.whitelabel.tables.CategoryTable;
import com.chatwing.whitelabel.tables.ChatBoxTable;
import com.chatwing.whitelabel.utils.LogUtils;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

/**
 * Author: Huy Nguyen
 * Date: 3/28/13
 * Time: 6:42 AM
 */
public class CategoriesFragment extends BaseExpandableListFragment {
    private static final int ID_CATEGORIES_LOADER = 100;
    public static final String UNREAD_GROUP_COUNT_COLLUMN_NAME = "count";
    public static final String GROUP_NAME = "title";

    @Inject
    Bus mBus;
    private NavigatableFragmentListener mListener;

    public CategoriesFragment() {
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View back = view.findViewById(R.id.back);
        back.setVisibility(View.VISIBLE);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.back(CategoriesFragment.this);
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mListener = (NavigatableFragmentListener) getActivity();
        mListener.inject(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mBus.register(this);
        getLoaderManager().restartLoader(getLoaderId(), null, this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mBus.unregister(this);
    }

    @Subscribe
    public void onChatBoxUnreadCountChanged(ChatBoxUnreadCountChangedEvent event) {
        // Ideally, the adapter should observe child cursors using
        // ContentObserver and re-query when change occurs.
        // But it doesn't, so we need to subscribe to  this event and explicitly
        // tell the adapter to re-query.
        notifyDataSetChanged();
    }

    @Subscribe
    public void onSyncUnreadEvent(SyncUnreadEvent event){
        LogUtils.v("Unread Download Done, loading to UI");
        getLoaderManager().restartLoader(getLoaderId(), null, this);
    }

    @Override
    public int getEmptyTextId() {
        return R.string.empty_chat_boxes;
    }

    @Override
    public CursorTreeAdapter constructAdapter() {
        return CategoryCursorTreeAdapter.newInstance(getActivity());
    }

    @Override
    public int getLoaderId() {
        return ID_CATEGORIES_LOADER;
    }

    @Override
    public void onChildClicked(Cursor childCursor) {
        int idIndex = childCursor.getColumnIndexOrThrow(ChatBoxTable._ID);
        mBus.post(new UserSelectedChatBoxEvent(childCursor.getInt(idIndex)));
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = ChatWingContentProvider.getAggregatedCategoriesUri();
        String projection[] = new String[]{
                CategoryTable.TABLE_CATEGORY + "." + CategoryTable._ID,
                CategoryTable.TABLE_CATEGORY + "." + CategoryTable.TITLE + "  as " + GROUP_NAME,
                "SUM (" + ChatBoxTable.UNREAD_COUNT + ") as " + UNREAD_GROUP_COUNT_COLLUMN_NAME};

        return new CursorLoader(getActivity(), uri, projection, null, null, null);
    }

}
