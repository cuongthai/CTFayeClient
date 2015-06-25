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

package com.chatwing.whitelabel.adapters;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.View;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.TextView;

import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.contentproviders.ChatWingContentProvider;
import com.chatwing.whitelabel.fragments.CategoriesFragment;
import com.chatwing.whitelabel.tables.CategoryTable;
import com.chatwing.whitelabel.tables.ChatBoxTable;


/**
 * Author: Huy Nguyen
 * Date: 5/30/13
 * Time: 5:31 PM
 */
public class CategoryCursorTreeAdapter extends SimpleCursorTreeAdapter {

    public static CategoryCursorTreeAdapter newInstance(Context context) {
        return new CategoryCursorTreeAdapter(
                context,
                null,
                R.layout.item_simple_text_with_unread_count_2,
                new String[]{CategoriesFragment.GROUP_NAME, CategoriesFragment.UNREAD_GROUP_COUNT_COLLUMN_NAME},
                new int[]{android.R.id.text1, R.id.unread_count},
                R.layout.item_simple_text_with_unread_count_2,
                new String[]{ChatBoxTable.NAME, ChatBoxTable.UNREAD_COUNT},
                new int[]{android.R.id.text1, R.id.unread_count}
        );
    }

    private Context mContext;

    public CategoryCursorTreeAdapter(Context context, Cursor cursor,
                                     int groupLayout, String[] groupFrom,
                                     int[] groupTo, int childLayout,
                                     String[] childFrom, int[] childTo) {
        super(context, cursor, groupLayout, groupFrom, groupTo, childLayout, childFrom, childTo);
        mContext = context;
        setViewBinder(new MyViewBinder());
    }

    @Override
    protected Cursor getChildrenCursor(Cursor groupCursor) {
        int titleIndex = groupCursor.getColumnIndex(CategoryTable.TITLE);
        String title = groupCursor.getString(titleIndex);
        Uri uri = ChatWingContentProvider.getCategoriesUri().buildUpon()
                .appendPath(title)
                .appendPath(ChatWingContentProvider.PATH_CHAT_BOXES)
                .build();
        return mContext.getContentResolver().query(
                uri,
                new String[]{ChatBoxTable._ID, ChatBoxTable.NAME, ChatBoxTable.ALIAS, ChatBoxTable.UNREAD_COUNT},
                null,
                null,
                null);
    }

    // A view binder that hide unread count view when the value is 0.
    private static class MyViewBinder implements ViewBinder {

        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            // Check whether we are binding unread count view or not
            if (view instanceof TextView) {
                if (cursor.getColumnName(columnIndex).equals(ChatBoxTable.UNREAD_COUNT) ||
                        cursor.getColumnName(columnIndex).equals(CategoriesFragment.UNREAD_GROUP_COUNT_COLLUMN_NAME)) {
                    int unreadCount = cursor.getInt(columnIndex);
                    TextView textView = (TextView) view;
                    if (unreadCount > 0) {
                        textView.setText(Integer.toString(unreadCount));
                        textView.setVisibility(View.VISIBLE);
                    } else {
                        textView.setVisibility(View.GONE);
                    }
                    return true;
                }
            }
            return false;
        }
    }
}