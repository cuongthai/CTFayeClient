package com.chatwing.whitelabel.services;

import android.content.Intent;
import android.graphics.Bitmap;

import com.chatwing.whitelabel.managers.ApiManager;
import com.chatwing.whitelabel.modules.ExtendChatWingModule;
import com.chatwingsdk.events.internal.UpdateUserEvent;
import com.chatwingsdk.managers.VolleyManager;
import com.chatwingsdk.modules.ChatWingModule;
import com.chatwingsdk.pojos.User;
import com.chatwingsdk.services.BaseIntentService;
import com.chatwingsdk.utils.BitmapUtils;
import com.chatwingsdk.utils.LogUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

public class UpdateAvatarIntentService extends ExtendBaseIntentService {
    private static final Object sLock = new Object();
    public static final String EXTRA_AVATAR_PATH = "bitmap";
    private static boolean sIsInProgress;
    @Inject
    VolleyManager mVolleyManager;

    @Inject
    ApiManager mApiManager;

    public UpdateAvatarIntentService() {
        super("UpdateAvatarIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        User currentUser = mUserManager.getCurrentUser();
        if (currentUser == null) {
            return;
        }
        setIsInProgress(true);
        LogUtils.v("Update avatar service");
        String path = intent.getStringExtra(EXTRA_AVATAR_PATH);
        try {
            post(UpdateUserEvent.started());
            //Make sure avatar is set in right direction
            fixImageExif(path);
            mApiManager.updateAvatar(currentUser, path);
            String permanentAvatarUrl = mApiManager.getAvatarUrl(currentUser);
            mVolleyManager.mQueue.getCache().remove(permanentAvatarUrl);
            post(UpdateUserEvent.success());
        } catch (Exception e) {
            LogUtils.e(e);
            post(new UpdateUserEvent(e));
        }
        setIsInProgress(false);
    }

    private void fixImageExif(String path) throws IOException {
        Bitmap bitmap = BitmapUtils.fixImageEXIF(path);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    private void post(final UpdateUserEvent event) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mBus.post(event);
            }
        });
    }

    private static void setIsInProgress(boolean isInProgress) {
        synchronized (sLock) {
            sIsInProgress = isInProgress;
        }
    }

    public static boolean isInProgress() {
        synchronized (sLock) {
            return sIsInProgress;
        }
    }

}
