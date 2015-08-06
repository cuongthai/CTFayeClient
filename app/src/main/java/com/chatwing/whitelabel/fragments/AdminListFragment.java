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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.adapters.UsersListAdapter;
import com.chatwing.whitelabel.contentproviders.ChatWingContentProvider;
import com.chatwing.whitelabel.events.UserSelectedDefaultUsersEvent;
import com.chatwing.whitelabel.managers.CurrentConversationManager;
import com.chatwing.whitelabel.managers.UserManager;
import com.chatwing.whitelabel.pojos.User;
import com.chatwing.whitelabel.pojos.params.CreateConversationParams;
import com.chatwing.whitelabel.pojos.responses.LoadModeratorsResponse;
import com.chatwing.whitelabel.tables.DefaultUserTable;
import com.chatwing.whitelabel.utils.LogUtils;
import com.squareup.otto.Bus;

import javax.inject.Inject;

/**
 * Author: Huy Nguyen
 * Date: 7/25/13
 * Time: 9:29 AM
 */
public class AdminListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final int DEFAULT_USERS_LOADER_ID = 20;

    protected NavigatableFragmentListener mDelegate;
    private TextView mEmptyTextView;
    private ListView mDefaultUsersListView;
    private UsersListAdapter mUsersAdapter;

    @Inject
    CurrentConversationManager mCurrentConversationManager;
    @Inject
    UserManager mUserManager;
    @Inject
    Bus mBus;

    public AdminListFragment() {

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
        mDefaultUsersListView = (ListView) view.findViewById(R.id.listview);
        mUsersAdapter = new UsersListAdapter(getActivity(),
                null,
                0);

        mDefaultUsersListView.setAdapter(mUsersAdapter);

        mDefaultUsersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) mUsersAdapter.getItem(position);
                LoadModeratorsResponse.Moderator moderator = DefaultUserTable.getModerator(cursor);

                User currentUser = mUserManager.getCurrentUser();

                if (currentUser != null && !currentUser.getIdentifier().equals(moderator.getIdentifier())) { // Dont send message to ourself
                    mBus.post(new UserSelectedDefaultUsersEvent(
                            new CreateConversationParams.SimpleUser(moderator.getLoginId(), moderator.getLoginType())));
                }
            }
        });

        view.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDelegate.back(AdminListFragment.this);
            }
        });

        loadDefaultUsersFromDb();
    }

    private void loadDefaultUsersFromDb() {
        mEmptyTextView.setVisibility(View.GONE);
        getLoaderManager().initLoader(DEFAULT_USERS_LOADER_ID, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_conversation_list,
                container, false);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case DEFAULT_USERS_LOADER_ID:
                return new CursorLoader(
                        getActivity(),
                        ChatWingContentProvider.getModeratorsUri(),
                        DefaultUserTable.getMinimumProjection(),
                        null,
                        null,
                        null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            case DEFAULT_USERS_LOADER_ID:
                mUsersAdapter.swapCursor(cursor);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case DEFAULT_USERS_LOADER_ID:
                mUsersAdapter.swapCursor(null);
                break;
        }

    }
}
