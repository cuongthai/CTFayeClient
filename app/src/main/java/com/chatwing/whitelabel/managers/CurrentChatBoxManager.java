/*
 * Copyright (C) 2014 ChatWing
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chatwing.whitelabel.managers;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.chatwing.whitelabel.events.CurrentChatBoxEvent;
import com.chatwing.whitelabel.events.LoadChatBoxDetailsEvent;
import com.chatwing.whitelabel.events.LoadCurrentChatBoxFailedEvent;
import com.chatwing.whitelabel.events.LoadOnlineUsersFailedEvent;
import com.chatwing.whitelabel.events.LoadOnlineUsersSuccessEvent;
import com.chatwing.whitelabel.events.RequestOpenChatBoxEvent;
import com.chatwing.whitelabel.events.ResumeOpenChatBoxEvent;
import com.chatwing.whitelabel.events.TaskFinishedEvent;
import com.chatwing.whitelabel.pojos.ChatBox;
import com.chatwing.whitelabel.pojos.responses.ChatBoxDetailsResponse;
import com.chatwing.whitelabel.pojos.responses.LoadOnlineUsersResponse;
import com.chatwing.whitelabel.services.AckChatboxIntentService;
import com.chatwing.whitelabel.services.LoadChatBoxDetailsService;
import com.chatwing.whitelabel.tasks.LoadOnlineUsersTask;
import com.chatwing.whitelabel.utils.LogUtils;
import com.chatwing.whitelabel.validators.ChatBoxIdValidator;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import javax.inject.Provider;

/**
 * Created by cuongthai on 21/07/2014.
 */
public class CurrentChatBoxManager extends CurrentCommunicationManager {
    private final ChatBoxIdValidator mChatBoxIdValidator;

    protected ChatBox mCurrentChatBox;
    protected ChatBox mLastKnownGoodChatBox;

    private PasswordManager mPasswordManager;
    private Provider<LoadOnlineUsersTask> mOnlineUsersTaskProvider;
    private LoadOnlineUsersTask mCurrentOnlineUsersTask;

    public CurrentChatBoxManager(Context context,
                                 Bus bus,
                                 ChatBoxIdValidator chatBoxIdValidator,
                                 Provider<LoadOnlineUsersTask> onlineUsersTaskProvider,
                                 PasswordManager passwordManager) {
        super(context, bus);
        mOnlineUsersTaskProvider = onlineUsersTaskProvider;
        mChatBoxIdValidator = chatBoxIdValidator;
        mPasswordManager = passwordManager;
    }

    @Override
    public void onDestroy() {
        mCurrentChatBox = null;
        mLastKnownGoodChatBox = null;
    }

    public void loadCurrentChatBox(final int chatBoxId) {
        if (!mChatBoxIdValidator.isValid(chatBoxId)) {
            return;
        }

        removeCurrentChatbox();
        mBus.post(new CurrentChatBoxEvent(CurrentChatBoxEvent.Status.LOADING, null));

        loadChatboxDetail(chatBoxId);
    }

    @Subscribe
    public void onResumeSetCurrentChatbox(ResumeOpenChatBoxEvent chatboxEvent){
        setCurrentChatBox(chatboxEvent.getChatBox(), true);
    }

    @Subscribe
    public void onLoadChatBoxDetailsEvent(LoadChatBoxDetailsEvent event) {
        Exception exc = event.getException();
        if (exc != null) {
            mBus.post(new LoadCurrentChatBoxFailedEvent(exc));
        } else {
            ChatBoxDetailsResponse response = event.getResponse();
            ChatBox newChatBox = response.getData();
            setCurrentChatBox(newChatBox, false);
        }
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
            if (data == null) {
                //Monitor this
                LogUtils.e("Hm... Why no data? " + response.getError());
                return;
            }
            mBus.post(new LoadOnlineUsersSuccessEvent(data.getCount(), data.getList()));
        }
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

    public ChatBox getCurrentChatBox() {
        return mCurrentChatBox;
    }

    public ChatBox getLastKnownGoodChatBox() {
        return mCurrentChatBox == null ? mLastKnownGoodChatBox : mCurrentChatBox;
    }

    public void resetLastKnownChatBox() {
        mLastKnownGoodChatBox = null;
    }

    private void setCurrentChatBox(ChatBox newChatBox, boolean confirmedPassword) {
        if (newChatBox == null) {
            return;
        }

        /**
         * At this stage, we have loaded chatbox. We tell UI to show password prompt
         */
        boolean isRememberedPassword = mPasswordManager.isRememberPassword(newChatBox.getKey());
        if (newChatBox.hasPassword() && !isRememberedPassword && !confirmedPassword) {
            mBus.post(new RequestOpenChatBoxEvent(newChatBox));
        }

        CurrentChatBoxEvent.Status status =
                mCurrentChatBox != null && mCurrentChatBox.equals(newChatBox)
                        ? CurrentChatBoxEvent.Status.UPDATED
                        : CurrentChatBoxEvent.Status.LOADED;
        mCurrentChatBox = newChatBox;
        mBus.post(new CurrentChatBoxEvent(status, mCurrentChatBox));
    }

    private void removeCurrentChatbox() {
        if (mCurrentChatBox != null) {
            AckChatboxIntentService.ack(mContext, mCurrentChatBox.getId());
        }
        mCurrentChatBox = null;
        mBus.post(new CurrentChatBoxEvent(CurrentChatBoxEvent.Status.REMOVED, null));
    }

    private void loadChatboxDetail(int chatboxId) {
        Intent intent = new Intent(mContext, LoadChatBoxDetailsService.class);
        intent.putExtra(LoadChatBoxDetailsService.EXTRA_CHAT_BOX_ID, chatboxId);
        mContext.startService(intent);
    }


    public void removeCurrentChatBox() {
        if (mCurrentChatBox != null) {
            mLastKnownGoodChatBox = mCurrentChatBox;
            mCurrentChatBox = null;
        }
        stopBackgroundTasks();
        mBus.post(new CurrentChatBoxEvent(CurrentChatBoxEvent.Status.REMOVED, null));
    }

    private void stopBackgroundTasks() {
        if (mCurrentOnlineUsersTask != null) {
            if (mCurrentOnlineUsersTask.getStatus() != AsyncTask.Status.FINISHED) {
                mCurrentOnlineUsersTask.cancel(true);
            }
            mCurrentOnlineUsersTask = null;
        }
    }
}
