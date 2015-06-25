package com.chatwing.whitelabel.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;

import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.managers.BuildManager;
import com.chatwing.whitelabel.managers.UserManager;
import com.chatwing.whitelabel.modules.StartActivityModule;
import com.chatwing.whitelabel.pojos.params.CreateConversationParams;
import com.chatwing.whitelabel.services.CreateConversationIntentService;
import com.chatwing.whitelabel.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by steve on 30/12/2014.
 */
public class StartActivity extends BaseABFragmentActivity {


    private static final int REQUEST_AUTHENTICATION = 1990;
    @Inject
    UserManager mUserManager;
    @Inject
    BuildManager mBuildManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        if (mUserManager.getCurrentUser() != null) {
            startActivity(new Intent(this, ExtendCommunicationActivity.class));
            finish();
            return;
        }
        findViewById(R.id.btn_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(StartActivity.this, LegacyLoginActivity.class), REQUEST_AUTHENTICATION);
            }
        });

        View registerButton = findViewById(R.id.btn_register);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(StartActivity.this, RegisterActivity.class), REQUEST_AUTHENTICATION);
            }
        });
        
        if(!mBuildManager.isSupportedRegister()){
            registerButton.setVisibility(View.GONE);
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ( keyCode == KeyEvent.KEYCODE_MENU ) {
            // http://stackoverflow.com/questions/19275447/pressing-menu-button-causes-crash-in-activity-with-no-actionbar/19320065#19320065
            // do nothing
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_AUTHENTICATION) {
            if (resultCode == RESULT_OK) {
                LogUtils.v("Populate user debug: populate");
                /***
                 * Admin lists
                 * Destiny nick0624@gmail.com 203624
                 */
                //Populate fake mods
                CreateConversationParams.SimpleUser simpleUser = new CreateConversationParams.SimpleUser("203624", "chatwing");
                Intent service = new Intent(this, CreateConversationIntentService.class);
                service.putExtra(CreateConversationIntentService.SILENT, true);
                service.putExtra(CreateConversationIntentService.EXTRA_USER, simpleUser);
                startService(service);

                startActivity(new Intent(this, ExtendCommunicationActivity.class));
                finish();
                return;
            }
        }
    }

    @Override
    protected List<Object> getModules() {
        List<Object> modules = new ArrayList<Object>();
        modules.add(new StartActivityModule(this));
        return modules;
    }
}
