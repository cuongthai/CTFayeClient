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

package com.chatwingsdk.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chatwingsdk.R;
import com.chatwingsdk.pojos.Conversation;
import com.chatwingsdk.pojos.User;
import com.chatwingsdk.tables.ConversationTable;
import com.google.gson.Gson;

/**
 * Created by cuongthai on 4/4/14.
 */
public class NotificationsAdapter extends CursorAdapter {
    private final User mCurrentUser;

    public NotificationsAdapter(Context context, User currentUser, Cursor c, int flags) {
        super(context, c, flags);
        mCurrentUser = currentUser;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater from = LayoutInflater.from(context);
        View root = from.inflate(R.layout.item_simple_text_with_unread_count, parent, false);
        root.findViewById(android.R.id.text2).setVisibility(View.GONE);
        return root;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        int dataIndexColumn = cursor.getColumnIndex(ConversationTable.DATA);
        Conversation conversation = new Gson().fromJson(cursor.getString(dataIndexColumn), Conversation.class);

        ((TextView) view.findViewById(android.R.id.text1))
                .setText(conversation.getConversationAlias(mCurrentUser.getId()));

        int countIndexColumn = cursor.getColumnIndex(ConversationTable.UNREAD_COUNT);
        int count = cursor.getInt(countIndexColumn);
        TextView countTv = (TextView) view.findViewById(R.id.unread_count);
        if (count == 0) {
            countTv.setVisibility(View.GONE);
        } else {
            countTv.setText(String.valueOf(count));
            countTv.setVisibility(View.VISIBLE);
        }
    }
}
