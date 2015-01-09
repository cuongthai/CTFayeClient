package com.chatwing.whitelabel.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import com.chatwing.whitelabel.LegacyLoginActivity;
import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.modules.StartActivityModule;
import com.chatwingsdk.activities.BaseABFragmentActivity;
import com.chatwingsdk.managers.UserManager;

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

        findViewById(R.id.btn_register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(StartActivity.this, RegisterActivity.class), REQUEST_AUTHENTICATION);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_AUTHENTICATION) {
            if (resultCode == RESULT_OK) {
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
