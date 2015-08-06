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
import com.chatwing.whitelabel.pojos.responses.LoadModeratorsResponse;
import com.chatwing.whitelabel.tables.DefaultUserTable;

/**
 * Created by steve on 22/07/2015.
 */
public class UsersListAdapter extends CursorAdapter {

    public UsersListAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater from = LayoutInflater.from(context);
        View root = from.inflate(R.layout.item_simple_text_with_unread_count, parent, false);
        root.findViewById(android.R.id.text2).setVisibility(View.GONE);
        root.findViewById(R.id.unread_count).setVisibility(View.GONE);
        return root;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        LoadModeratorsResponse.Moderator moderator = DefaultUserTable.getModerator(cursor);

        TextView aliasTv = (TextView) view.findViewById(android.R.id.text1);
        aliasTv.setText(moderator.getName());
        aliasTv.setTypeface(null, Typeface.ITALIC);
    }
}
