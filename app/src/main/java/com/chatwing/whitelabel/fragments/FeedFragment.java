package com.chatwing.whitelabel.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.GridView;
import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.activities.NoMenuWebViewActivity;
import com.chatwing.whitelabel.adapters.FeedAdapter;
import com.chatwing.whitelabel.events.UserSelectedFeedSource;
import com.chatwingsdk.fragments.InjectableFragmentDelegate;
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

public class FeedFragment extends Fragment implements FeedAdapter.OnArticleClickListener, Callback {
    private InjectableFragmentDelegate mDelegate;

    // Feed list & adapter
    private List<Article> mFeed = new ArrayList<Article>();
    private FeedAdapter mAdapter;

    private GridView mGrid;
    private View mNoContent;
    private View mLoadingView;

    @Inject
    Bus mBus;
    @Inject
    ErrorMessageView mErrorMessageView;
    private Category mCategory;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_feed, container, false);
        mGrid = (GridView) view.findViewById(R.id.feedGrid);
        mNoContent = view.findViewById(R.id.noContent);
        mLoadingView = view.findViewById(R.id.loading_view);
        return view;
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
    }

    private void setLoadingFeedUI(boolean loading) {
        if(loading) {
            mLoadingView.setVisibility(View.VISIBLE);
            mNoContent.setVisibility(View.GONE);
            mGrid.setVisibility(View.GONE);
        }else{
            mLoadingView.setVisibility(View.GONE);
        }
    }

    private void refreshFeedContent() {
        setLoadingFeedUI(false);
        mNoContent.setVisibility(View.GONE);
        mGrid.setVisibility(View.VISIBLE);

        if (mAdapter == null) {
            mAdapter = new FeedAdapter(getActivity(), mFeed);
            mGrid.setAdapter(mAdapter);
        } else
            mAdapter.updateFeed(mFeed);

        mAdapter.setOnClickListener(FeedFragment.this);

        mGrid.setOnScrollListener(new AbsListView.OnScrollListener() {
            int currentVisibleItemCount = 0;
            int preLast = 0;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                this.currentVisibleItemCount = visibleItemCount;
                final int lastItem = firstVisibleItem + visibleItemCount;
                if (lastItem == totalItemCount - 1) {
                    if (preLast != lastItem) { //to avoid multiple calls for last item
                        PkRSS.with(getActivity())
                                .load(mCategory.getUrl())
                                .nextPage()
                                .callback(FeedFragment.this)
                                .async();
                        preLast = lastItem;
                    }
                }
            }
        });
    }

    @Override
    public void onClick(Article article) {
        Activity activity = getActivity();
        Intent i = new Intent(activity, NoMenuWebViewActivity.class);
        i.putExtra(NoMenuWebViewActivity.EXTRA_URL, article.getSource().toString());
        activity.startActivity(i);
    }


    @Override
    public void OnPreLoad() {
    }

    @Override
    public void OnLoaded(List<Article> newArticles) {
        mFeed = PkRSS.with(getActivity()).get(mCategory.getUrl());
        refreshFeedContent();
    }

    @Override
    public void OnLoadFailed() {
        mErrorMessageView.show(R.string.error_feed_loading);
        setLoadingFeedUI(false);
    }


    public static FeedFragment newInstance() {
        FeedFragment mFragment = new FeedFragment();
        return mFragment;
    }

    @Subscribe
    public void onUserSelectedFeedSource(UserSelectedFeedSource event) {
        mCategory = event.getCategory();
        PkRSS.with(getActivity()).load(mCategory.getUrl()).skipCache().callback(this).async();
        setLoadingFeedUI(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mDelegate = (InjectableFragmentDelegate) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mDelegate = null;
    }
}