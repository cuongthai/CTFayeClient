package com.chatwing.whitelabel.pojos;


import com.chatwing.whitelabel.utils.LogUtils;

import java.io.Serializable;

/**
 * Created by steve on 12/05/2015.
 */
public class Song implements Serializable {
    private String audioUrl;
    private String audioName;
    private String hostedChatboxName;

    public Song(String audioUrl, String audioName, String hostedChatboxName) {
        this.audioUrl = audioUrl;
        this.audioName = audioName;
        this.hostedChatboxName = hostedChatboxName;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public String getAudioName() {
        return audioName;
    }

    public String getHostedChatboxName() {
        return hostedChatboxName;
    }

    @Override
    public boolean equals(Object o) {
        Song song = (Song) o;
        return song.hostedChatboxName.equals(hostedChatboxName)
                && song.audioName.equals(audioName)
                && song.audioUrl.equals(audioUrl);
    }

    @Override
    public int hashCode() {
        int i = (audioUrl + audioName + hostedChatboxName).hashCode();
        LogUtils.v(audioUrl + " Hashcode " + i);
        return i;
    }
}
