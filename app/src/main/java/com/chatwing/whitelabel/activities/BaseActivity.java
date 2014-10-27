package com.chatwing.whitelabel.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.chatwing.whitelabel.ChatWingApplication;

import java.util.List;

import dagger.ObjectGraph;

/**
 * Created by cuongthai on 21/10/2014.
 */
public abstract class BaseActivity extends FragmentActivity {
    private ObjectGraph mObjectGraph;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Create the activity graph by .plus-ing our modules onto the application graph.
        ChatWingApplication application = (ChatWingApplication) getApplication();
        mObjectGraph = application.getApplicationGraph().plus(getModules().toArray());

        // Inject ourselves so subclasses will have dependencies fulfilled when this method returns.
        mObjectGraph.inject(this);

    }

    protected abstract List<Object> getModules();


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mObjectGraph = null;
    }

    /**
     * Inject the supplied {@code object} using the activity-specific graph.
     */
    public void inject(Object object) {
        mObjectGraph.inject(object);
    }
}
