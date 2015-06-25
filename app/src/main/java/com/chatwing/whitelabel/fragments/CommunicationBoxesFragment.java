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

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.events.SyncCommunicationBoxEvent;
import com.chatwing.whitelabel.managers.UserManager;
import com.chatwing.whitelabel.utils.LogUtils;
import com.squareup.otto.Bus;

import javax.inject.Inject;

/**
 * Created by cuongthai on 22/10/2014.
 */
public abstract class CommunicationBoxesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final int COMMUNICATIONS_LOADER_ID = 12;
    protected NavigatableFragmentListener mDelegate;

    private TextView mEmptyTextView;
    private ListView mListView;
    protected SwipeRefreshLayout mSwipeRefreshLayout;

    CursorAdapter mListAdapter;
    @Inject
    Bus mBus;
    @Inject
    UserManager mUserManager;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mDelegate = (NavigatableFragmentListener) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_communication_list,
                container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDelegate.inject(this);
        LogUtils.v("Load conversation boxes");
        mEmptyTextView = (TextView) view.findViewById(android.R.id.empty);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        mListView = (ListView) view.findViewById(R.id.listview);
        loadCommunicationFromDb();
        mListAdapter = getCommunicationBoxesAdapter();
        mListView.setAdapter(mListAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Cursor cursor = (Cursor) mListAdapter.getItem(position);
                handleItemClick(cursor);
            }
        });

        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_dark, android.R.color.holo_blue_light, android.R.color.holo_green_light, android.R.color.holo_green_light);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(true);

                if(!mDelegate.onSwipe()){
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            }
        });
    }

    protected abstract CursorAdapter getCommunicationBoxesAdapter();

    protected abstract void handleItemClick(Cursor cursor);

    private void loadCommunicationFromDb() {
        mEmptyTextView.setVisibility(View.GONE);
        getLoaderManager().initLoader(COMMUNICATIONS_LOADER_ID, null, this);

    }

    @Override
    public void onResume() {
        super.onResume();
        mBus.register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mBus.unregister(this);
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mDelegate = null;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return createLoader(i, bundle);
    }

    protected abstract Loader<Cursor> createLoader(int i, Bundle bundle);

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mListAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mListAdapter.swapCursor(null);
    }

    public void onSyncCommunicationBoxEvent(SyncCommunicationBoxEvent event) {
        mSwipeRefreshLayout.setRefreshing(false);
    }
}
