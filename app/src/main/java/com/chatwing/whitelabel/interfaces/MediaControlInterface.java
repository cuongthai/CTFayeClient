package com.chatwing.whitelabel.interfaces;

import com.chatwing.whitelabel.services.MusicService;
import com.chatwingsdk.pojos.Song;

/**
 * Created by steve on 15/05/2015.
 */
public interface MediaControlInterface {
    MusicService.STATUS getMediaStatus();

    void pauseCurrentPlayingMedia();

    boolean isBindMediaService();

    void playMedia(Song song);

    void updateUIForPlayerPreparing(boolean preparing);

    void resumeMedia();
}
