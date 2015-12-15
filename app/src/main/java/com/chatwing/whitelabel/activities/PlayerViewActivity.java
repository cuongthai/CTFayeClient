package com.chatwing.whitelabel.activities;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.chatwing.whitelabel.Constants;
import com.chatwing.whitelabel.R;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * Created by cuongthai on 12/15/15.
 */
@TargetApi(11)
public class PlayerViewActivity extends YouTubeFailureRecoveryActivity implements
        YouTubePlayer.OnFullscreenListener {

    public static final String YOUTUBE_VIDEO_ID = "YOUTUBE_VIDEO_ID";
    private ActionBarPaddedFrameLayout viewContainer;
    private YouTubePlayerFragment playerFragment;
    private String youtubeVideoID;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.youtube_playerview);
        youtubeVideoID = getIntent().getStringExtra(YOUTUBE_VIDEO_ID);
        viewContainer = (ActionBarPaddedFrameLayout) findViewById(R.id.view_container);
        playerFragment =
                (YouTubePlayerFragment) getFragmentManager().findFragmentById(R.id.player_fragment);
        playerFragment.initialize(Constants.YOUTUBE_DEVELOPER_KEY, this);
        viewContainer.setActionBar(getActionBar());

        // Action bar background is transparent by default.
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.primary)));
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player,
                                        boolean wasRestored) {
        player.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT);
        player.setOnFullscreenListener(this);

        if (!wasRestored) {
            player.loadVideo(youtubeVideoID);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected YouTubePlayer.Provider getYouTubePlayerProvider() {
        return (YouTubePlayerFragment) getFragmentManager().findFragmentById(R.id.player_fragment);
    }

    @Override
    public void onFullscreen(boolean fullscreen) {
        viewContainer.setEnablePadding(!fullscreen);

        ViewGroup.LayoutParams playerParams = playerFragment.getView().getLayoutParams();
        if (fullscreen) {
            playerParams.width = MATCH_PARENT;
            playerParams.height = MATCH_PARENT;
        } else {
            playerParams.width = 0;
            playerParams.height = WRAP_CONTENT;
        }
    }

    /**
     * This is a FrameLayout which adds top-padding equal to the height of the ActionBar unless
     * disabled by {@link #setEnablePadding(boolean)}.
     */
    public static final class ActionBarPaddedFrameLayout extends FrameLayout {

        private ActionBar actionBar;
        private boolean paddingEnabled;

        public ActionBarPaddedFrameLayout(Context context) {
            this(context, null);
        }

        public ActionBarPaddedFrameLayout(Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public ActionBarPaddedFrameLayout(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
            paddingEnabled = false;
        }

        public void setActionBar(ActionBar actionBar) {
            this.actionBar = actionBar;
            requestLayout();
        }

        public void setEnablePadding(boolean enable) {
            paddingEnabled = enable;
            requestLayout();
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int topPadding =
                    paddingEnabled && actionBar != null && actionBar.isShowing() ? actionBar.getHeight() : 0;
            setPadding(0, topPadding, 0, 0);

            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

}
