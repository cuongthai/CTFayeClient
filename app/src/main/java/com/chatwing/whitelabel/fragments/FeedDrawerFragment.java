package com.chatwing.whitelabel.fragments;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.chatwing.whitelabel.Constants;
import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.adapters.FeedSourcesAdapter;
import com.chatwing.whitelabel.events.UserSelectedFeedSource;
import com.squareup.otto.Bus;

import javax.inject.Inject;

public class FeedDrawerFragment extends ListFragment {
    @Inject
    FeedSourcesAdapter mAdapter;
    @Inject
    Bus mBus;

    private NavigatableFragmentListener mListener;

    public FeedDrawerFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_feed_source, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        view.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.back(FeedDrawerFragment.this);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mBus.register(this);
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

        mAdapter.addAll(Constants.CATEGORIES);
        setListAdapter(mAdapter);
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        mBus.post(new UserSelectedFeedSource(mAdapter.getItem(position)));
    }

}
