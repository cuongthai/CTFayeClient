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
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.chatwing.whitelabel.ChatWing;
import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.activities.CommunicationActivity;
import com.chatwing.whitelabel.contentproviders.ChatWingContentProvider;
import com.chatwing.whitelabel.events.SyncUnreadEvent;
import com.chatwing.whitelabel.events.UpdateUserEvent;
import com.chatwing.whitelabel.managers.ApiManager;
import com.chatwing.whitelabel.managers.UserManager;
import com.chatwing.whitelabel.managers.VolleyManager;
import com.chatwing.whitelabel.pojos.User;
import com.chatwing.whitelabel.tables.ChatBoxTable;
import com.chatwing.whitelabel.tables.ConversationTable;
import com.chatwing.whitelabel.utils.LogUtils;
import com.chatwing.whitelabel.views.ErrorMessageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

/**
 * Created by nguyenthanhhuy on 10/30/13.
 */
public class CommunicationDrawerFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {


    protected static final int LOADER_ID_COUNT_CONVERSATIONS_MESSAGES_UNREAD = 5002;
    protected static final int LOADER_ID_CHATBOXES = 5003;

    protected static final String COLUMN_NAME_SUM_UNREAD_COUNT = "sum_unread_count";
    private View mLogoutButton;


    public interface Listener extends InjectableFragmentDelegate {
        void showCategories();

        void showConversations();

        public WebView getWebView();

        void logout();
    }

    @Inject
    UserManager mUserManager;
    @Inject
    ApiManager mApiManager;
    @Inject
    VolleyManager mVolleyManager;
    @Inject
    Bus mBus;
    @Inject
    ErrorMessageView mErrorView;
    @Inject
    ProgressDialog mProgressDialog;

    private Listener mListener;
    protected ImageView mUserAvatarView;
    private TextView mUsernameView;
    protected TextView mAccountTypeView;
    private View mUserInfoContainer;
    private View mLoginButton;
    private View mCategoriesView;
    private TextView mCategoriesUnreadCountView;
    private View mConversationsView;
    private TextView mConversationsUnreadCountView;

    public CommunicationDrawerFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (Listener) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat_boxes_drawer, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mUserAvatarView = (ImageView) view.findViewById(R.id.avatar);
        mUsernameView = (TextView) view.findViewById(R.id.username);
        mAccountTypeView = (TextView) view.findViewById(R.id.account_type);
        mUserInfoContainer = view.findViewById(R.id.user_info_layout);
        mLoginButton = view.findViewById(R.id.login);

