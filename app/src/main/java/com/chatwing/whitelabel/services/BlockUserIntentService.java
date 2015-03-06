package com.chatwing.whitelabel.services;

import android.content.Intent;

import com.chatwing.whitelabel.events.BlockedEvent;
import com.chatwing.whitelabel.fragments.ExtendChatMessagesFragment;
import com.chatwing.whitelabel.managers.ApiManager;
import com.chatwing.whitelabel.modules.ExtendChatWingModule;
import com.chatwing.whitelabel.pojos.responses.BlackListResponse;
import com.chatwingsdk.modules.ChatWingModule;
import com.chatwingsdk.pojos.Message;
import com.chatwingsdk.services.BaseIntentService;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by steve on 30/06/2014.
 */
public class BlockUserIntentService extends ExtendBaseIntentService {
    public static final String EXTRA_MESSAGE = "message";
    public static final String EXTRA_BLOCK = "block";
    public static final String EXTRA_CLEAR_MESSAGE = "clear_message";
    public static final String EXTRA_REASON = "reason";
    public static final String EXTRA_DURATION = "duration";
    @Inject
    ApiManager mApiManager;

    public BlockUserIntentService() {
        super("BlockUserIntentService");
    }

    /**
     * throws
     * ApiManager.ApiException
     * ApiManager.UserUnauthenticatedException
     * ApiManager.ValidationException
     * ApiManager.InvalidAccessTokenException
     * ApiManager.RequiredAdminPermissionException
     *
     * @param intent
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        Message message = (Message) intent.getSerializableExtra(EXTRA_MESSAGE);
        boolean clearMessage = intent.getBooleanExtra(EXTRA_CLEAR_MESSAGE, false);
        String reason = intent.getStringExtra(EXTRA_REASON);
        long duration = intent.getLongExtra(EXTRA_DURATION, 0);
        ExtendChatMessagesFragment.BLOCK block = (ExtendChatMessagesFragment.BLOCK)
                intent.getSerializableExtra(EXTRA_BLOCK);
        BlockedEvent event;
        try {
            BlackListResponse blackListResponse = mApiManager.blockUser(mUserManager.getCurrentUser(),
                    block,
                    message,
                    clearMessage,
                    reason,
                    duration);
            event = new BlockedEvent(blackListResponse);
        } catch (Exception e) {
            event = new BlockedEvent(e);
        }

        post(event);
    }

    private void post(final BlockedEvent event) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mBus.post(event);
            }
        });
    }
}
