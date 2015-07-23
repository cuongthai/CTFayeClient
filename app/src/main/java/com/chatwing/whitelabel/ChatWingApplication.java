package com.chatwing.whitelabel;

import android.app.Application;
import android.view.ViewConfiguration;

import com.chatwing.whitelabel.activities.ExtendCommunicationActivity;
import com.chatwing.whitelabel.activities.LegacyLoginActivity;
import com.chatwing.whitelabel.activities.WalkthroughActivity;
import com.chatwing.whitelabel.modules.ChatWingModule;
import com.chatwing.whitelabel.utils.Utils;
import com.crashlytics.android.Crashlytics;
import com.flurry.android.FlurryAgent;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import dagger.ObjectGraph;
import io.fabric.sdk.android.Fabric;

/**
 * Created by cuongthai on 21/10/2014.
 */
public class ChatWingApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        /**
         * App IDS
         * b4b391d0-e9bf-11e4-871f-f1829c245e2e : Test app id
         * Seattle 270c8870-14bf-11e5-a3c3-b94d1997848d
         * Demo for Aaron 3181f6e0-2933-11e5-8bc8-6fdd6f0ea556
         *
         */
        //Currently support only one chatbox enter from client, it should be loaded from server
        ChatWing.initialize(this, "3181f6e0-2933-11e5-8bc8-6fdd6f0ea556", "", new String[]{"1873"}, isOfficialChatWingApp()
                ? WalkthroughActivity.class
                : LegacyLoginActivity.class);
        ChatWing.setIsDebugging(true);
        ChatWing.instance(this).setMainActivityClass(ExtendCommunicationActivity.class);
        ChatWing.instance(this).getChatwingGraph().plus(getModules().toArray());

        FlurryAgent.init(this, getString(R.string.flurry_api_key));
        workaroundOverflowMenuKey();

        // Build Custom Singleton, required by PkRSS
        Utils.buildSingleton(this);
    }

    private void workaroundOverflowMenuKey() {
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if(menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception ex) {
            // Ignore
        }
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
