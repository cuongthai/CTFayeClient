package com.chatwing.whitelabel.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.chatwing.whitelabel.R;
import com.chatwingsdk.tables.SyncedBookmarkTable;
import com.chatwingsdk.pojos.ChatBox;
import com.chatwingsdk.tables.ChatBoxTable;
import com.chatwingsdk.utils.LogUtils;

import java.util.HashSet;

/**
 * Created by steve on 1/4/14.
 */
public class BookmarkBoxesAdapter extends CursorAdapter {
    // multi selection mode flag
    private boolean mIsMultiMode;
    private HashSet<Integer> mBookmarkIds;

    public BookmarkBoxesAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mBookmarkIds = new HashSet<Integer>();
    }

    public void enterMultiMode() {
        mIsMultiMode = true;
        notifyDataSetChanged();
    }

    public void exitMultiMode() {
        mIsMultiMode = false;
        notifyDataSetChanged();
    }

    public void setBookmarkChecked(Cursor cursor, boolean checked) {
        Integer bookmarkId = cursor.getInt(cursor.getColumnIndexOrThrow(SyncedBookmarkTable.BOOKMARK_ID));
        LogUtils.v("setBookmarkChecked "+bookmarkId);
        if (checked) {
            mBookmarkIds.add(bookmarkId);
        } else {
            mBookmarkIds.remove(bookmarkId);
        }
        if (mIsMultiMode) {
            notifyDataSetChanged();
        }
    }

    public void toggleBookmarkChecked(Cursor cursor) {
        Integer bookmarkId = cursor.getInt(cursor.getColumnIndexOrThrow(SyncedBookmarkTable.BOOKMARK_ID));
        if (mBookmarkIds.contains(bookmarkId)) {
            mBookmarkIds.remove(bookmarkId);
        } else {
            mBookmarkIds.add(bookmarkId);
        }
        notifyDataSetChanged();
    }

    public int getBookmarkSelectedCount() {
        return mBookmarkIds.size();
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.item_title_subtitle_with_unread_count, viewGroup, false);
        return v;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView bookmarkTv = (TextView) view.findViewById(android.R.id.text1);
        TextView unreadCountTv = (TextView) view.findViewById(android.R.id.text2);
        TextView aliasTv = (TextView) view.findViewById(android.R.id.summary);

        //Change background state when in multi mode
        Integer bookmarkId = cursor.getInt(cursor.getColumnIndexOrThrow(SyncedBookmarkTable.BOOKMARK_ID));
        LogUtils.v("Bindview "+bookmarkId);
        view.setBackgroundResource(mIsMultiMode ? R.drawable.item_bookmark_multi_selector : R.drawable.item_selector);
        if (mBookmarkIds.contains(bookmarkId)) {
            // if this item is checked - set checked state
            view.getBackground().setState(
                    new int[]{android.R.attr.state_checked});
        } else {
            // if this item is unchecked - set unchecked state (notice the minus)
            view.getBackground().setState(
                    new int[]{-android.R.attr.state_checked});
        }
        ChatBox chatBox = ChatBoxTable.getChatBox(cursor);

        int bookmarkIndex = cursor.getColumnIndexOrThrow(ChatBoxTable.NAME);
        bookmarkTv.setText(cursor.getString(bookmarkIndex));

        String alias = chatBox.getAlias();
        if(alias == null) {
            aliasTv.setVisibility(View.GONE);
        }else{
            aliasTv.setText("/" + alias);
            aliasTv.setVisibility(View.VISIBLE);
        }

        int unreadCount = chatBox.getUnreadCount();
        if (unreadCount > 0) {
            unreadCountTv.setText(String.valueOf(unreadCount));
            unreadCountTv.setVisibility(View.VISIBLE);
        } else {
            unreadCountTv.setVisibility(View.GONE);
        }
    }

    public HashSet<Integer> getSelectedBookmarkIds() {
        return mBookmarkIds;
    }

    public void clearSelectedBookmark() {
        mBookmarkIds.clear();
        notifyDataSetChanged();
    }
}
