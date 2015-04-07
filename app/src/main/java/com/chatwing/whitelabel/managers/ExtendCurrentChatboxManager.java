package com.chatwing.whitelabel.managers;

import android.content.Context;
import android.os.AsyncTask;

import com.chatwing.whitelabel.events.LoadOnlineUsersFailedEvent;
import com.chatwing.whitelabel.events.LoadOnlineUsersSuccessEvent;
import com.chatwing.whitelabel.pojos.responses.LoadOnlineUsersResponse;
import com.chatwing.whitelabel.tasks.LoadOnlineUsersTask;
import com.chatwingsdk.events.internal.ResumeOpenChatBoxEvent;
import com.chatwingsdk.events.internal.TaskFinishedEvent;
import com.chatwingsdk.managers.CurrentChatBoxManager;
import com.chatwingsdk.managers.PasswordManager;
import com.chatwingsdk.validators.ChatBoxIdValidator;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import javax.inject.Provider;

/**
 * Created by steve on 11/12/2014.
 */
public class ExtendCurrentChatboxManager extends CurrentChatBoxManager {
    private Provider<LoadOnlineUsersTask> mOnlineUsersTaskProvider;
    private LoadOnlineUsersTask mCurrentOnlineUsersTask;

    public ExtendCurrentChatboxManager(Context context,
                                       Bus bus,
                                       ChatBoxIdValidator chatBoxIdValidator,
                                       Provider<LoadOnlineUsersTask> onlineUsersTaskProvider,
                                       PasswordManager passwordManager) {
        super(context, bus, chatBoxIdValidator, passwordManager);
        mOnlineUsersTaskProvider = onlineUsersTaskProvider;

    }

    @com.squareup.otto.Subscribe
    public void onLoadChatBoxDetailsEvent(com.chatwingsdk.events.internal.LoadChatBoxDetailsEvent event) {
        super.onLoadChatBoxDetailsEvent(event);
    }

    @Subscribe
    public void onResumeSetCurrentChatbox(ResumeOpenChatBoxEvent chatboxEvent){
        super.onResumeSetCurrentChatbox(chatboxEvent);
    }


    public void loadOnlineUsers() {
        if (mCurrentChatBox == null) {
            return;
        }
        if (mCurrentOnlineUsersTask != null
                && mCurrentOnlineUsersTask.getStatus() != AsyncTask.Status.FINISHED) {
            // There is a running task, no need to start a new one.
            return;
        }
        mCurrentOnlineUsersTask = mOnlineUsersTaskProvider.get();
        mCurrentOnlineUsersTask.execute(mCurrentChatBox.getId());
    }

    @Subscribe
    public void onTaskFinishedEvent(TaskFinishedEvent event) {
        if (event.getTask() != mCurrentOnlineUsersTask) {
            return;
        }
        Exception exception = event.getException();
        if (exception != null) {
            mBus.post(new LoadOnlineUsersFailedEvent(exception));
        } else {
            LoadOnlineUsersResponse response = (LoadOnlineUsersResponse) event.getResult();
            LoadOnlineUsersResponse.Data data = response.getData();
            mBus.post(new LoadOnlineUsersSuccessEvent(data.getCount(), data.getList()));
        }
    }

    @Override
    public void stopBackgroundTasks() {
        if (mCurrentOnlineUsersTask != null) {
            if (mCurrentOnlineUsersTask.getStatus() != AsyncTask.Status.FINISHED) {
                mCurrentOnlineUsersTask.cancel(true);
            }
            mCurrentOnlineUsersTask = null;
        }
    }
}
