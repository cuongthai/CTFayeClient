package com.chatwing.whitelabel.services;

import android.content.Intent;

import com.chatwing.whitelabel.events.BlockedEvent;
import com.chatwing.whitelabel.fragments.ChatMessagesFragment;
import com.chatwing.whitelabel.managers.ApiManager;
import com.chatwing.whitelabel.pojos.Message;
import com.chatwing.whitelabel.pojos.responses.BlackListResponse;

import javax.inject.Inject;

/**
 * Created by steve on 30/06/2014.
 */
public class BlockUserIntentService extends BaseIntentService {
    public static final String EXTRA_MESSAGE = "message";
    public static final String EXTRA_BLOCK_TYPE = "block";
    public static final String EXTRA_CLEAR_MESSAGE = "clear_message";
    public static final String EXTRA_REASON = "reason";
    public static final String EXTRA_DURATION = "duration";
    @Inject
    protected ApiManager mApiManager;

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
        Message blockMessage = (Message) intent.getSerializableExtra(EXTRA_MESSAGE);

        boolean shouldRemoveMessage = intent.getBooleanExtra(EXTRA_CLEAR_MESSAGE, false);
        String blockReason = intent.getStringExtra(EXTRA_REASON);
        long blockDuration = intent.getLongExtra(EXTRA_DURATION, 0);

        ChatMessagesFragment.BLOCK blockType = (ChatMessagesFragment.BLOCK)
                intent.getSerializableExtra(EXTRA_BLOCK_TYPE);

        BlockedEvent event;
        try {
            BlackListResponse blackListResponse = mApiManager.blockUser(
                    mUserManager.getCurrentUser(),
                    blockType,
                    blockMessage,
                    shouldRemoveMessage,
                    blockReason,
                    blockDuration);
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
