package com.chatwing.whitelabel.activities;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewTreeObserver;

import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.adapters.WalkthroughPagerAdapter;
import com.chatwing.whitelabel.fragments.WalkthroughPageFragment;
import com.chatwing.whitelabel.managers.UserManager;
import com.chatwing.whitelabel.modules.WalkthroughActivityModule;
import com.chatwing.whitelabel.pojos.User;
import com.chatwing.whitelabel.timers.SafeCountDownTimer;
import com.chatwing.whitelabel.validators.ChatBoxKeyValidator;
import com.viewpagerindicator.CirclePageIndicator;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

public class WalkthroughActivity extends BaseABFragmentActivity implements SafeCountDownTimer.Listener {
    public static final int AUTO_SCROLL_INTERVAL = 3000; // Tick every 3 seconds
    public static final int AUTO_SCROLL_TOTAL_TIME = 20 * AUTO_SCROLL_INTERVAL; // Tick 20 times

    protected static final int REQUEST_CODE_AUTHENTICATE = 1000;

    protected static final String EXTRA_REQUESTED_CHAT_BOX_KEY = "requested_chat_box_key";

    @Inject
    UserManager mUserManager;
    @Inject
    ChatBoxKeyValidator mChatBoxKeyValidator;
    @Inject
    WalkthroughPagerAdapter mWalkthroughPagerAdapter;
    @Inject
    Provider<SafeCountDownTimer> mTimerProvider;
    private ViewPager mViewPager;
    private SafeCountDownTimer mAutoScrollTimer;
    private boolean mIsAutoScrolling;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walkthrough);
        setupViews();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_AUTHENTICATE && resultCode == RESULT_OK) {
            checkUser((User) data.getSerializableExtra(AuthenticateActivity.INTENT_USER));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        restartAutoScrollTimer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopAutoScrollTimer();
    }

    ///////////////////////////////////////////////////////
    // SafeCountDownTimer.Listener
    ///////////////////////////////////////////////////////
    @Override
    public void onTick(long millisUntilFinished, boolean isFirstTick, Object tag) {
        if (isFirstTick) {
            // First tick is fired right after the timer was started.
            // But we want to auto scroll 1 interval after that time.
            // So we skip the first tick.
            return;
        }
        int currentItem = mViewPager.getCurrentItem();
        int nextItem = currentItem + 1;
        boolean smoothScroll = true;
        if (nextItem >= mWalkthroughPagerAdapter.getCount()) {
            nextItem = 0;
            smoothScroll = false;
        }
        mIsAutoScrolling = true;
        mViewPager.setCurrentItem(nextItem, smoothScroll);
        mIsAutoScrolling = false;
    }

    @Override
    public void onFinish(Object tag) {
    }

    //////////////////////////////////////////////////////////
    // Other instance methods
    //////////////////////////////////////////////////////////
    @Override
    protected List<Object> getModules() {
        return Arrays.<Object>asList(new WalkthroughActivityModule(this));
    }

    private void setupViews() {
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mViewPager.setAdapter(mWalkthroughPagerAdapter);

        CirclePageIndicator indicator = (CirclePageIndicator) findViewById(R.id.indicator);
        indicator.setViewPager(mViewPager);
        indicator.setSnap(true);
        indicator.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (!mIsAutoScrolling) {
                    // User has just selected a page, let's stop auto scrolling.
                    stopAutoScrollTimer();
                }
            }
        });
        if (Build.VERSION.SDK_INT >= 11) {
            mViewPager.setPageTransformer(false, new DepthPageTransformer());
        }

        // The content of each fragment in the view pager should not overlap
        // controls in this view.
        // So padding of the content in each fragment will be calculated when
        // the size of controls container is ready.
        final View controlsContainer = findViewById(R.id.controls_container);
        controlsContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                controlsContainer.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                Resources res = getResources();
                WalkthroughPageFragment.Padding fragmentPadding = new WalkthroughPageFragment.Padding(
                        res.getDimensionPixelSize(R.dimen.activity_horizontal_margin),
                        res.getDimensionPixelSize(R.dimen.activity_vertical_margin),
                        res.getDimensionPixelSize(R.dimen.activity_horizontal_margin),
                        controlsContainer.getHeight()
                );
                mWalkthroughPagerAdapter.setFragmentPadding(fragmentPadding);
            }
        });

        findViewById(R.id.register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(WalkthroughActivity.this, RegisterActivity.class);
                startActivityForResult(i, REQUEST_CODE_AUTHENTICATE);
            }
        });

        findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(WalkthroughActivity.this, LegacyLoginActivity.class);
                startActivityForResult(i, REQUEST_CODE_AUTHENTICATE);
            }
        });
    }

    private void checkUser(User intentUser) {
        if (intentUser == null) {
            return;
        }

        User user = mUserManager.getCurrentUser();

        if (user != null
                && user.equals(intentUser)) {
            //Okay, user is set
            if (user.isSessionValid()) {
                Intent intent = new Intent();
                intent.putExtra(AuthenticateActivity.INTENT_USER, intentUser);
                setResult(RESULT_OK, intent);
                finish();
            } else {
                // Session expired.
                mUserManager.removeUser(user);
            }
        }
    }

    private void restartAutoScrollTimer() {
        stopAutoScrollTimer();
        mAutoScrollTimer = mTimerProvider.get();
        mAutoScrollTimer.setListener(this);
        mAutoScrollTimer.start();
    }

    private void stopAutoScrollTimer() {
        if (mAutoScrollTimer != null) {
            mAutoScrollTimer.cancelSafely();
            mAutoScrollTimer = null;
        }
    }

    /**
     * Reference: http://docs.huihoo.com/android/4.4/training/animation/screen-slide.html
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static class DepthPageTransformer implements ViewPager.PageTransformer {
        private static float MIN_SCALE = 0.75f;

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0);

            } else if (position <= 0) { // [-1,0]
                // Use the default slide transition when moving to the left page
                view.setAlpha(1);
                view.setTranslationX(0);
                view.setScaleX(1);
                view.setScaleY(1);

            } else if (position <= 1) { // (0,1]
                // Fade the page out.
                view.setAlpha(1 - position);

                // Counteract the default slide transition
                view.setTranslationX(pageWidth * -position);

                // Scale the page down (between MIN_SCALE and 1)
                float scaleFactor = MIN_SCALE
                        + (1 - MIN_SCALE) * (1 - Math.abs(position));
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0);
            }
        }
    }
}
