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
import android.graphics.Typeface;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.pojos.Conversation;
import com.chatwing.whitelabel.pojos.User;
import com.chatwing.whitelabel.tables.ConversationTable;
import com.chatwing.whitelabel.utils.LogUtils;

/**
 * Created by cuongthai on 4/4/14.
 */
public class ConversationsAdapter extends CursorAdapter {
    private final User mCurrentUser;

    public ConversationsAdapter(Context context, User currentUser, Cursor c, int flags) {
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
        Conversation conversation = ConversationTable.getConversation(cursor);

        TextView aliasTv = (TextView) view.findViewById(android.R.id.text1);
        aliasTv.setText(conversation.getConversationAlias(mCurrentUser.getId()));

        int countIndexColumn = cursor.getColumnIndex(ConversationTable.UNREAD_COUNT);
        int count = cursor.getInt(countIndexColumn);
        TextView countTv = (TextView) view.findViewById(R.id.unread_count);
        if (count == 0) {
            countTv.setVisibility(View.GONE);
        } else {
            countTv.setText(String.valueOf(count));
            countTv.setVisibility(View.VISIBLE);
        }

        LogUtils.v("Conversation "+aliasTv.getText()+":"+conversation.isModerator());
        if(conversation.isModerator()){
            aliasTv.setTypeface(null, Typeface.ITALIC);
        }else{
            aliasTv.setTypeface(null, Typeface.NORMAL);
        }
    }
}
