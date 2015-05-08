package com.chatwing.whitelabel.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.managers.BuildManager;
import com.chatwing.whitelabel.modules.StartActivityModule;
import com.chatwingsdk.activities.BaseABFragmentActivity;
import com.chatwingsdk.managers.UserManager;
import com.chatwingsdk.pojos.params.CreateConversationParams;
import com.chatwingsdk.services.CreateConversationIntentService;
import com.chatwingsdk.utils.LogUtils;

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
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_AUTHENTICATION) {
            if (resultCode == RESULT_OK) {
                LogUtils.v("Populate user debug: populate");

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
