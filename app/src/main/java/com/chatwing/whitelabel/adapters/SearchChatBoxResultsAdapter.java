package com.chatwing.whitelabel.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.managers.ApiManager;
import com.chatwing.whitelabel.modules.ForActivity;
import com.chatwing.whitelabel.pojos.LightWeightChatBox;

import javax.inject.Inject;

/**
 * Created by nguyenthanhhuy on 12/21/13.
 */
public class SearchChatBoxResultsAdapter extends CompatArrayAdapter<LightWeightChatBox> {

    private LayoutInflater mLayoutInflater;
    private ApiManager mApiManager;

    @Inject
    SearchChatBoxResultsAdapter(@ForActivity Context context,
                                @ForActivity LayoutInflater layoutInflater,
                                ApiManager apiManager) {
        super(context, 0);
        mLayoutInflater = layoutInflater;
        mApiManager = apiManager;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.item_title_subtitle_with_unread_count, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.mainText = (TextView) convertView.findViewById(android.R.id.text1);
            viewHolder.subText = (TextView) convertView.findViewById(android.R.id.summary);
            convertView.findViewById(android.R.id.text2).setVisibility(View.GONE);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        LightWeightChatBox chatBox = getItem(position);
        viewHolder.mainText.setText(chatBox.getName());
        viewHolder.subText.setText(mApiManager.getFullChatBoxAliasUrl(chatBox.getAlias()));
        return convertView;
    }

    private static class ViewHolder {
        TextView mainText;
        TextView subText;
    }
}
