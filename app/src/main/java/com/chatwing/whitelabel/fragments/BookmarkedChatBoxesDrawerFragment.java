package com.chatwing.whitelabel.fragments;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.adapters.BookmarkBoxesAdapter;
import com.chatwing.whitelabel.contentproviders.ChatWingContentProvider;
import com.chatwing.whitelabel.events.UserSelectedChatBoxEvent;
import com.chatwing.whitelabel.managers.UserManager;
import com.chatwing.whitelabel.services.DeleteBookmarkIntentService;
import com.chatwing.whitelabel.tables.ChatBoxTable;
import com.chatwing.whitelabel.tables.SyncedBookmarkTable;
import com.chatwing.whitelabel.utils.StatisticTracker;
import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.HashSet;

import javax.inject.Inject;

public class BookmarkedChatBoxesDrawerFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemLongClickListener {

    private static final int SYNCED_BOOKMARKS_LOADER = 200;

    private ActionMode mActionMode;
    private BookmarkBoxesAdapter mAdapter;
    @Inject
    Bus mBus;
    @Inject
    UserManager mUserManager;

    private NavigatableFragmentListener mListener;

    public BookmarkedChatBoxesDrawerFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bookmarked_chat_boxes, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAdapter = new BookmarkBoxesAdapter(
                getActivity(),
                null,
                0);
        setListAdapter(mAdapter);

        getLoaderManager().initLoader(SYNCED_BOOKMARKS_LOADER, null, this);
        getListView().setOnItemLongClickListener(this);

        view.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.back(BookmarkedChatBoxesDrawerFragment.this);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mBus.register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (this.mActionMode != null) {
            this.mActionMode.finish();
        }
        mBus.unregister(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mListener = (NavigatableFragmentListener) getActivity();
        mListener.inject(this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = ChatWingContentProvider.getSyncedBookmarksUri();
        return new CursorLoader(
                getActivity(),
                uri,
                new String[]{
                        SyncedBookmarkTable.BOOKMARK_ID,
                        ChatBoxTable.TABLE_CHAT_BOX + "." + ChatBoxTable._ID,
                        ChatBoxTable.TABLE_CHAT_BOX + "." + ChatBoxTable.NAME,
                        ChatBoxTable.TABLE_CHAT_BOX + "." + ChatBoxTable.DATA,
                        ChatBoxTable.TABLE_CHAT_BOX + "." + ChatBoxTable.KEY,
                        ChatBoxTable.TABLE_CHAT_BOX + "." + ChatBoxTable.FAYE_CHANNEL,
                        ChatBoxTable.TABLE_CHAT_BOX + "." + ChatBoxTable.ALIAS,
                        ChatBoxTable.UNREAD_COUNT,
                        ChatBoxTable.LAST_READ
                },
                null,
                null,
                SyncedBookmarkTable.TABLE + "." + SyncedBookmarkTable.CREATED_DATE +" DESC"
        );
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> cursorLoader, Cursor cursor) {
        StatisticTracker.trackNumberOfBookmarksPerUser(cursor.getCount());
        getListAdapter().swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> cursorLoader) {
        getListAdapter().swapCursor(null);
    }

    @Override
    public CursorAdapter getListAdapter() {
        return (CursorAdapter) super.getListAdapter();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (mActionMode != null) {
            // if action mode, toggle checked state of item
            Cursor cursor = (Cursor) getListAdapter().getItem(position);
            if (cursor != null) {
                mAdapter.toggleBookmarkChecked(cursor);
                mActionMode.invalidate();
            }
        } else {
            mBus.post(new UserSelectedChatBoxEvent((int) id));
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int pos, long l) {
        if (mActionMode != null) {
            // if already in action mode - do nothing
            return false;
        }
        mAdapter.setBookmarkChecked((Cursor) mAdapter.getItem(pos), true);
        mActionMode = ((ActionBarActivity)getActivity()).startSupportActionMode(new ActionModeCallback());
        mActionMode.invalidate();
        return true;
    }

    private final class ActionModeCallback implements ActionMode.Callback {
        private String selected = getActivity().getString(R.string.message_bookmarks_selected);

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mAdapter.enterMultiMode();
            // save global action mode
            mActionMode = mode;
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            // remove previous items
            menu.clear();
            final int checked = mAdapter.getBookmarkSelectedCount();

            // update title with number of checked items
            mode.setTitle(checked + " " + this.selected);
            switch (checked) {
                case 0:
                    // if nothing checked - exit action mode
                    mode.finish();
                    return true;
                default:
                    getActivity().getMenuInflater().inflate(
                            R.menu.menu_bookmark_delete, menu);
                    return true;
            }
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.delete:
                    removeSelectedBookmarkFromDb();
                    mAdapter.clearSelectedBookmark();
                    mode.finish();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mAdapter.exitMultiMode();
            mAdapter.clearSelectedBookmark();
            // don't forget to remove it, because we are assuming that if it's not null we are in ActionMode
            mActionMode = null;
        }
    }

    /**
     * Run a task to delete bookmarks remotely then run a batch delete on local
     */
    private void removeSelectedBookmarkFromDb() {
        HashSet<Integer> selectedBookmarkIds = mAdapter.getSelectedBookmarkIds();
        DeleteBookmarkIntentService.start(getActivity(), new ArrayList<Integer>(selectedBookmarkIds));
    }
}
