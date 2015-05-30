package com.chatwing.whitelabel.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.RemoteControlClient;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.chatwing.whitelabel.BuildConfig;
import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.activities.ExtendCommunicationActivity;
import com.chatwing.whitelabel.utils.AudioFocusHelper;
import com.chatwing.whitelabel.utils.MediaButtonHelper;
import com.chatwing.whitelabel.utils.MusicFocusable;
import com.chatwing.whitelabel.utils.MusicIntentReceiver;
import com.chatwing.whitelabel.utils.RemoteControlClientCompat;
import com.chatwing.whitelabel.utils.RemoteControlHelper;
import com.chatwingsdk.ChatWing;
import com.chatwingsdk.pojos.Song;
import com.chatwingsdk.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by steve on 12/05/2015.
 */
public class MusicService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener, MusicFocusable {
    // These are the Intent actions that we are prepared to handle. Notice that the fact these
    // constants exist in our class is a mere convenience: what really defines the actions our
    // service can handle are the <action> tags in the <intent-filters> tag for our service in
    // AndroidManifest.xml.
    public static final String ACTION_TOGGLE_PLAYBACK =
            BuildConfig.APPLICATION_ID + ".action.TOGGLE_PLAYBACK";
    public static final String ACTION_PLAY = BuildConfig.APPLICATION_ID + ".action.PLAY";
    public static final String ACTION_PAUSE = BuildConfig.APPLICATION_ID + ".action.PAUSE";
    public static final String ACTION_NEXT = BuildConfig.APPLICATION_ID + ".action.NEXT";
    public static final String ACTION_BACK = BuildConfig.APPLICATION_ID + ".action.BACK";
    public static final String ACTION_STOP = BuildConfig.APPLICATION_ID + ".action.STOP";
    public static final String ACTION_URL = BuildConfig.APPLICATION_ID + ".action.URL";
    public static final String ACTION_PLAY_AT_INDEX = BuildConfig.APPLICATION_ID + ".action.PLAY_INDEX";
    public static final String ACTION_PLAY_LAST_MEDIA_IF_STOPPING = BuildConfig.APPLICATION_ID + ".action.PLAY_LAST_IF_STOPPING";
    public static final String SONG_EXTRA = "SONG_EXTRA";
    public static final String SONG_INDEX_EXTRA = "SONG_INDEX_EXTRA";
    public static final String EVENT_CONTROL_CHANGED = "EVENT_CONTROL_CHANGED";

    private final IBinder musicBind = new MusicBinder();
    //media mPlayer
    private MediaPlayer mPlayer;
    private STATUS mState = STATUS.STOPPED;
    AudioFocus mAudioFocus = AudioFocus.NoFocusNoDuck;
    // our AudioFocusHelper object, if it's available (it's available on SDK level >= 8)
    // If not available, this will be null. Always check for null before using!
    AudioFocusHelper mAudioFocusHelper = null;

    // The ID we use for the notification (the onscreen alert that appears at the notification
    // area at the top of the screen as an icon -- and as text as well if the user expands the
    // notification area).
    final int NOTIFICATION_ID = 1;
    NotificationManager mNotificationManager;
    Notification mNotification = null;
    WifiManager.WifiLock mWifiLock;
    AudioManager mAudioManager;
    private String mSongTitle = "";
    private String mChatboxName = "";
    private List<Song> songs = new ArrayList<Song>();
    ComponentName mMediaButtonReceiverComponent;
    // our RemoteControlClient object, which will use remote control APIs available in
    // SDK level >= 14, if they're available.
    RemoteControlClientCompat mRemoteControlClientCompat;
    // The volume we set the media player to when we lose audio focus, but are allowed to reduce
    // the volume instead of stopping playback.
    public static final float DUCK_VOLUME = 0.1f;
    // Dummy album art we will pass to the remote control (if the APIs are available).
    Bitmap mDummyAlbumArt;
    private int currentSongIndex = -1;

    void processTogglePlaybackRequest() {
        if (mState == STATUS.PAUSED || mState == STATUS.STOPPED) {
            processPlayRequest();
        } else {
            processPauseRequest();
        }
    }

    void processPlayAtIndex(Intent intent) {
        if (mState == STATUS.PLAYING || mState == STATUS.PAUSED || mState == STATUS.STOPPED) {
            int index = intent.getIntExtra(SONG_INDEX_EXTRA, 0);
            LogUtils.v("Playing at index " + index);
            if (index < 0 || index > songs.size() - 1) {
                return;
            }
            currentSongIndex = index;
            playSong();
        }
    }

