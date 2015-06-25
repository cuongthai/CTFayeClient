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
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.CursorTreeAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.chatwing.whitelabel.R;


/**
 * Created by nguyenthanhhuy on 10/30/13.
 */
public abstract class BaseExpandableListFragment extends Fragment
        implements ExpandableListView.OnChildClickListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    private View mProgressView;
    private View mContentView;
    private CursorTreeAdapter mAdapter;

    public abstract int getEmptyTextId();

    public abstract CursorTreeAdapter constructAdapter();

    public abstract int getLoaderId();

    public abstract void onChildClicked(Cursor childCursor);

    //////////////////////////////////////////////////////////////////
    // Fragment life-cycle
    //////////////////////////////////////////////////////////////////
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_expandable_list, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mProgressView = view.findViewById(R.id.progress_container);
        mContentView = view.findViewById(R.id.content_container);
        TextView emptyView = (TextView) view.findViewById(android.R.id.empty);
        ExpandableListView listView
                = (ExpandableListView) view.findViewById(android.R.id.list);
        mAdapter = constructAdapter();

        emptyView.setText(getEmptyTextId());
        listView.setEmptyView(emptyView);
        listView.setOnChildClickListener(this);
        listView.setAdapter(mAdapter);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadData();
    }

    ///////////////////////////////////////////////////////////////
    // ExpandableListView.OnChildClickListener
    //////////////////////////////////////////////////////////////
    @Override
    public boolean onChildClick(ExpandableListView parent, View v,
                                int groupPosition, int childPosition, long id) {
        // Highlight selected chat box
        int flatPosition = parent.getFlatListPosition(
                ExpandableListView.getPackedPositionForChild(groupPosition, childPosition));
        parent.setItemChecked(flatPosition, true);

        Cursor c = mAdapter.getChild(groupPosition, childPosition);
        onChildClicked(c);
        return true;
    }

    ////////////////////////////////////////////////////////////////
    // LoaderCallbacks methods
    ////////////////////////////////////////////////////////////////
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.setGroupCursor(data);
        setContentShown(true);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.setGroupCursor(null);
    }

    /////////////////////////////////////////////////////////////
    // Progress view management
    ////////////////////////////////////////////////////////////
    private boolean isShowingContent() {
        return mContentView.getVisibility() == View.VISIBLE;
    }

    public void setContentShown(boolean shown) {
        if (isShowingContent() == shown) {
            return;
        }
        if (shown) {
            mProgressView.startAnimation(AnimationUtils.loadAnimation(getActivity(),
                    android.R.anim.fade_out));
            mContentView.startAnimation(AnimationUtils.loadAnimation(getActivity(),
                    android.R.anim.fade_in));
            mProgressView.setVisibility(View.GONE);
            mContentView.setVisibility(View.VISIBLE);
        } else {
            mProgressView.startAnimation(AnimationUtils.loadAnimation(getActivity(),
                    android.R.anim.fade_in));
            mContentView.startAnimation(AnimationUtils.loadAnimation(getActivity(),
                    android.R.anim.fade_out));
            mProgressView.setVisibility(View.VISIBLE);
            mContentView.setVisibility(View.GONE);
        }
    }

    /////////////////////////////////////////////////////////////
    // Adapter management
    /////////////////////////////////////////////////////////////
    public final void notifyDataSetChanged() {
        mAdapter.notifyDataSetChanged();
    }

    ////////////////////////////////////////////////////////////
    // Other instance methods
    ////////////////////////////////////////////////////////////
    private void loadData() {
        setContentShown(false);
        getLoaderManager().initLoader(getLoaderId(), null, this);
    }
}
