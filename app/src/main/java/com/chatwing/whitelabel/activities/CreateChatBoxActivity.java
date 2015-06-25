package com.chatwing.whitelabel.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.events.TaskFinishedEvent;
import com.chatwing.whitelabel.managers.ProgressViewsManager;
import com.chatwing.whitelabel.managers.UserManager;
import com.chatwing.whitelabel.modules.CreateChatBoxActivityModule;
import com.chatwing.whitelabel.modules.ExtendChatWingModule;
import com.chatwing.whitelabel.pojos.LightWeightChatBox;
import com.chatwing.whitelabel.pojos.responses.CreateChatBoxResponse;
import com.chatwing.whitelabel.tasks.CreateChatBoxTask;
import com.chatwing.whitelabel.views.ErrorMessageView;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Activity which allows user to create a new chat box.
 */
public class CreateChatBoxActivity extends BaseABFragmentActivity {
    public static final String EXTRA_RESULT_CHAT_BOX = "chat_box";
    public static final String EXTRA_RESULT_EXCEPTION = "exception";

    @Inject
    Bus mBus;
    @Inject
    UserManager mUserManager;
    @Inject
    Provider<CreateChatBoxTask> mCreateChatBoxProvider;
    @Inject
    ProgressViewsManager mProgressViewsManager;
    @Inject
    ErrorMessageView mErrorMessageView;
    private CreateChatBoxTask mCurrentTask = null;
    // UI references.
    private EditText mNameTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_create_chat_box);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        if (!mUserManager.userCanCreateChatBox()) {
            mErrorMessageView.show(R.string.error_required_chat_wing_login_to_create_chat_boxes);
            finish();
            return;
        }

        mNameTextView = (EditText) findViewById(R.id.name);

        mProgressViewsManager.setViews(
                findViewById(R.id.create_chat_box_status),
                (TextView) findViewById(R.id.create_chat_box_status_message),
                findViewById(R.id.create_chat_box_form)
        );

        findViewById(R.id.create_chat_box_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptCreate();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBus.register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBus.unregister(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopCurrentTask();
    }

    @Override
    protected List<Object> getModules() {
        return Arrays.<Object>asList(new ExtendChatWingModule(this), new CreateChatBoxActivityModule(this));
    }

    private void stopCurrentTask() {
        if (mCurrentTask != null) {
            if (mCurrentTask.getStatus() != AsyncTask.Status.FINISHED) {
                mCurrentTask.cancel(true);
            }
            mCurrentTask = null;
        }
    }

    public void attemptCreate() {
        if (mCurrentTask != null) {
            return;
        }

        // Reset errors.
        mNameTextView.setError(null);

        String name = mNameTextView.getText().toString();

        mProgressViewsManager.showProgress(true, R.string.progress_creating_chat_box);
        mCurrentTask = mCreateChatBoxProvider.get();
        mCurrentTask.setName(name);
        mCurrentTask.execute();
    }

    @Subscribe
    public void onTaskFinished(TaskFinishedEvent event) {
        if (event.getTask() != mCurrentTask) {
            return;
        }

        mProgressViewsManager.showProgress(false);
        mCurrentTask = null;
        Intent intent = new Intent();
        if (event.getStatus() == TaskFinishedEvent.Status.FAILED) {
            Exception exception = event.getException();
            intent.putExtra(EXTRA_RESULT_EXCEPTION, exception);
            setResult(RESULT_EXCEPTION, intent);
            finish();
        } else {
            CreateChatBoxResponse response = (CreateChatBoxResponse) event.getResult();
            LightWeightChatBox chatBox = response.getData();
            intent.putExtra(EXTRA_RESULT_CHAT_BOX, chatBox);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
