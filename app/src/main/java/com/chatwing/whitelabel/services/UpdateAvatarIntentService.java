package com.chatwing.whitelabel.services;

import android.content.Intent;
import android.graphics.Bitmap;

import com.chatwing.whitelabel.events.UpdateUserEvent;
import com.chatwing.whitelabel.managers.ApiManager;
import com.chatwing.whitelabel.managers.VolleyManager;
import com.chatwing.whitelabel.pojos.User;
import com.chatwing.whitelabel.utils.BitmapUtils;
import com.chatwing.whitelabel.utils.LogUtils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.utils.DiskCacheUtils;
import com.nostra13.universalimageloader.utils.MemoryCacheUtils;

import java.io.FileOutputStream;
import java.io.IOException;

import javax.inject.Inject;

public class UpdateAvatarIntentService extends BaseIntentService {
    public static final String EXTRA_AVATAR_PATH = "bitmap";
    private static final Object sLock = new Object();
    @Inject
    protected VolleyManager mVolleyManager;
    @Inject
    protected ApiManager mApiManager;

    private static boolean sIsInProgress;


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

        String path = intent.getStringExtra(EXTRA_AVATAR_PATH);
        try {
            post(UpdateUserEvent.started());
            //Make sure avatar is set in right direction
            fixImageExif(path);

            mApiManager.updateAvatar(currentUser, path);

            //Invalidate caches
            String permanentAvatarUrl = mApiManager.getAvatarUrl(currentUser);

            mVolleyManager.mQueue.getCache().remove(permanentAvatarUrl);
            DiskCacheUtils.removeFromCache(permanentAvatarUrl,
                    ImageLoader.getInstance().getDiskCache());
            MemoryCacheUtils.removeFromCache(permanentAvatarUrl,
                    ImageLoader.getInstance().getMemoryCache());

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
