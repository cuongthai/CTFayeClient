package com.chatwing.whitelabel.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.pojos.responses.LoadModeratorsResponse;
import com.chatwing.whitelabel.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by steve on 22/07/2015.
 */
public class UsersListAdapter extends BaseAdapter {
    private final Context context;
    private List<LoadModeratorsResponse.Moderator> moderators = new ArrayList<LoadModeratorsResponse.Moderator>();

    public UsersListAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return moderators.size();
    }

    @Override
    public LoadModeratorsResponse.Moderator getItem(int position) {
        return moderators.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final LoadModeratorsResponse.Moderator moderator = moderators.get(position);
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.item_simple_text_with_unread_count, parent, false);

            holder = new ViewHolder();
            holder.usernameTv = (TextView) convertView.findViewById(android.R.id.text1);
            holder.unreadCountTv = (TextView) convertView.findViewById(android.R.id.text2);

            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();

        LogUtils.v("Moderators "+moderator.getIdentifier());

        holder.usernameTv.setText(moderator.getName());
        return convertView;
    }

    public void setUsers(List<LoadModeratorsResponse.Moderator> moderators) {
        LogUtils.v("Moderators "+moderators.size());
        this.moderators.clear();
        this.moderators.addAll(moderators);
        notifyDataSetChanged();
    }

    public void clear() {
        this.moderators.clear();
        notifyDataSetChanged();
    }

    private class ViewHolder {
        public TextView usernameTv;
        public TextView unreadCountTv;
    }
}