        mCategoriesView = view.findViewById(R.id.categories);
        mCategoriesView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.showCategories();
            }
        });
        mCategoriesUnreadCountView = (TextView) view.findViewById(R.id.categories_unread_count);

        mConversationsView = view.findViewById(R.id.conversation);
        mConversationsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.showConversations();
            }
        });
        mConversationsUnreadCountView = (TextView) view.findViewById(R.id.conversation_unread_count);

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), ChatWing.instance(getActivity()).getAuthenticationClass());
                getActivity().startActivityForResult(i, CommunicationActivity.REQUEST_CODE_AUTHENTICATION);
            }
        });

        mLogoutButton = view.findViewById(R.id.logout);
        mLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.logout();
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mListener.inject(this);
        updateUserViews();

        LoaderManager loaderManager = getLoaderManager();
        loaderManager.initLoader(LOADER_ID_CHATBOXES, null, this);
        loaderManager.initLoader(LOADER_ID_COUNT_CONVERSATIONS_MESSAGES_UNREAD, null, this);
    }

    @Override
    public void onStart() {
        super.onStart();
        mBus.register(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUserViews();
    }

    @Override
    public void onStop() {
        super.onStop();
        mBus.unregister(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public View getCategoriesView() {
        return mCategoriesView;
    }


    @Subscribe
    public void onSyncChatboxUnreadComplete(SyncUnreadEvent event) {
        getLoaderManager().restartLoader(LOADER_ID_COUNT_CONVERSATIONS_MESSAGES_UNREAD, null, this);
    }

    @Subscribe
    public void onUpdateUserEvent(UpdateUserEvent event) {
        switch (event.getState()) {
            case CANCELLED:
                hideLoadingDialog();
                break;
            case STARTED:
                showLoadingDialog();
                break;
            case SUCCESS:
                hideLoadingDialog();
                updateUserViews();
                break;
            case ERROR:
                hideLoadingDialog();
                mErrorView.show(R.string.error_failed_to_update_user_profile);
                break;
        }
    }


    protected void updateUserViews() {
        LogUtils.v("Updating User Views");
        User user = mUserManager.getCurrentUser();
        if (user == null) {
            mUserInfoContainer.setVisibility(View.GONE);
            showLoginButton(true);
            mLogoutButton.setVisibility(View.GONE);
            return;
        } else {
            mLogoutButton.setVisibility(View.VISIBLE);
        }
        mUserInfoContainer.setVisibility(View.VISIBLE);
        showLoginButton(false);

        String avatarUrl = mApiManager.getAvatarUrl(user);
        LogUtils.v("Avatar drawer "+avatarUrl);
//        ImageLoader.getInstance().clearDiskCache();
        ImageLoader.getInstance().displayImage(avatarUrl, mUserAvatarView);
        mUsernameView.setText(user.getName());
        mAccountTypeView.setText(mApiManager.getDisplayUserLoginType(user.getLoginType()));
    }

    protected void showLoginButton(boolean show) {
        if (show) {
            mLoginButton.setVisibility(View.VISIBLE);
        } else {
            mLoginButton.setVisibility(View.GONE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        LogUtils.v("Loader running "+id);

        switch (id) {
            case LOADER_ID_COUNT_CONVERSATIONS_MESSAGES_UNREAD:
                Uri uri = ChatWingContentProvider.getConversationsUri();
                return new CursorLoader(
                        getActivity(),
                        uri,
                        new String[]{
                                "sum(" + ConversationTable.UNREAD_COUNT + ") as " + COLUMN_NAME_SUM_UNREAD_COUNT,
                        },
                        null,
                        null,
                        null);
            case LOADER_ID_CHATBOXES:
                uri = ChatWingContentProvider.getCategorizedChatBoxesUri();
                return new CursorLoader(
                        getActivity(),
                        uri,
                        new String[]{
                                "sum(" + ChatBoxTable.UNREAD_COUNT + ") as " + COLUMN_NAME_SUM_UNREAD_COUNT,
                        },
                        null,
                        null,
                        null);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        TextView unreadCountView = getUnreadCountView(loader);
        if (unreadCountView == null) return;

        if (data == null || !data.moveToFirst() || data.getCount() == 0) {
            unreadCountView.setVisibility(View.GONE);
            return;
        }

        int columnIndex = data.getColumnIndexOrThrow(COLUMN_NAME_SUM_UNREAD_COUNT);
        int unreadCount = data.getInt(columnIndex);

        if (unreadCount == 0) {
            unreadCountView.setVisibility(View.GONE);
        } else {
            unreadCountView.setText(Integer.toString(unreadCount));
            unreadCountView.setVisibility(View.VISIBLE);
        }
    }

    protected TextView getUnreadCountView(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case LOADER_ID_COUNT_CONVERSATIONS_MESSAGES_UNREAD:
                return mConversationsUnreadCountView;
            case LOADER_ID_CHATBOXES:
                return mCategoriesUnreadCountView;
            default:
                return null;
        }
    }

    private void hideLoadingDialog() {
        if (mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }

    }

    private void showLoadingDialog() {
        if (!mProgressDialog.isShowing()) {
            mProgressDialog.setMessage(getString(R.string.message_updating_profile));
            mProgressDialog.show();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case LOADER_ID_COUNT_CONVERSATIONS_MESSAGES_UNREAD:
                mConversationsUnreadCountView.setVisibility(View.GONE);
                break;
            case LOADER_ID_CHATBOXES:
                mCategoriesUnreadCountView.setVisibility(View.GONE);
                break;
        }
    }

}
