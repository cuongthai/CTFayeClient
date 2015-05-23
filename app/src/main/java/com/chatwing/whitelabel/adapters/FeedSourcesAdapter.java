package com.chatwing.whitelabel.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chatwing.whitelabel.R;
import com.chatwingsdk.modules.ForActivity;
import com.pkmmte.pkrss.Category;


import javax.inject.Inject;

/**
 * Created by steve on 1/4/14.
 */
public class FeedSourcesAdapter extends CompatArrayAdapter<Category> {
    private LayoutInflater mInflater;

    @Inject
    FeedSourcesAdapter(@ForActivity Context context,
                       @ForActivity LayoutInflater layoutInflater) {
        super(context, 0);
        mInflater = layoutInflater;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    public Category getItem(int i) {
        return super.getItem(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_feed_source, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.categoryNameTv
                    = (TextView) convertView.findViewById(R.id.source_name);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Category category = getItem(position);
        viewHolder.categoryNameTv.setText(category.getName());

        return convertView;
    }

    private static class ViewHolder {
        private TextView categoryNameTv;
    }
}
