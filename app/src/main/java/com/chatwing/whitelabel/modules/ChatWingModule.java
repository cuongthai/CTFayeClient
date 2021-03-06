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

package com.chatwing.whitelabel.modules;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.SearchManager;
import android.content.Context;
import android.graphics.Typeface;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.inputmethod.InputMethodManager;

import com.chatwing.whitelabel.ChatWing;
import com.chatwing.whitelabel.interfaces.FayeReceiver;
import com.chatwing.whitelabel.managers.ApiManager;
import com.chatwing.whitelabel.managers.ApiManagerImpl;
import com.chatwing.whitelabel.managers.CWNotificationManager;
import com.chatwing.whitelabel.managers.SyncManager;
import com.chatwing.whitelabel.managers.UserManager;
import com.chatwing.whitelabel.managers.VolleyManager;
import com.chatwing.whitelabel.parsers.EventParser;
import com.chatwing.whitelabel.parsers.EventParserImpl;
import com.chatwing.whitelabel.receivers.NetworkReceiver;
import com.chatwing.whitelabel.services.AckChatboxIntentService;
import com.chatwing.whitelabel.services.AckConversationIntentService;
import com.chatwing.whitelabel.services.BlockUserIntentService;
import com.chatwing.whitelabel.services.ChatWingChatService;
import com.chatwing.whitelabel.services.CreateBookmarkIntentService;
import com.chatwing.whitelabel.services.CreateConversationIntentService;
import com.chatwing.whitelabel.services.CreateMessageIntentService;
import com.chatwing.whitelabel.services.DeleteBookmarkIntentService;
import com.chatwing.whitelabel.services.DeleteMessageIntentService;
import com.chatwing.whitelabel.services.DownloadUserDetailIntentService;
import com.chatwing.whitelabel.services.FlagMessageIntentService;
import com.chatwing.whitelabel.services.GetMessagesIntentService;
import com.chatwing.whitelabel.services.IgnoreUserIntentService;
import com.chatwing.whitelabel.services.LoadChatBoxDetailsService;
import com.chatwing.whitelabel.services.NotificationIntentService;
import com.chatwing.whitelabel.services.NotificationStatusIntentService;
import com.chatwing.whitelabel.services.OfflineIntentService;
import com.chatwing.whitelabel.services.SyncBookmarkIntentService;
import com.chatwing.whitelabel.services.SyncCommunicationBoxesIntentService;
import com.chatwing.whitelabel.services.UpdateAvatarIntentService;
import com.chatwing.whitelabel.services.UpdateGcmIntentService;
import com.chatwing.whitelabel.services.UpdateNotificationSettingsService;
import com.chatwing.whitelabel.services.UpdateUserProfileService;
import com.chatwing.whitelabel.services.VerifyEmailIntentService;
import com.chatwing.whitelabel.tasks.PingUserTask;
import com.chatwing.whitelabel.utils.NetworkUtils;
import com.chatwing.whitelabel.validators.PermissionsValidator;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

import javax.inject.Provider;
import javax.inject.Singleton;

import asia.papaslove.ctfayeclient.CTFayeClient;
import dagger.Module;
import dagger.Provides;

/**
 * Created by cuongthai on 21/07/2014.
 */
@Module(
        injects = {
                ChatWing.class,
                CreateConversationIntentService.class,
                LoadChatBoxDetailsService.class,
                CreateMessageIntentService.class,
                UpdateGcmIntentService.class,
                NotificationIntentService.class,
                SyncCommunicationBoxesIntentService.class,
                UpdateNotificationSettingsService.class,
                OfflineIntentService.class,
                NotificationStatusIntentService.class,
                AckConversationIntentService.class,
                GetMessagesIntentService.class,
                AckChatboxIntentService.class,
                UpdateUserProfileService.class,
                DeleteMessageIntentService.class,
                BlockUserIntentService.class,
                IgnoreUserIntentService.class,
                FlagMessageIntentService.class,
                DownloadUserDetailIntentService.class,
                SyncBookmarkIntentService.class,
                CreateBookmarkIntentService.class,
                VerifyEmailIntentService.class,
                DeleteBookmarkIntentService.class,
                UpdateAvatarIntentService.class,
                ChatWingChatService.class,
                NetworkReceiver.class
        },
        library = true
)
public class ChatWingModule {
    private final Context mApplication;

    public ChatWingModule(Context application) {
        mApplication = application;
    }

    @Provides
    @Singleton
    @ForApplication
    Context provideApplicationContext() {
        return mApplication;
    }

    @Provides
    @Singleton
    Bus provideBus() {
        return new Bus(ThreadEnforcer.MAIN);
    }

    /**
     * Provides handler associates with main thread.
     * Since it is shared between multiple objects, {@link android.os.Message}s
     * that were posted by an object may be cleared unexpectedly by other objects.
     * So this handler should be used to post {@link Runnable}s only.
     */
    @Provides
    @Singleton
    @ForMainThread
    Handler provideMainHandler() {
        return new Handler(Looper.getMainLooper());
    }

    @Provides
    @Singleton
    InputMethodManager provideInputMethodManager() {
        return (InputMethodManager) mApplication.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    @Provides
    @Singleton
    NetworkUtils provideNetworkUtils() {
        return new NetworkUtils((ConnectivityManager) mApplication.getSystemService(
                Context.CONNECTIVITY_SERVICE));
    }

    @Provides
    @Singleton
    ApiManager provideApiManager(ApiManagerImpl impl) {
        return impl;
    }

    @Provides
    @Singleton
    UserManager provideUserManager(PermissionsValidator permissionsValidator, Bus bus, Provider<PingUserTask> pingTaskProvider) {
        return new UserManager(mApplication, bus, permissionsValidator, pingTaskProvider);
    }

    @Provides
    @Singleton
    Typeface provideIconicTypeface() {
        return Typeface.createFromAsset(mApplication.getAssets(), "iconic_font.ttf");
    }

    @Provides
    @Singleton
    VolleyManager provideVolleyManager() {
        return new VolleyManager(mApplication, new VolleyManager.OkHttpStack());
    }

    @Provides
    @Singleton
    SyncManager provideSyncManager(Bus bus, @ForMainThread Handler handler) {
        return new SyncManager(bus, handler);
    }

    @Provides
    @Singleton
    NotificationManager provideNotificationManager() {
        return (NotificationManager) mApplication.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Singleton
    @Provides
    SearchManager provideSearchManager() {
        return (SearchManager) mApplication.getSystemService(Context.SEARCH_SERVICE);
    }

    @Provides
    @Singleton
    FayeReceiver provideFayeClient(Bus bus,
                                   @ForMainThread Handler handler) {
        return new FayeReceiver(new CTFayeClient(), handler, bus);
    }


    @Provides
    @Singleton
    EventParser provideEventParser(EventParserImpl impl) {
        return impl;
    }

    @Provides
    @Singleton
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    SoundPool provideSoundPool() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            return new SoundPool.Builder()
                    .setMaxStreams(25)
                    .setAudioAttributes(audioAttributes)
                    .build();
        } else {
            return legacySoundPool();
        }
    }

    @SuppressWarnings("deprecation")
    private SoundPool legacySoundPool() {
        return new SoundPool(1, AudioManager.STREAM_NOTIFICATION, 0);
    }

    @Provides
    @Singleton
    public CWNotificationManager provideCWNotificationManager(@ForApplication Context context,
                                                              SoundPool soundPool,
                                                              UserManager userManager,
                                                              NotificationManager notificationManager) {
        return new CWNotificationManager(context, soundPool, notificationManager, userManager);
    }

}
