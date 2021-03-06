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

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.chatwing.whitelabel.ChatWing;
import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.activities.CommunicationActivity;
import com.chatwing.whitelabel.contentproviders.ChatWingContentProvider;
import com.chatwing.whitelabel.events.AccountSwitchEvent;
import com.chatwing.whitelabel.events.SyncUnreadEvent;
import com.chatwing.whitelabel.events.UpdateUserEvent;
import com.chatwing.whitelabel.managers.ApiManager;
import com.chatwing.whitelabel.managers.BuildManager;
import com.chatwing.whitelabel.managers.UserManager;
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
public class CommunicationDrawerFragment
        extends BaseFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int LOADER_ID_SYNCED_BOOKMARKS = 5000;
    private static final int LOADER_ID_COUNT_CONVERSATIONS_MESSAGES_UNREAD = 5002;
    private static final int LOADER_ID_CHATBOXES = 5003;

    private static final String COLUMN_NAME_SUM_UNREAD_COUNT = "sum_unread_count";

    @Inject
    protected BuildManager mBuildManager;
    @Inject
    protected UserManager mUserManager;
    @Inject
    protected ApiManager mApiManager;
    @Inject
    protected Bus mBus;
    @Inject
    protected ErrorMessageView mErrorView;
    @Inject
    protected ProgressDialog mProgressDialog;

    private View mNextView;
    private View feedView;
    private View musicView;
    private View mSearchChatBoxView;
    private View mCreateChatBoxView;
    private View bookmarkView;
    private View mLogoutButton;
    private View mUserInfoContainer;
    private View mLoginButton;
    private View mCategoriesView;
    private View mConversationsView;
    private View mAdminContactsView;
    private TextView mCategoriesUnreadCountView;
    private TextView mBookmarksUnreadCountView;
    private TextView mConversationsUnreadCountView;
    private TextView mUsernameView;
    private TextView mAccountTypeView;
    private Listener mListener;
    private ImageView mUserAvatarView;

    public interface Listener extends InjectableFragmentDelegate {
        void showCategories();

        void showConversations();

        void showAdminList();

        void showSettings();

        void updateAvatar();

        void searchChatBox();

        void createChatBox();

        void showBookmarks();

        void openAccountPicker();

        void showFeedsSources();

        void showMusicBox();

        void logout();
    }


    public CommunicationDrawerFragment() {
    }


    @Override
    protected void onAttachToContext(Context context) {
        if (context instanceof Listener) {
            mListener = (Listener) context;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
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
        mAdminContactsView = view.findViewById(R.id.admin_list);
        mAdminContactsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.showAdminList();
            }
        });
        mConversationsUnreadCountView = (TextView) view.findViewById(R.id.conversation_unread_count);

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(),
                        ChatWing.instance(getActivity()).getAuthenticationClass());
                getActivity().startActivityForResult(i,
                        CommunicationActivity.REQUEST_CODE_AUTHENTICATION);
            }
        });


        mLogoutButton = view.findViewById(R.id.logout);
        mLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.logout();
            }
        });


        mUserInfoContainer = view.findViewById(R.id.user_info_layout);
        mNextView = view.findViewById(R.id.next);

        view.findViewById(R.id.settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.showSettings();
            }
        });


        mSearchChatBoxView = view.findViewById(R.id.search_chat_box);
        mSearchChatBoxView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.searchChatBox();
            }
        });

        mCreateChatBoxView = view.findViewById(R.id.create_chat_box);
        mCreateChatBoxView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.createChatBox();
            }
        });

        bookmarkView = view.findViewById(R.id.bookmarks);
        bookmarkView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.showBookmarks();
            }
        });

        feedView = view.findViewById(R.id.feeds);
        feedView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.showFeedsSources();
            }
        });

        musicView = view.findViewById(R.id.music_box);
        musicView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.showMusicBox();
            }
        });
        mUserInfoContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.openAccountPicker();
            }
        });


        mBookmarksUnreadCountView = (TextView) view.findViewById(R.id.bookmarks_unread_count);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mListener.inject(this);
        updateUserViews();

        LoaderManager loaderManager = getLoaderManager();
        loaderManager.initLoader(LOADER_ID_CHATBOXES, null, this);
        loaderManager.initLoader(LOADER_ID_COUNT_CONVERSATIONS_MESSAGES_UNREAD, null, this);
        loaderManager.initLoader(LOADER_ID_SYNCED_BOOKMARKS, null, this);

        if (!mBuildManager.isOfficialChatWingApp()) {
            mCreateChatBoxView.setVisibility(View.GONE);
            mSearchChatBoxView.setVisibility(View.GONE);
            bookmarkView.setVisibility(View.GONE);
            mNextView.setVisibility(View.GONE);
        }

        if (!mBuildManager.isSupportedRss()) {
            feedView.setVisibility(View.GONE);
        }

        if (!mBuildManager.isSupportedMusicBox()) {
            musicView.setVisibility(View.GONE);
        }
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

    @Subscribe
    public void onAccountSwitch(AccountSwitchEvent event) {
        updateUserViews();
    }

    @Subscribe
    public void onSyncChatboxUnreadComplete(SyncUnreadEvent event) {
        getLoaderManager().restartLoader(LOADER_ID_COUNT_CONVERSATIONS_MESSAGES_UNREAD, null, this);
        getLoaderManager().restartLoader(LOADER_ID_SYNCED_BOOKMARKS, null, this);
        getLoaderManager().restartLoader(LOADER_ID_CHATBOXES, null, this);
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


    protected void showLoginButton(boolean show) {
        if (show) {
            mLoginButton.setVisibility(View.VISIBLE);
        } else {
            mLoginButton.setVisibility(View.GONE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        LogUtils.v("Loader running " + id);

        switch (id) {
            case LOADER_ID_COUNT_CONVERSATIONS_MESSAGES_UNREAD:
                Uri uri = ChatWingContentProvider.getConversationsUri();
                return new CursorLoader(
                        getActivity(),
                        uri,
                        new String[]{
                                "sum(" + ConversationTable.UNREAD_COUNT + ") as " +
                                        COLUMN_NAME_SUM_UNREAD_COUNT,
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
                                "sum(" + ChatBoxTable.UNREAD_COUNT + ") as " +
                                        COLUMN_NAME_SUM_UNREAD_COUNT,
                        },
                        null,
                        null,
                        null);
            case LOADER_ID_SYNCED_BOOKMARKS:
                uri = ChatWingContentProvider.getSyncedBookmarksUri();
                return new CursorLoader(
                        getActivity(),
                        uri,
                        new String[]{
                                "sum(" + ChatBoxTable.TABLE_CHAT_BOX
                                        + "." + ChatBoxTable.UNREAD_COUNT + ") as " +
                                        COLUMN_NAME_SUM_UNREAD_COUNT,
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

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case LOADER_ID_COUNT_CONVERSATIONS_MESSAGES_UNREAD:
                mConversationsUnreadCountView.setVisibility(View.GONE);
                break;
            case LOADER_ID_CHATBOXES:
                mCategoriesUnreadCountView.setVisibility(View.GONE);
                break;
            case LOADER_ID_SYNCED_BOOKMARKS:
                mBookmarksUnreadCountView.setVisibility(View.GONE);
        }
    }

    protected TextView getUnreadCountView(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case LOADER_ID_COUNT_CONVERSATIONS_MESSAGES_UNREAD:
                return mConversationsUnreadCountView;
            case LOADER_ID_CHATBOXES:
                return mCategoriesUnreadCountView;
            case LOADER_ID_SYNCED_BOOKMARKS:
                return mBookmarksUnreadCountView;
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

    private void updateUserViews() {
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
        LogUtils.v("Avatar drawer " + avatarUrl);
        ImageLoader.getInstance().displayImage(avatarUrl, mUserAvatarView);
        mUsernameView.setText(user.getName());
        mAccountTypeView.setText(mApiManager.getDisplayUserLoginType(user.getLoginType()));

        if (!mBuildManager.canShowAdminList()) {
            mAdminContactsView.setVisibility(View.GONE);
        }

        //Only allow chatwing to update avatar
        User currentUser = mUserManager.getCurrentUser();
        if (currentUser != null && (currentUser.isChatWing() || currentUser.isAppUser())) {
            mUserAvatarView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.updateAvatar();
                }
            });
        } else {
            mUserAvatarView.setOnClickListener(null);
        }
        if (currentUser != null) {
            mAccountTypeView.setText(mApiManager.getDisplayUserLoginType(currentUser.getLoginType()));
        }
    }


}
