package com.chatwing.whitelabel.managers;

import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;

import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.events.UserSelectedFeedSource;
import com.chatwingsdk.fragments.NotificationFragment;
import com.chatwingsdk.managers.CommunicationActivityManager;
import com.chatwingsdk.managers.CommunicationModeManager;
import com.chatwingsdk.managers.UserManager;
import com.chatwingsdk.pojos.Message;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

/**
 * Created by steve on 20/05/2015.
 */
public class FeedModeManager extends CommunicationModeManager {
    public static final int DRAWER_GRAVITY_FEED_SOURCES = Gravity.LEFT;
    private final CharSequence mFeedTitle;

    public FeedModeManager(Bus bus,
                           Delegate delegate,
                           UserManager userManager,
                           CommunicationActivityManager communicationActivityManager) {
        super(bus, delegate, userManager, communicationActivityManager);
        mFeedTitle = getString(R.string.title_feeds);

    }

    @Override
    public void logout() {

    }

    @Override
    public void reloadCurrentBox() {

    }

    @Override
    public ActionBarDrawerToggle getDrawerToggleListener() {
        final ActionBarActivity activity = mActivityDelegate.getActivity();
        final DrawerLayout drawerLayout = mActivityDelegate.getDrawerLayout();
        return new ActionBarDrawerToggle(activity,
                drawerLayout,
                com.chatwingsdk.R.drawable.ic_drawer,
                com.chatwingsdk.R.string.message_drawer_open,
                com.chatwingsdk.R.string.message_drawer_close) {
            @Override
            public void onDrawerStateChanged(int newState) {
                if (newState == DrawerLayout.STATE_IDLE) {
                    CharSequence title;
                    if (drawerLayout.isDrawerOpen(DRAWER_GRAVITY_FEED_SOURCES)) {
                        title = mFeedTitle;
                    } else {
                        title = mTitle;
                    }
                    activity.getSupportActionBar().setTitle(title);
                    invalidateOptionsMenu();
                }
            }
        };
    }

    @Override
    public int getResourceStringNoCommunicationBox() {
        return R.string.message_select_feed_source;
    }

    @Override
    public boolean isSecondaryDrawerOpening() {
        return false;
    }

    @Override
    public boolean isInCurrentCommunicationBox(Message message) {
        return false;
    }

    @Override
    public void processMessageInCurrentCommunicationBox(Message message) {

    }

    @Override
    public void onPostResume() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    protected NotificationFragment getNotificationSettingFragment() {
        return null;
    }

    @Override
    protected int getCommunicationBoxDrawerGravity() {
        return DRAWER_GRAVITY_FEED_SOURCES;
    }

    @Subscribe
    public void onUserSelectedFeedSource(UserSelectedFeedSource event){
        mActivityDelegate.getDrawerLayout().closeDrawers();
    }
}
