package com.chatwing.whitelabel.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.chatwing.whitelabel.R;

import java.io.Serializable;

/**
 * Created by nguyenthanhhuy on 4/3/14.
 */
public class WalkthroughPageFragment extends Fragment {
    private static final String EXTRA_INFO = "info";

    public static class Info implements Serializable {
        public final int mBackgroundColor;
        public final int mBackgroundDrawable;
        public final int mMainText;
        public final int mSecondaryText;
        public final int mContentDrawable;
        public final Padding mContentPadding;

        public Info(int backgroundDrawable, int backgroundColor, int mainText,
                    int secondaryText, int contentDrawable, Padding contentPadding) {
            mBackgroundDrawable = backgroundDrawable;
            mBackgroundColor = backgroundColor;
            mMainText = mainText;
            mSecondaryText = secondaryText;
            mContentDrawable = contentDrawable;
            mContentPadding = contentPadding;
        }
    }

    public static class Padding implements Serializable {
        public final int mLeft;
        public final int mTop;
        public final int mRight;
        public final int mBottom;

        public Padding(int left, int top, int right, int bottom) {
            mLeft = left;
            mTop = top;
            mRight = right;
            mBottom = bottom;
        }
    }

    public static WalkthroughPageFragment newInstance(Info info) {
        WalkthroughPageFragment instance = new WalkthroughPageFragment();
        Bundle args = new Bundle(1);
        args.putSerializable(EXTRA_INFO, info);
        instance.setArguments(args);
        return instance;
    }

    public WalkthroughPageFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_walkthrough_page, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Info info = (Info) getArguments().get(EXTRA_INFO);
        ImageView backgroundImage = (ImageView) view.findViewById(R.id.background_image);
        View contentContainer = view.findViewById(R.id.content_container);
        TextView mainText = (TextView) view.findViewById(R.id.main_text);
        TextView secondaryText = (TextView) view.findViewById(R.id.secondary_text);
        ImageView contentImage = (ImageView) view.findViewById(R.id.content_image);

        contentContainer.setBackgroundColor(info.mBackgroundColor);
        Padding contentPadding = info.mContentPadding;
        if (contentPadding != null) {
            contentContainer.setPadding(
                    contentPadding.mLeft,
                    contentPadding.mTop,
                    contentPadding.mRight,
                    contentPadding.mBottom);
        }
        if (info.mBackgroundDrawable == 0) {
            backgroundImage.setVisibility(View.GONE);
        } else {
            backgroundImage.setVisibility(View.VISIBLE);
            backgroundImage.setImageResource(info.mBackgroundDrawable);
        }
        if (info.mMainText != 0) {
            mainText.setText(info.mMainText);
        }
        if (info.mSecondaryText != 0) {
            secondaryText.setText(info.mSecondaryText);
        }
        if (info.mContentDrawable != 0) {
            contentImage.setImageResource(info.mContentDrawable);
        }
    }
}
