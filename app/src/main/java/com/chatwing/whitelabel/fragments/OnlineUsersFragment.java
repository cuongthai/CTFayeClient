package com.chatwing.whitelabel.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.chatwing.whitelabel.Constants;
import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.adapters.OnlineUsersAdapter;
import com.chatwing.whitelabel.events.CurrentChatBoxEvent;
import com.chatwing.whitelabel.events.LoadOnlineUsersSuccessEvent;
import com.chatwing.whitelabel.managers.UserManager;
import com.chatwing.whitelabel.pojos.BaseUser;
import com.chatwing.whitelabel.pojos.ChatBox;
import com.chatwing.whitelabel.pojos.CommunicationBoxJson;
import com.chatwing.whitelabel.pojos.Filter;
import com.chatwing.whitelabel.pojos.OnlineUser;
import com.chatwing.whitelabel.pojos.OnlineUserProfile;
import com.chatwing.whitelabel.pojos.User;
import com.chatwing.whitelabel.pojos.params.CreateConversationParams;
import com.chatwing.whitelabel.views.QuickMessageView;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

/**
 * Author: Huy Nguyen
 * Date: 7/25/13
 * Time: 9:29 AM
 */
public class OnlineUsersFragment extends ListFragment implements AdapterView.OnItemClickListener {
    public static interface OnlineUsersFragmentDelegate extends InjectableFragmentDelegate {
        public void createConversation(CreateConversationParams.SimpleUser simpleUser);
    }

    private OnlineUsersFragmentDelegate mDelegate;
    @Inject
    OnlineUsersAdapter mListAdapter;
    @Inject
    Bus mBus;
    @Inject
    UserManager mUserManager;
    @Inject
    QuickMessageView mQuickMessageView;

    private FrameLayout mRootView;
    private TextView mEmptyTextView;

    public OnlineUsersFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mDelegate = (OnlineUsersFragmentDelegate) activity;
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
        getListView().setOnItemClickListener(this);
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
        Set<OnlineUser> onlineUsers = event.getOnlineUsers();
        addYourSelf(onlineUsers);
        mListAdapter.setNotifyOnChange(false);
        mListAdapter.clear();
        mListAdapter.addAllData(onlineUsers);
        mListAdapter.notifyDataSetChanged();
    }

    private void addYourSelf(Set<OnlineUser> onlineUsers) {
        User currentUser = mUserManager.getCurrentUser();
        if (currentUser == null) return;

        onlineUsers.add(new OnlineUser(
                true,
                currentUser.getId(),
                currentUser.getLoginId(),
                currentUser.getLoginType(),
                new OnlineUserProfile(currentUser.getProfile().getName())));

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
        User currentUser = mUserManager.getCurrentUser();
        OnlineUser onlineUser = mListAdapter.getItem(pos);
        if (currentUser == null) {
            mQuickMessageView.show(R.string.error_required_login);
            return;
        }
        if (!onlineUser.isAuthenticated()) {
            return;
        }

        if (BaseUser.computeIdentifier(onlineUser.getLoginId(), onlineUser.getLoginType()).equals(currentUser.getIdentifier())) {
            //Click on myself
            return;
        }

        if(Constants.TYPE_GUEST.equals(onlineUser.getLoginType())){
            return;
        }

        mDelegate.createConversation(new CreateConversationParams.SimpleUser(onlineUser.getLoginId(), onlineUser.getLoginType()));
    }
}
