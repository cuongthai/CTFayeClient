package com.chatwing.whitelabel.managers;

import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;

import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.fragments.NotificationFragment;
import com.chatwing.whitelabel.pojos.Message;
import com.squareup.otto.Bus;

/**
 * Created by steve on 20/05/2015.
 */
public class MusicModeManager extends CommunicationModeManager {
    public static final int DRAWER_GRAVITY_MUSIC_SOURCES = Gravity.LEFT;
    private final CharSequence mMusicTitle;

    public MusicModeManager(Bus bus,
                            Delegate delegate,
                            UserManager userManager,
                            CommunicationActivityManager communicationActivityManager) {
        super(bus, delegate, userManager, communicationActivityManager);
        mMusicTitle = getString(R.string.title_music_box);
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
                R.drawable.ic_drawer,
                R.string.message_drawer_open,
                R.string.message_drawer_close) {
            @Override
            public void onDrawerStateChanged(int newState) {
                if (newState == DrawerLayout.STATE_IDLE) {
                    CharSequence title;
                    if (drawerLayout.isDrawerOpen(DRAWER_GRAVITY_MUSIC_SOURCES)) {
                        title = mMusicTitle;
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
        return DRAWER_GRAVITY_MUSIC_SOURCES;
    }


}
