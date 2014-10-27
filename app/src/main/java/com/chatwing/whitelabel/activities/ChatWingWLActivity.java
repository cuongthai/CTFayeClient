package com.chatwing.whitelabel.activities;

import com.chatwingsdk.activities.CommunicationActivity;

/**
 * Created by cuongthai on 26/10/2014.
 */
public class ChatWingWLActivity extends CommunicationActivity {
    @Override
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu();
    }
}
