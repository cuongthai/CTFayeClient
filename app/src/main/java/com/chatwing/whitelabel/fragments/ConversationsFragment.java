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
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.adapters.ConversationsAdapter;
import com.chatwing.whitelabel.contentproviders.ChatWingContentProvider;
import com.chatwing.whitelabel.events.UserSelectedConversationEvent;
import com.chatwing.whitelabel.managers.CurrentConversationManager;
import com.chatwing.whitelabel.managers.UserManager;
import com.chatwing.whitelabel.pojos.responses.LoadModeratorsResponse;
import com.chatwing.whitelabel.tables.ConversationTable;
import com.chatwing.whitelabel.tables.DefaultUserTable;
import com.chatwing.whitelabel.utils.LogUtils;
import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Author: Huy Nguyen
 * Date: 7/25/13
 * Time: 9:29 AM
 */
public class ConversationsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final int CONVERSATIONS_LOADER_ID = 10;

    protected NavigatableFragmentListener mDelegate;
    private TextView mEmptyTextView;
    private ListView mConversationsListView;
    private CursorAdapter mListAdapter;

    @Inject
    CurrentConversationManager mCurrentConversationManager;
    @Inject
    UserManager mUserManager;
    @Inject
    Bus mBus;

    public ConversationsFragment() {

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mDelegate = (NavigatableFragmentListener) activity;
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
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDelegate.inject(this);
        LogUtils.v("Load conversation boxes");
        mEmptyTextView = (TextView) view.findViewById(android.R.id.empty);
        mConversationsListView = (ListView) view.findViewById(R.id.listview);
        mListAdapter = new ConversationsAdapter(getActivity(),
                mUserManager.getCurrentUser(),
                null,
                0);

        mConversationsListView.setAdapter(mListAdapter);
        mConversationsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Cursor cursor = (Cursor) mListAdapter.getItem(position);
                int columnConversationIndex = cursor.getColumnIndexOrThrow(ConversationTable.CONVERSATION_ID);
                String conversationId = cursor.getString(columnConversationIndex);
                mBus.post(new UserSelectedConversationEvent(conversationId));
            }
        });

        view.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDelegate.back(ConversationsFragment.this);
            }
        });

        loadConversationsFromDb();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_conversation_list,
                container, false);
    }

    private void loadConversationsFromDb() {
        mEmptyTextView.setVisibility(View.GONE);
        getLoaderManager().initLoader(CONVERSATIONS_LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case CONVERSATIONS_LOADER_ID:
                return new CursorLoader(
                        getActivity(),
                        ChatWingContentProvider.getConversationsUri(),
                        ConversationTable.getMinimumProjection(),
                        null,
                        null,
                        ConversationTable.TABLE + "." + ConversationTable.IS_MODERATOR + " DESC, " +
                                ConversationTable.TABLE + "." + ConversationTable.DATE_UPDATED + " DESC");
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case CONVERSATIONS_LOADER_ID:
                mListAdapter.swapCursor(cursor);
                break;
        }
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case CONVERSATIONS_LOADER_ID:
                mListAdapter.swapCursor(null);
                break;
        }

    }
}
