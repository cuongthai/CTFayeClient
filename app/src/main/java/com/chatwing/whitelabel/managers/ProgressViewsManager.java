package com.chatwing.whitelabel.managers;

import android.content.res.Resources;
import android.view.View;
import android.widget.TextView;

import com.chatwing.whitelabel.Constants;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.view.ViewPropertyAnimator;

import javax.inject.Inject;

import static com.nineoldandroids.view.ViewPropertyAnimator.animate;

/**
 * Created by nguyenthanhhuy on 4/15/14.
 */
public class ProgressViewsManager {
    private final int mAnimTime;
    private View mProgressView;
    private TextView mProgressText;
    private View mContentView;

    @Inject
    ProgressViewsManager(Resources res) {
        mAnimTime = res.getInteger(android.R.integer.config_shortAnimTime);
    }

    public void setViews(View progressView, TextView progressText, View contentView) {
        mProgressView = progressView;
        mProgressText = progressText;
        mContentView = contentView;
    }

    public void showProgress(final boolean show) {
        showProgress(show, 0);
    }

    public void showProgress(final boolean show, int progressTextId) {
        // Animations should be started immediately instead of at the next opportunity.
        // This is useful while testing.
        boolean shouldStartImmediately = Constants.DEBUG;

        mProgressView.setVisibility(View.VISIBLE);
        ViewPropertyAnimator animator = animate(mProgressView)
                .setDuration(mAnimTime)
                .alpha(show ? 1 : 0)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                    }
                });
        //noinspection ConstantConditions
        if (shouldStartImmediately) {
            animator.start();
        }

        mContentView.setVisibility(View.VISIBLE);
        animator = animate(mContentView)
                .setDuration(mAnimTime)
                .alpha(show ? 0 : 1)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mContentView.setVisibility(show ? View.GONE : View.VISIBLE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                    }
                });
        //noinspection ConstantConditions
        if (shouldStartImmediately) {
            animator.start();
        }

        if (show && mProgressText != null && progressTextId != 0) {
            mProgressText.setText(progressTextId);
        }
    }
}
