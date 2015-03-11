package com.chatwing.whitelabel.timers;

import android.os.CountDownTimer;

import java.lang.ref.WeakReference;

/**
 * Created by nguyenthanhhuy on 4/4/14.
 */
public class SafeCountDownTimer extends CountDownTimer {
    public interface Listener {
        void onTick(long millisUntilFinished, boolean isFirstTick, Object tag);

        void onFinish(Object tag);
    }

    private WeakReference<Listener> mListenerRef;
    private Object mTag;
    private boolean mIsFirstTick;

    public SafeCountDownTimer(long millisInFuture, long countDownInterval) {
        super(millisInFuture, countDownInterval);
        mIsFirstTick = true;
    }

    public void setListener(Listener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }
        mListenerRef = new WeakReference<Listener>(listener);
    }

    public void setTag(Object tag) {
        mTag = tag;
    }

    @Override
    public void onTick(long millisUntilFinished) {
        Listener listener = mListenerRef.get();
        if (listener != null) {
            listener.onTick(millisUntilFinished, mIsFirstTick, mTag);
        }
        mIsFirstTick = false;
    }

    @Override
    public void onFinish() {
        Listener listener = mListenerRef.get();
        if (listener != null) {
            listener.onFinish(mTag);
        }
        clear();
    }

    public void cancelSafely() {
        clear();
        cancel();
    }

    private void clear() {
        if (mListenerRef.get() != null) {
            mListenerRef.clear();
        }
        mTag = null;
    }
}