    void processAddRequest(Intent intent) {
        //User add enqueue a song
        if (mState == STATUS.PLAYING || mState == STATUS.PAUSED || mState == STATUS.STOPPED) {
            Song song = (Song) intent.getSerializableExtra(SONG_EXTRA);
            if (!songs.contains(song)) {
                songs.add(song);
                tryToGetAudioFocus();
            }
        }
    }

    void processPlayRequest() {
        tryToGetAudioFocus();
        // actually play the song
        if (mState == STATUS.STOPPED) {
            if (songs.size() != 0) {
                if (currentSongIndex == -1) {
                    currentSongIndex = 0;
                }
                playSong();
            }
        } else if (mState == STATUS.PAUSED) {
            // If we're paused, just continue playback and restore the 'foreground service' state.
            mState = STATUS.PLAYING;
            setUpAsForeground(mChatboxName + mSongTitle + " (playing)");
            configAndStartMediaPlayer();
            broadCastEvent(EVENT_CONTROL_CHANGED);
        }
        // Tell any remote controls that our playback state is 'playing'.
        if (mRemoteControlClientCompat != null) {
            mRemoteControlClientCompat
                    .setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
        }
    }

    void processPauseRequest() {
        if (mState == STATUS.PLAYING) {
            // Pause media player and cancel the 'foreground service' state.
            mState = STATUS.PAUSED;
            mPlayer.pause();
            relaxResources(false); // while paused, we always retain the MediaPlayer
            // do not give up audio focus
            broadCastEvent(EVENT_CONTROL_CHANGED);
        }
        // Tell any remote controls that our playback state is 'paused'.
        if (mRemoteControlClientCompat != null) {
            mRemoteControlClientCompat
                    .setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED);
        }
    }

    void processStopRequest() {
        processStopRequest(false);
    }

    void processStopRequest(boolean force) {
        if (mState == STATUS.PLAYING || mState == STATUS.PAUSED || force) {
            mState = STATUS.STOPPED;
            broadCastEvent(EVENT_CONTROL_CHANGED);

            // let go of all resources...
            relaxResources(true);
            giveUpAudioFocus();
            // Tell any remote controls that our playback state is 'paused'.
            if (mRemoteControlClientCompat != null) {
                mRemoteControlClientCompat
                        .setPlaybackState(RemoteControlClient.PLAYSTATE_STOPPED);
            }
            // service is no longer necessary. Will be started again if needed.
            stopSelf();
        }
    }

    @Override
    public void onGainedAudioFocus() {
        mAudioFocus = AudioFocus.Focused;
        // restart media player with new focus settings
        if (mState == STATUS.PLAYING)
            configAndStartMediaPlayer();
    }

    @Override
    public void onLostAudioFocus(boolean canDuck) {
        mAudioFocus = canDuck ? AudioFocus.NoFocusCanDuck : AudioFocus.NoFocusNoDuck;
        // start/restart/pause media player with new focus settings
        if (mPlayer != null && mPlayer.isPlaying())
            configAndStartMediaPlayer();
    }

    public boolean containsSong(Song song) {
        return songs.contains(song);
    }

    public Song getCurrentSong() {
        if (currentSongIndex < 0 || currentSongIndex > songs.size() - 1) {
            return null;
        }
        return songs.get(currentSongIndex);
    }


    public enum STATUS {
        STOPPED,
        PLAYING,
        PREPARING,
        PAUSED
    }

    // do we have audio focus?
    enum AudioFocus {
        NoFocusNoDuck,    // we don't have audio focus, and can't duck
        NoFocusCanDuck,   // we don't have focus, but can play at a low volume ("ducking")
        Focused           // we have full audio focus
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.v("onStartCommand ");
        String action = intent.getAction();
        if (action == null) {
            return START_NOT_STICKY; // Means we started the service, but don't want it to
            // restart in case it's killed.
        }
        if (action.equals(ACTION_TOGGLE_PLAYBACK)) processTogglePlaybackRequest();
        else if (action.equals(ACTION_PLAY)) processPlayRequest();
        else if (action.equals(ACTION_PAUSE)) processPauseRequest();
        else if (action.equals(ACTION_NEXT)) processNextRequest();
        else if (action.equals(ACTION_BACK)) processBackRequest();
        else if (action.equals(ACTION_STOP)) processStopRequest();
        else if (action.equals(ACTION_URL)) processAddRequest(intent);
        else if (action.equals(ACTION_PLAY_AT_INDEX)) processPlayAtIndex(intent);
        else if (action.equals(ACTION_PLAY_LAST_MEDIA_IF_STOPPING))
            processPlayLastMediaIfStopping();
        return START_NOT_STICKY; // Means we started the service, but don't want it to
        // restart in case it's killed.
    }

    private void processBackRequest() {
        if (currentSongIndex < 0 || currentSongIndex > songs.size() - 1) {
            return;
        }

        currentSongIndex = currentSongIndex == 0 ? songs.size() - 1 : currentSongIndex - 1;
        playSong();
    }

    private void processNextRequest() {
        if (currentSongIndex < 0 || currentSongIndex > songs.size() - 1) {
            return;
        }

        currentSongIndex = currentSongIndex == songs.size() - 1 ? 0 : currentSongIndex + 1;
        playSong();
    }

    private void processPlayLastMediaIfStopping() {
        if (mState == STATUS.STOPPED) {
            currentSongIndex = songs.size() - 1;
            playSong();
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mWifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        // create the Audio Focus Helper, if the Audio Focus feature is available (SDK 8 or above)
        if (android.os.Build.VERSION.SDK_INT >= 8)
            mAudioFocusHelper = new AudioFocusHelper(getApplicationContext(), this);
        else
            mAudioFocus = AudioFocus.Focused; // no focus feature, so we always "have" audio focus

        mDummyAlbumArt = BitmapFactory.decodeResource(getResources(), R.drawable.dummy_album_art);
        mMediaButtonReceiverComponent = new ComponentName(this, MusicIntentReceiver.class);

    }

    public void createMediaPlayerIfNeeded() {
        if (mPlayer == null) {
            mPlayer = new MediaPlayer();

            //set mPlayer properties
            mPlayer.setWakeMode(getApplicationContext(),
                    PowerManager.PARTIAL_WAKE_LOCK);
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mPlayer.setOnPreparedListener(this);
            mPlayer.setOnCompletionListener(this);
            mPlayer.setOnErrorListener(this);
        } else {
            mPlayer.reset();
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return musicBind;
    }


    public STATUS getStatus() {
        return mState;
    }

    public int getNumberOfSongs() {
        return songs.size();
    }

    public int getCurrentSongIndex() {
        return currentSongIndex;
    }

    public List<Song> getSongs() {
        return songs;
    }

    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if (currentSongIndex < songs.size() - 1) {
            currentSongIndex++;
            playSong();
        }
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
        LogUtils.e("Error: what=" + String.valueOf(what) + ", extra=" + String.valueOf(extra));

        mState = STATUS.STOPPED;
        relaxResources(true);
        giveUpAudioFocus();
        return true; // true indicates we handled the error
    }

    @Override
    public void onDestroy() {
        // Service is being killed, so make sure we release our resources
        mState = STATUS.STOPPED;
        relaxResources(true);
        giveUpAudioFocus();
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        LogUtils.v("On Prepared");
        mPlayer.start();
        mState = STATUS.PLAYING;
        broadCastEvent(EVENT_CONTROL_CHANGED);
        updateNotification("Box :" + mChatboxName + "-" + mSongTitle);
    }

    private void playSong() {
        if (currentSongIndex == -1 || currentSongIndex >= songs.size()) {
            return;
        }
        try {
            mState = STATUS.STOPPED;
            relaxResources(false); // release everything except MediaPlayer

            createMediaPlayerIfNeeded();
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            Song song = songs.get(currentSongIndex);
            mPlayer.setDataSource(song.getAudioUrl());
            mSongTitle = song.getAudioName();
            mChatboxName = song.getHostedChatboxName();
            mState = STATUS.PREPARING;
            broadCastEvent(EVENT_CONTROL_CHANGED);
            setUpAsForeground("Box :" + mChatboxName + "-" + mSongTitle + " (Loading)");
            // Use the media button APIs (if available) to register ourselves for media button
            // events
            MediaButtonHelper.registerMediaButtonEventReceiverCompat(
                    mAudioManager, mMediaButtonReceiverComponent);
            // Use the remote control APIs (if available) to set the playback state
            if (mRemoteControlClientCompat == null) {
                Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
                intent.setComponent(mMediaButtonReceiverComponent);
                mRemoteControlClientCompat = new RemoteControlClientCompat(
                        PendingIntent.getBroadcast(this /*context*/,
                                0 /*requestCode, ignored*/, intent /*intent*/, 0 /*flags*/));
                RemoteControlHelper.registerRemoteControlClient(mAudioManager,
                        mRemoteControlClientCompat);
            }
            mRemoteControlClientCompat.setPlaybackState(
                    RemoteControlClient.PLAYSTATE_PLAYING);
            mRemoteControlClientCompat.setTransportControlFlags(
                    RemoteControlClient.FLAG_KEY_MEDIA_PLAY |
                            RemoteControlClient.FLAG_KEY_MEDIA_PAUSE |
                            RemoteControlClient.FLAG_KEY_MEDIA_NEXT |
                            RemoteControlClient.FLAG_KEY_MEDIA_STOP);
            // Update the remote controls
            mRemoteControlClientCompat.editMetadata(true)
                    .putString(MediaMetadataRetriever.METADATA_KEY_TITLE, song.getAudioName())
                    .putBitmap(
                            RemoteControlClientCompat.MetadataEditorCompat.METADATA_KEY_ARTWORK,
                            mDummyAlbumArt)
                    .apply();
            // starts preparing the media mPlayer in the background. When it's done, it will call
            // our OnPreparedListener (that is, the onPrepared() method on this class, since we set
            // the listener to 'this').
            //
            // Until the media mPlayer is prepared, we *cannot* call start() on it!

            mPlayer.prepareAsync();
            mWifiLock.acquire();
        } catch (Exception e) {
            LogUtils.e("Music Player Error setting data source");
        }
    }

    private void broadCastEvent(String action) {
        Intent intent = new Intent(action);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    /**
     * Updates the notification.
     */
    void updateNotification(String text) {
        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                getOpenIntent(),
                PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(com.chatwingsdk.R.drawable.ic_launcher)
                        .setContentTitle(text)
                        .setTicker(text)
                        .setContentText(getString(R.string.message_touch_to_stop));

        builder.setContentIntent(contentIntent);
        mNotification = builder.build();
        mNotificationManager.notify(NOTIFICATION_ID, mNotification);
    }

    private Intent getOpenIntent() {
        Intent i = new Intent(this, ChatWing.instance(this).getMainActivityClass());
        i.setAction(ExtendCommunicationActivity.ACTION_STOP_MEDIA);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return i;
    }

    /**
     * Configures service as a foreground service. A foreground service is a service that's doing
     * something the user is actively aware of (such as playing music), and must appear to the
     * user as a notification. That's why we create the notification here.
     */
    void setUpAsForeground(String text) {
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, getOpenIntent(), PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(com.chatwingsdk.R.drawable.ic_launcher)
                        .setContentTitle(text)
                        .setTicker(text)
                        .setContentText(getString(R.string.message_touch_to_stop));

        builder.setContentIntent(contentIntent);

        mNotification = builder.build();
        startForeground(NOTIFICATION_ID, mNotification);
    }

    /**
     * Reconfigures MediaPlayer according to audio focus settings and starts/restarts it. This
     * method starts/restarts the MediaPlayer respecting the current audio focus state. So if
     * we have focus, it will play normally; if we don't have focus, it will either leave the
     * MediaPlayer paused or set it to a low volume, depending on what is allowed by the
     * current focus settings. This method assumes mPlayer != null, so if you are calling it,
     * you have to do so from a context where you are sure this is the case.
     */
    void configAndStartMediaPlayer() {
        if (mAudioFocus == AudioFocus.NoFocusNoDuck) {
            // If we don't have audio focus and can't duck, we have to pause, even if mState
            // is State.Playing. But we stay in the Playing state so that we know we have to resume
            // playback once we get the focus back.
            if (mPlayer.isPlaying()) mPlayer.pause();
            return;
        } else if (mAudioFocus == AudioFocus.NoFocusCanDuck)
            mPlayer.setVolume(DUCK_VOLUME, DUCK_VOLUME);  // we'll be relatively quiet
        else
            mPlayer.setVolume(1.0f, 1.0f); // we can be loud
        if (!mPlayer.isPlaying()) mPlayer.start();
    }

    void tryToGetAudioFocus() {
        if (mAudioFocus != AudioFocus.Focused && mAudioFocusHelper != null
                && mAudioFocusHelper.requestFocus())
            mAudioFocus = AudioFocus.Focused;
    }

    /**
     * Releases resources used by the service for playback. This includes the "foreground service"
     * status and notification, the wake locks and possibly the MediaPlayer.
     *
     * @param releaseMediaPlayer Indicates whether the Media Player should also be released or not
     */
    void relaxResources(boolean releaseMediaPlayer) {
        // stop being a foreground service
        stopForeground(true);
        // stop and release the Media Player, if it's available
        if (releaseMediaPlayer && mPlayer != null) {
            mPlayer.reset();
            mPlayer.release();
            mPlayer = null;
        }
        // we can also release the Wifi lock, if we're holding it
        if (mWifiLock.isHeld()) mWifiLock.release();
    }

    void giveUpAudioFocus() {
        if (mAudioFocus == AudioFocus.Focused && mAudioFocusHelper != null
                && mAudioFocusHelper.abandonFocus())
            mAudioFocus = AudioFocus.NoFocusNoDuck;
    }


}