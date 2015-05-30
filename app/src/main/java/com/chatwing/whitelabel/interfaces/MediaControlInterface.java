package com.chatwing.whitelabel.interfaces;

import com.chatwing.whitelabel.services.MusicService;
import com.chatwingsdk.fragments.InjectableFragmentDelegate;
import com.chatwingsdk.pojos.Song;

/**
 * Created by steve on 15/05/2015.
 */
public interface MediaControlInterface extends InjectableFragmentDelegate{
    MusicService.STATUS getMediaStatus();

    boolean isBindMediaService();

    void updateUIForPlayerPreparing(boolean preparing);

    MusicService getMediaService();

    void enqueue(Song song);

    void playLastMediaIfStopping();

}
