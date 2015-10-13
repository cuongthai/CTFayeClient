package com.chatwing.whitelabel.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;

import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.managers.BuildManager;
import com.chatwing.whitelabel.managers.UserManager;
import com.chatwing.whitelabel.modules.WhiteLabelCoverActivityModule;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by steve on 30/12/2014.
 */
public class WhiteLabelCoverActivity extends BaseABFragmentActivity {
    private static final int REQUEST_AUTHENTICATION = 1990;

    @Inject
    protected UserManager mUserManager;
    @Inject
    protected BuildManager mBuildManager;

    @Override
    protected List<Object> getModules() {
        List<Object> modules = new ArrayList<Object>();
        modules.add(new WhiteLabelCoverActivityModule(this));
        return modules;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_whitelabel_cover);

        //If user already signed in
        if (mUserManager.getCurrentUser() != null) {
            startActivity(new Intent(this, CommunicationActivity.class));
            finish();
            return;
        }

        findViewById(R.id.btn_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(
                        new Intent(WhiteLabelCoverActivity.this, LegacyLoginActivity.class),
                        REQUEST_AUTHENTICATION);
            }
        });

        View registerButton = findViewById(R.id.btn_register);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(
                        new Intent(WhiteLabelCoverActivity.this, RegisterActivity.class),
                        REQUEST_AUTHENTICATION);
            }
        });

        if (!mBuildManager.isSupportedRegister()) {
            registerButton.setVisibility(View.GONE);
            findViewById(R.id.separator).setVisibility(View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_AUTHENTICATION) {
            if (resultCode == RESULT_OK) {
                startActivity(new Intent(this, CommunicationActivity.class));
                finish();
                return;
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            // http://stackoverflow.com/questions/19275447/pressing-menu-button-causes-crash-in-activity-with-no-actionbar/19320065#19320065
            // do nothing
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
