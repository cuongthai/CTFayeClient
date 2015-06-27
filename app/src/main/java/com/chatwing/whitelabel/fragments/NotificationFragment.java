package com.chatwing.whitelabel.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.events.SubscriptionStatusEvent;
import com.chatwing.whitelabel.managers.ApiManager;
import com.chatwing.whitelabel.managers.UserManager;
import com.chatwing.whitelabel.services.NotificationStatusIntentService;
import com.chatwing.whitelabel.services.UpdateNotificationSettingsService;
import com.chatwing.whitelabel.views.QuickMessageView;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.Map;

import javax.inject.Inject;


/**
 * Created by steve on 24/01/2015.
 */
public class NotificationFragment extends DialogFragment {
    @Inject
    UserManager mUserManager;
    @Inject
    Bus mBus;
    @Inject
    QuickMessageView messageView;
    private static final String CHATBOX_ID = "CHATBOX_ID";
    private static final String CONVERSATION_ID = "CONVERSATION_ID";

    private CompoundButton emailSwitchBtn;
    private CompoundButton pushSwitchBtn;
    private InjectableFragmentDelegate mDelegate;
    private View mContent;
    private View mLoading;
    private View mUpdateBtn;
    private int chatboxID;
    private String conversationID;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().setTitle(R.string.title_activity_notification);
        return inflater.inflate(R.layout.fragment_notification_settings, container, false);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AppCompatDialog(getActivity(), R.style.Theme_ChatWing_AlertDialog);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mDelegate = (InjectableFragmentDelegate) activity;
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

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDelegate.inject(this);

        chatboxID = getArguments().getInt(CHATBOX_ID, 0);
        conversationID = getArguments().getString(CONVERSATION_ID);
        Intent i = new Intent(getActivity(), NotificationStatusIntentService.class);
        i.putExtra(NotificationStatusIntentService.CHATBOX_ID, chatboxID);
        i.putExtra(NotificationStatusIntentService.CONVERSATION_ID, conversationID);
        getActivity().startService(i);

        mContent = view.findViewById(R.id.content);
        mLoading = view.findViewById(R.id.loading);
        emailSwitchBtn = (CompoundButton) view.findViewById(R.id.emailSwitch);
        pushSwitchBtn = (CompoundButton) view.findViewById(R.id.pushSwitch);
        setVisibleContent(false);

        view.findViewById(R.id.cancelBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        mUpdateBtn = view.findViewById(R.id.updateBtn);

    }

    private void setVisibleContent(boolean showContent) {
        if (showContent) {
            mContent.setVisibility(View.VISIBLE);
            mLoading.setVisibility(View.INVISIBLE);
        } else {
            mContent.setVisibility(View.INVISIBLE);
            mLoading.setVisibility(View.VISIBLE);
        }
    }

    @Subscribe
    public void onSubscriptionStatusEvent(SubscriptionStatusEvent event) {
        if (event.getStatus() == SubscriptionStatusEvent.Status.SUCCEED) {
            loadContent(event.getSubscriptionResponse().getData());
            setVisibleContent(true);
        } else if (event.getStatus() == SubscriptionStatusEvent.Status.FAILED) {
            dismiss();
            if(event.getException() instanceof ApiManager.InvalidIdentityException){
                messageView.show(R.string.error_required_login_except_guest);
            }
        }
    }

    private void loadContent(Map<String, Boolean> data) {
        final boolean isConversation = conversationID != null ? true : false;
        final boolean isEmailOn = data.get("email");
        final boolean isPushOn = data.get("push");

        emailSwitchBtn.setChecked(isEmailOn);
        pushSwitchBtn.setChecked(isPushOn);

        mUpdateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ((emailSwitchBtn.isChecked()
                        ^ isEmailOn)) {
                    Intent i = new Intent(getActivity(), UpdateNotificationSettingsService.class);
                    i.setAction(emailSwitchBtn.isChecked()
                            ? UpdateNotificationSettingsService.ACTION_SUBSCRIBE
                            : UpdateNotificationSettingsService.ACTION_UNSUBSCRIBE);
                    if (!isConversation) {
                        i.putExtra(UpdateNotificationSettingsService.CHATBOX_ID, chatboxID);
                    } else {
                        i.putExtra(UpdateNotificationSettingsService.CONVERSATION_ID, conversationID);
                    }
                    i.putExtra(UpdateNotificationSettingsService.TARGET, UpdateNotificationSettingsService.TARGET_EMAIL);
                    getActivity().startService(i);
                }

                if ((pushSwitchBtn.isChecked()
                        ^ isPushOn)) {
                    Intent i = new Intent(getActivity(), UpdateNotificationSettingsService.class);
                    i.setAction(pushSwitchBtn.isChecked()
                            ? UpdateNotificationSettingsService.ACTION_SUBSCRIBE
                            : UpdateNotificationSettingsService.ACTION_UNSUBSCRIBE);
                    if (!isConversation) {
                        i.putExtra(UpdateNotificationSettingsService.CHATBOX_ID, chatboxID);
                    } else {
                        i.putExtra(UpdateNotificationSettingsService.CONVERSATION_ID, conversationID);
                    }
                    i.putExtra(UpdateNotificationSettingsService.TARGET, UpdateNotificationSettingsService.TARGET_PUSH);
                    getActivity().startService(i);
                }

                dismiss();
            }
        });

    }

    public static NotificationFragment newInstance(int chatboxID) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(CHATBOX_ID, chatboxID);
        NotificationFragment notificationFragment = new NotificationFragment();
        notificationFragment.setArguments(bundle);
        return notificationFragment;
    }

    public static NotificationFragment newInstance(String conversationID) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(CONVERSATION_ID, conversationID);
        NotificationFragment notificationFragment = new NotificationFragment();
        notificationFragment.setArguments(bundle);
        return notificationFragment;
    }
}
