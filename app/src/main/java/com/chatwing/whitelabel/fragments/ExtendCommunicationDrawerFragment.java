package com.chatwing.whitelabel.fragments;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.events.AccountSwitchEvent;
import com.chatwingsdk.events.internal.SyncUnreadEvent;
import com.chatwing.whitelabel.managers.BuildManager;
import com.chatwingsdk.contentproviders.ChatWingContentProvider;
import com.chatwingsdk.fragments.CommunicationDrawerFragment;
import com.chatwingsdk.managers.UserManager;
import com.chatwingsdk.tables.ChatBoxTable;
import com.chatwingsdk.utils.LogUtils;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

/**
 * Created by steve on 17/12/2014.
 */
public class ExtendCommunicationDrawerFragment extends CommunicationDrawerFragment {
    protected static final int LOADER_ID_SYNCED_BOOKMARKS = 5000;

    private Listener mListener;
    private View mSearchChatBoxView;
    private View mCreateChatBoxView;
    private TextView mBookmarksUnreadCountView;
    private View mUserInfoContainer;
    private View bookmarkView;
    @Inject
    BuildManager mBuildManager;
    @Inject
    UserManager mUserManager;
    private View mNextView;
    private TextView mWebsiteTv;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUserInfoContainer = view.findViewById(R.id.user_info_layout);
        mNextView = view.findViewById(R.id.next);
        mWebsiteTv = (TextView)view.findViewById(R.id.websiteTv);

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
        LoaderManager loaderManager = getLoaderManager();
        loaderManager.initLoader(LOADER_ID_SYNCED_BOOKMARKS, null, this);

        if (!mBuildManager.isOfficialChatWingApp()) {
            mCreateChatBoxView.setVisibility(View.GONE);
            mSearchChatBoxView.setVisibility(View.GONE);
            bookmarkView.setVisibility(View.GONE);
            mNextView.setVisibility(View.GONE);

            mWebsiteTv.setVisibility(View.GONE);
            //TODO FIXME only apply for Kentucky!
            mWebsiteTv.setText(Html.fromHtml("<a href='http://wildcatsociety.com/'>http://wildcatsociety.com/</a>"));
            mWebsiteTv.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (Listener) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    protected TextView getUnreadCountView(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case LOADER_ID_SYNCED_BOOKMARKS:
                return mBookmarksUnreadCountView;
            default:
                return super.getUnreadCountView(loader);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case LOADER_ID_SYNCED_BOOKMARKS:
                mBookmarksUnreadCountView.setVisibility(View.GONE);
                break;
            default:
                super.onLoaderReset(loader);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_ID_SYNCED_BOOKMARKS:
                Uri uri = ChatWingContentProvider.getSyncedBookmarksUri();
                return new CursorLoader(
                        getActivity(),
                        uri,
                        new String[]{
                                "sum(" + ChatBoxTable.TABLE_CHAT_BOX + "." + ChatBoxTable.UNREAD_COUNT + ") as " + COLUMN_NAME_SUM_UNREAD_COUNT,
                        },
                        null,
                        null,
                        null);
            default:
                return super.onCreateLoader(id, args);
        }
    }

    @Override
    protected void updateUserViews() {
        super.updateUserViews();

        //Only allow chatwing to update avatar
        if (mUserManager.getCurrentUser() != null && mUserManager.getCurrentUser().isChatWing()) {
            mUserAvatarView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.updateAvatar();
                }
            });
        }else{
            mUserAvatarView.setOnClickListener(null);
        }
    }

    @com.squareup.otto.Subscribe
    public void onUpdateUserEvent(com.chatwingsdk.events.internal.UpdateUserEvent event) {
        super.onUpdateUserEvent(event);
    }

    @Subscribe
    public void onSyncChatboxUnreadComplete(SyncUnreadEvent event){
        super.onSyncChatboxUnreadComplete(event);
        getLoaderManager().restartLoader(LOADER_ID_SYNCED_BOOKMARKS, null, this);
        getLoaderManager().restartLoader(LOADER_ID_CHATBOXES, null, this);
    }

    @Subscribe
    public void onAccountSwitch(AccountSwitchEvent event) {
        updateUserViews();
    }

    public  interface Listener extends CommunicationDrawerFragment.Listener {
        void showSettings();

        void updateAvatar();

        void searchChatBox();

        void createChatBox();

        void showBookmarks();

        void openAccountPicker();
    }

}
