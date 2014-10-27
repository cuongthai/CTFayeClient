package com.chatwing.whitelabel;

import android.app.Application;

import com.chatwingsdk.ChatWing;
import com.chatwingsdk.modules.ChatWingModule;

import java.util.Arrays;
import java.util.List;

import dagger.ObjectGraph;

/**
 * Created by cuongthai on 21/10/2014.
 */
public class ChatWingApplication extends Application {
    private ObjectGraph applicationGraph;

    @Override
    public void onCreate() {
        super.onCreate();
        applicationGraph = ObjectGraph.create(getModules().toArray());
        ChatWing.initialize(this, "android", "", new String[]{"1873"}, LegacyLoginActivity.class);
    }

    /**
     * A list of modules to use for the application graph. Subclasses can override this method to
     * provide additional modules provided they call {@code super.getModules()}.
     */
    protected List<Object> getModules() {
        return Arrays.<Object>asList(new ChatWingModule(this));
    }

    public ObjectGraph getApplicationGraph() {
        return applicationGraph;
    }
}
