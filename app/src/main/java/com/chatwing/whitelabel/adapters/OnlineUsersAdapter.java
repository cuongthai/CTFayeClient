package com.chatwing.whitelabel.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.chatwing.whitelabel.Constants;
import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.managers.ApiManager;
import com.chatwing.whitelabel.managers.VolleyManager;
import com.chatwing.whitelabel.modules.ForActivity;
import com.chatwing.whitelabel.pojos.OnlineUser;
import com.chatwing.whitelabel.utils.StringUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;

import java.util.List;

import javax.inject.Inject;

/**
 * Author: Huy Nguyen
 * Date: 7/25/13
 * Time: 2:39 PM
 */
public class OnlineUsersAdapter extends CompatArrayAdapter<OnlineUser> {

    private LayoutInflater mInflater;
    private ApiManager mApiManager;
    private VolleyManager mVolleyManager;
    private List<String> mNameFilters;
    private int mUsernameTextColor;
    private final DisplayImageOptions displayImageOptions;


    @Inject
    OnlineUsersAdapter(@ForActivity Context context,
                       @ForActivity LayoutInflater layoutInflater,
                       ApiManager apiManager,
                       VolleyManager volleyManager) {
        super(context, 0);
        mInflater = layoutInflater;
        mApiManager = apiManager;
        mVolleyManager = volleyManager;
        displayImageOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.default_avatar)
                .showImageForEmptyUri(R.drawable.default_avatar)
                .showImageOnFail(R.drawable.default_avatar)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .displayer(new SimpleBitmapDisplayer())
                .build();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_online_user, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.loginType
                    = (ImageView) convertView.findViewById(R.id.login_type);
            viewHolder.avatar
                    = (ImageView) convertView.findViewById(R.id.image_view_avatar);
            viewHolder.username
                    = (TextView) convertView.findViewById(R.id.username);

            viewHolder.username.setTextColor(mUsernameTextColor);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        OnlineUser user = getItem(position);

        String username = user.getName();
        if (mNameFilters != null && mNameFilters.size() > 0) {
            username = StringUtils.applyFilters(
                    username,
                    mNameFilters,
                    Constants.FILTER_REPLACE_SEQUENCE);
        }
        viewHolder.username.setText(username);

        int resId = mApiManager.getLoginTypeImageResId(user.getLoginType());
        viewHolder.loginType.setImageResource(resId);

        String avatarUrl = mApiManager.getAvatarUrl(user);
        ImageLoader.getInstance().displayImage(avatarUrl, viewHolder.avatar, displayImageOptions);
        return convertView;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    public void removeNameFilters() {
        mNameFilters = null;
        notifyDataSetChanged();
    }

    public void update(List<String> nameFilters, int usernameTextColor) {
        mNameFilters = nameFilters;
        mUsernameTextColor = usernameTextColor;
        notifyDataSetChanged();
    }

    private static class ViewHolder {
        private ImageView loginType;
        private ImageView avatar;
        private TextView username;
    }
}
