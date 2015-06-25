package com.chatwing.whitelabel.interfaces;

import com.chatwing.whitelabel.fragments.InjectableFragmentDelegate;
import com.chatwing.whitelabel.pojos.Song;
import com.chatwing.whitelabel.services.MusicService;

/**
 * Created by steve on 15/05/2015.
 */
public interface MediaControlInterface extends InjectableFragmentDelegate {
    MusicService.STATUS getMediaStatus();

    boolean isBindMediaService();

    void updateUIForPlayerPreparing(boolean preparing);

    MusicService getMediaService();

    void enqueue(Song song);

    void playLastMediaIfStopping();

}
