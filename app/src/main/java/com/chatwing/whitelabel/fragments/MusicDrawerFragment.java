package com.chatwing.whitelabel.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.chatwing.whitelabel.Constants;
import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.adapters.FeedSourcesAdapter;
import com.chatwing.whitelabel.adapters.MusicPlaylistAdapter;
import com.chatwing.whitelabel.events.UserSelectedFeedSource;
import com.chatwing.whitelabel.events.UserSelectedSongEvent;
import com.chatwing.whitelabel.interfaces.MediaControlInterface;
import com.chatwing.whitelabel.services.MusicService;
import com.squareup.otto.Bus;

import javax.inject.Inject;

public class MusicDrawerFragment extends ListFragment {
    @Inject
    MusicPlaylistAdapter mAdapter;
    @Inject
    Bus mBus;

    private NavigatableFragmentListener mListener;
    private MediaControlInterface controlInterface;

    public MusicDrawerFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_music_play_list, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        view.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.back(MusicDrawerFragment.this);
            }
        });
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        controlInterface = (MediaControlInterface) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        controlInterface = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        mBus.register(this);
        reloadPlayList();
    }

    private void reloadPlayList() {
        if(controlInterface.isBindMediaService()){
            MusicService mediaService = controlInterface.getMediaService();
            mAdapter.clear();
            mAdapter.addAll(mediaService.getSongs());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mBus.unregister(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mListener = (NavigatableFragmentListener) getActivity();
        mListener.inject(this);

        setListAdapter(mAdapter);
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        mBus.post(new UserSelectedSongEvent(position));
    }

}
