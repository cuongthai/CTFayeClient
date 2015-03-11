package com.chatwing.whitelabel.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.chatwingsdk.managers.UserManager;
import com.chatwingsdk.modules.ForActivity;
import com.chatwingsdk.pojos.User;

import javax.inject.Inject;

/**
 * Created by steve on 06/07/2014.
 */
public class AccountPickerAdapter extends CompatArrayAdapter<User> {
    private final LayoutInflater mInflater;
    private final UserManager mUserManager;

    @Inject
    AccountPickerAdapter(@ForActivity Context context,
                         @ForActivity LayoutInflater layoutInflater,
                         UserManager userManager) {
        super(context, 0);
        mInflater = layoutInflater;
        mUserManager = userManager;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(android.R.layout.simple_list_item_2, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.accountType
                    = (TextView) convertView.findViewById(android.R.id.text2);
            viewHolder.username
                    = (TextView) convertView.findViewById(android.R.id.text1);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        User user = getItem(position);

        String username = user.getName();
        viewHolder.username.setText(username);
        viewHolder.accountType.setText(user.getLoginType());

        if (user.equals(mUserManager.getCurrentUser())) {
            viewHolder.username.setTypeface(null, Typeface.BOLD);
            viewHolder.accountType.setTypeface(null, Typeface.BOLD);
        } else {
            viewHolder.username.setTypeface(null, Typeface.NORMAL);
            viewHolder.accountType.setTypeface(null, Typeface.NORMAL);
        }
        return convertView;
    }

    static class ViewHolder {
        public TextView username;
        public TextView accountType;
    }
}
