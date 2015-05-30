package com.chatwing.whitelabel.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chatwing.whitelabel.R;
import com.chatwingsdk.modules.ForActivity;
import com.chatwingsdk.pojos.Song;

import javax.inject.Inject;

/**
 * Created by steve on 1/4/14.
 */
public class MusicPlaylistAdapter extends CompatArrayAdapter<Song> {
    private LayoutInflater mInflater;

    @Inject
    MusicPlaylistAdapter(@ForActivity Context context,
                         @ForActivity LayoutInflater layoutInflater) {
        super(context, 0);
        mInflater = layoutInflater;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    public Song getItem(int i) {
        return super.getItem(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_music_playlist, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.songName
                    = (TextView) convertView.findViewById(R.id.song_name);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Song song = getItem(position);
        viewHolder.songName.setText(song.getAudioName());

        return convertView;
    }

    private static class ViewHolder {
        private TextView songName;
    }
}
