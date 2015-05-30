package com.chatwing.whitelabel.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.TextView;

import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.activities.NoMenuWebViewActivity;
import com.chatwing.whitelabel.adapters.FeedAdapter;
import com.chatwing.whitelabel.events.UserSelectedFeedSource;
import com.chatwing.whitelabel.events.UserSelectedSongEvent;
import com.chatwing.whitelabel.interfaces.MediaControlInterface;
import com.chatwing.whitelabel.services.MusicService;
import com.chatwingsdk.fragments.InjectableFragmentDelegate;
import com.chatwingsdk.pojos.ChatBox;
import com.chatwingsdk.pojos.Song;
import com.chatwingsdk.utils.LogUtils;
import com.chatwingsdk.views.ErrorMessageView;
import com.pkmmte.pkrss.Article;
import com.pkmmte.pkrss.Callback;
import com.pkmmte.pkrss.Category;
import com.pkmmte.pkrss.PkRSS;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class MusicFragment extends Fragment implements OnClickListener {
    private MediaControlInterface mDelegate;

    @Inject
    Bus mBus;
    @Inject
    ErrorMessageView mErrorMessageView;
    private Category mCategory;
    private View playBtn;
    private View pauseBtn;
    private View backBtn;
    private View nextBtn;
    private TextView songNameTv;
    private TextView chatboxNameTv;

    //Handle broadcast event from MusicService
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateUI();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_music_player, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        mBus.register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        mBus.unregister(this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDelegate.inject(this);
        playBtn = view.findViewById(R.id.media_play);
        pauseBtn = view.findViewById(R.id.media_pause);
        nextBtn = view.findViewById(R.id.media_ff);
        backBtn = view.findViewById(R.id.media_rew);
        songNameTv = (TextView) view.findViewById(R.id.songName);
        chatboxNameTv = (TextView) view.findViewById(R.id.chatbox);

        playBtn.setOnClickListener(this);
        pauseBtn.setOnClickListener(this);
        nextBtn.setOnClickListener(this);
        backBtn.setOnClickListener(this);
        updateUI();
    }

    private void updateUI() {
        Song currentSong = mDelegate.isBindMediaService() ? mDelegate.getMediaService().getCurrentSong() : null;
        chatboxNameTv.setText(currentSong != null ? currentSong.getHostedChatboxName() : null);
        songNameTv.setText(currentSong != null ? currentSong.getAudioName() : null);
    }

    public static MusicFragment newInstance() {
        MusicFragment mFragment = new MusicFragment();
        return mFragment;
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Register mMessageReceiver to receive messages.
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver,
                new IntentFilter(MusicService.EVENT_CONTROL_CHANGED));
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mDelegate = (MediaControlInterface) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mDelegate = null;
    }

    @Override
    public void onClick(View view) {
        if (view == playBtn) {
            resumeSong();
        } else if (view == pauseBtn) {
            pauseSong();
        } else if (view == nextBtn) {
            nextSong();
        } else if (view == backBtn) {
            backSong();
        }
        updateUI();
    }

    @Subscribe
    public void onUserSelectedSongEvent(UserSelectedSongEvent event) {
        playSongAtIndex(event.getPosition());
    }

    private void playSongAtIndex(int position) {
        Intent service = new Intent(MusicService.ACTION_PLAY_AT_INDEX);
        service.putExtra(MusicService.SONG_INDEX_EXTRA, position);
        getActivity().startService(service);
    }

    public void resumeSong() {
        getActivity().startService(new Intent(MusicService.ACTION_PLAY));
    }

    public void pauseSong() {
        getActivity().startService(new Intent(MusicService.ACTION_PAUSE));
    }

    public void backSong() {
        getActivity().startService(new Intent(MusicService.ACTION_NEXT));
    }

    public void nextSong() {
        getActivity().startService(new Intent(MusicService.ACTION_BACK));
    }
}