package com.chatwing.whitelabel.listeners;

import android.widget.AbsListView;

/**
 * Created by nguyenthanhhuy on 12/21/13.
 */
public abstract class EndlessOnScrollListener implements AbsListView.OnScrollListener {
    public enum Direction {
        UP,
        DOWN
    }

    private int mVisibleThreashold;
    private Direction mDirection;

    public EndlessOnScrollListener(int visibleThreashold, Direction direction) {
        mVisibleThreashold = visibleThreashold;
        mDirection = direction;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // Don't take any action on changed
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

        if (!isLoading()
                && totalItemCount - getHeaderCount() > 0) { //Prevent loadMore when there is header
            boolean shouldLoadMore;
            if (mDirection == Direction.DOWN) {
                shouldLoadMore = firstVisibleItem + visibleItemCount >= totalItemCount - getHeaderCount() - mVisibleThreashold;
            } else {
                shouldLoadMore = firstVisibleItem <= mVisibleThreashold;
            }
            if (shouldLoadMore) {
                loadMoreResults();
            }
        }
    }

    protected abstract int getHeaderCount();

    public abstract boolean isLoading();

    public abstract void loadMoreResults();
}
