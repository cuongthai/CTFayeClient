package com.chatwing.whitelabel.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.adapters.OnlineUsersAdapter;
import com.chatwing.whitelabel.events.LoadOnlineUsersSuccessEvent;
import com.chatwing.whitelabel.managers.ApiManager;
import com.chatwingsdk.events.internal.CurrentChatBoxEvent;
import com.chatwingsdk.fragments.InjectableFragmentDelegate;
import com.chatwingsdk.managers.VolleyManager;
import com.chatwingsdk.pojos.ChatBox;
import com.chatwingsdk.pojos.CommunicationBoxJson;
import com.chatwingsdk.pojos.Filter;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Author: Huy Nguyen
 * Date: 7/25/13
 * Time: 9:29 AM
 */
public class OnlineUsersFragment extends ListFragment {

    private InjectableFragmentDelegate mDelegate;
    @Inject
    OnlineUsersAdapter mListAdapter;
    @Inject
    Bus mBus;
    private FrameLayout mRootView;
    private TextView mEmptyTextView;

    public OnlineUsersFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mDelegate = (InjectableFragmentDelegate) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_online_users,
                container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRootView = (FrameLayout) view.findViewById(android.R.id.content);
        mEmptyTextView = (TextView) view.findViewById(android.R.id.empty);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mDelegate.inject(this);
        setListAdapter(mListAdapter);
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
    public void onDetach() {
        super.onDetach();
        mDelegate = null;
    }

    ////////////////////////////////////////////////////////////
    // Instance methods
    ////////////////////////////////////////////////////////////
    @Subscribe
    public void onCurrentChatBoxChanged(CurrentChatBoxEvent event) {
        CurrentChatBoxEvent.Status status = event.getStatus();
        switch (status) {
            case REMOVED:
                mListAdapter.clear();
                mListAdapter.removeNameFilters();
                break;
            case LOADED:
            case UPDATED:
                updateChatBoxDetail(event.getChatbox());
                break;
        }
    }

    private void updateChatBoxDetail(ChatBox chatBox) {
        if (chatBox == null) {
            return;
        }

        List<Filter> filters = chatBox.getFilters();
        List<String> nameFilters = new ArrayList<String>();
        for (Filter f : filters) {
            if (f.isFilterName()) {
                nameFilters.add(f.getName());
            }
        }
        CommunicationBoxJson json = chatBox.getJson();
        mListAdapter.update(nameFilters, json.getUserListColor());

        mRootView.setBackgroundColor(json.getUserListBackgroundColor());


        mEmptyTextView.setTextColor(json.getUserListColor());
    }

    @Subscribe
    public void onLoadOnlineUsersSuccess(LoadOnlineUsersSuccessEvent event) {
        mListAdapter.setNotifyOnChange(false);
        mListAdapter.clear();
        mListAdapter.addAllData(event.getOnlineUsers());
        mListAdapter.notifyDataSetChanged();
    }
}
