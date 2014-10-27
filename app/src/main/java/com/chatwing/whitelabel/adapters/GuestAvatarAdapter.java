package com.chatwing.whitelabel.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.android.volley.toolbox.NetworkImageView;
import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.managers.ApiManager;
import com.chatwingsdk.managers.VolleyManager;
import com.chatwingsdk.modules.ForActivity;

import javax.inject.Inject;

/**
 * Created by steve on 02/07/2014.
 */
public class GuestAvatarAdapter extends BaseAdapter {

    private final String[] mAvatars;
    private final LayoutInflater mInflater;
    private final VolleyManager mVolleyManager;

    @Inject
    GuestAvatarAdapter(@ForActivity LayoutInflater layoutInflater,
                       VolleyManager volleyManager,
                       String[] avatars) {
        mAvatars = avatars;
        mInflater = layoutInflater;
        mVolleyManager = volleyManager;
    }

    @Override
    public int getCount() {
        return mAvatars.length;
    }

    @Override
    public String getItem(int i) {
        return String.format(ApiManager.GUEST_AVATAR_URL, mAvatars[i]);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_guest_avatar, viewGroup, false);
            viewHolder = new ViewHolder();
            viewHolder.avatar = (NetworkImageView) convertView.findViewById(R.id.avatar);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.avatar.setDefaultImageResId(R.drawable.ic_action_group_light);
        viewHolder.avatar.setErrorImageResId(R.drawable.ic_action_group_light);
        viewHolder.avatar.setImageUrl(getItem(i), mVolleyManager.getImageLoader());

        return convertView;
    }

    public String getAvatarAt(int position) {
        return mAvatars[position];
    }

    private class ViewHolder {
        NetworkImageView avatar;
    }
}
