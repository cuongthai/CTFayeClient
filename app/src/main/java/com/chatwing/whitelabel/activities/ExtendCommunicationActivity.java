package com.chatwing.whitelabel.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.fragments.ExtendCommunicationDrawerFragment;
import com.chatwing.whitelabel.fragments.OnlineUsersFragment;
import com.chatwing.whitelabel.managers.ExtendChatBoxModeManager;
import com.chatwing.whitelabel.modules.ExtendCommunicationActivityModule;
import com.chatwingsdk.activities.CommunicationActivity;
import com.chatwingsdk.modules.CommunicationActivityModule;
import com.chatwingsdk.utils.LogUtils;
import com.squareup.otto.Subscribe;

import java.util.Arrays;
import java.util.List;

/**
 * Created by steve on 10/12/2014.
 */
public class ExtendCommunicationActivity extends CommunicationActivity implements ExtendCommunicationDrawerFragment.Listener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String onlineFragmentTag = getString(R.string.fragment_tag_online_user);
        if (getSupportFragmentManager().findFragmentByTag(onlineFragmentTag) == null) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.right_drawer_container, new OnlineUsersFragment(), onlineFragmentTag);
            fragmentTransaction.commit();
        }
    }

    @Override
    protected Class<? extends CommunicationActivity> getEntranceActivityClass() {
        return ExtendCommunicationActivity.class;
    }

    @Override
    protected List<Object> getModules() {
        return Arrays.<Object>asList(new CommunicationActivityModule(this),
                new ExtendCommunicationActivityModule(this));
    }

    @Subscribe
    public void onAllSyncsCompleted(com.chatwingsdk.events.internal.AllSyncsCompletedEvent event) {
        super.onAllSyncsCompleted(event);
    }

    @Subscribe
    public void onTouchUserInfoEvent(com.chatwingsdk.events.internal.TouchUserInfoEvent event) {
        super.onTouchUserInfoEvent(event);
    }

    @com.squareup.otto.Subscribe
    public void onServerConnectionChangedEvent(com.chatwingsdk.events.faye.ServerConnectionChangedEvent event) {
        super.onServerConnectionChangedEvent(event);
    }

    @com.squareup.otto.Subscribe
    public void onSyncCommunicationBoxEvent(com.chatwingsdk.events.internal.SyncCommunicationBoxEvent event) {
        super.onSyncCommunicationBoxEvent(event);
    }


    @Subscribe
    public void onChannelSubscriptionChanged(com.chatwingsdk.events.faye.ChannelSubscriptionChangedEvent event) {
        super.onChannelSubscriptionChanged(event);
    }

    @Subscribe
    public void onFayePublished(com.chatwingsdk.events.faye.FayePublishEvent event) {
        super.onFayePublished(event);
    }

    @Subscribe
    public void onMessageReceived(com.chatwingsdk.events.faye.MessageReceivedEvent event) {
        super.onMessageReceived(event);
    }


    @Override
    public void onBackPressed() {
        if (mCurrentCommunicationMode.isSecondaryDrawerOpening()) {
            ((ExtendChatBoxModeManager) mCurrentCommunicationMode).closeSecondaryDrawer();
        } else if (!mCurrentCommunicationMode.isCommunicationBoxDrawerOpening()) {
            // Both online users and chat boxes/conversation lists are closed.
            // Open chat boxes/conversation list now.
            mCurrentCommunicationMode.openCommunicationBoxDrawer();
        } else {
            // Online users list is closed, chat boxes list is opened.
            // User probably is trying to quit the app.
            FragmentManager fragmentManager = getSupportFragmentManager();
            int stackSize = fragmentManager.getBackStackEntryCount();
            if (stackSize == 0) {
                finish();
            } else {
                String fragmentTag = fragmentManager.getBackStackEntryAt(stackSize - 1).getName();
                fragmentManager.popBackStack(fragmentTag, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        }
    }

    @Override
    public void showSettings() {
        Intent i = new Intent(this, MainPreferenceActivity.class);
        startActivity(i);
    }
}
