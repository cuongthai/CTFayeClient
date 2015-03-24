package com.chatwing.whitelabel;

import android.app.Application;

import com.chatwing.whitelabel.activities.ExtendCommunicationActivity;
import com.chatwing.whitelabel.activities.LegacyLoginActivity;
import com.chatwing.whitelabel.activities.WalkthroughActivity;
import com.chatwingsdk.ChatWing;
import com.chatwingsdk.modules.ChatWingModule;
import com.crashlytics.android.Crashlytics;

import java.util.Arrays;
import java.util.List;

import dagger.ObjectGraph;

/**
 * Created by cuongthai on 21/10/2014.
 */
public class ChatWingApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Crashlytics.start(this);

        //Currently support only one chatbox enter from client, it should be loaded from server
        ChatWing.initialize(this, "android", "", new String[]{"1873"}, isOfficialChatWingApp()
                ? WalkthroughActivity.class
                : LegacyLoginActivity.class);
        ChatWing.setIsDebugging(false);
        ChatWing.instance(this).setMainActivityClass(ExtendCommunicationActivity.class);
        ChatWing.instance(this).getChatwingGraph().plus(getModules().toArray());
    }

    /**
     * A list of modules to use for the application graph. Subclasses can override this method to
     * provide additional modules provided they call {@code super.getModules()}.
     */
    protected List<Object> getModules() {
        return Arrays.<Object>asList(new ChatWingModule(this));
    }

    public ObjectGraph getApplicationGraph() {
        return ChatWing.instance(this).getChatwingGraph();
    }

    private boolean isOfficialChatWingApp() {
        return getResources().getBoolean(R.bool.official);
    }
}
