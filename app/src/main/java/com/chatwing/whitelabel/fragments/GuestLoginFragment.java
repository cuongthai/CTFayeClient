package com.chatwing.whitelabel.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.adapters.GuestAvatarAdapter;
import com.chatwing.whitelabel.events.UserAuthenticationEvent;
import com.chatwing.whitelabel.managers.ApiManager;
import com.chatwing.whitelabel.pojos.oauth.GuestOAuthParams;
import com.chatwing.whitelabel.views.CompatibleGridView;
import com.squareup.otto.Bus;

import javax.inject.Inject;

/**
 * Created by steve on 02/07/2014.
 */
public class GuestLoginFragment extends BaseFragment {
    @Inject
    protected GuestAvatarAdapter adapter;
    @Inject
    protected Bus mBus;

    private InjectableFragmentDelegate mDelegate;

    public GuestLoginFragment() {
    }

    @Override
    protected void onAttachToContext(Context context) {
        if (context instanceof InjectableFragmentDelegate) {
            mDelegate = (InjectableFragmentDelegate) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mDelegate = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_guest_login, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mDelegate.inject(this);
        final CompatibleGridView gridView = (CompatibleGridView) getView().findViewById(R.id.gridView);
        gridView.setAdapter(adapter);

        getView().findViewById(R.id.btn_login_guest).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int supportCheckedItemPosition = gridView.getSupportCheckedItemPosition();
                if (supportCheckedItemPosition == -1) return;
                EditText guestName = (EditText) getView().findViewById(R.id.guest_name);
                if (TextUtils.isEmpty(guestName.getText())) {
                    guestName.setError(getString(R.string.error_invalid_guest_name));
                    return;
                }

                guestName.setError(null);

                GuestOAuthParams params = new GuestOAuthParams(
                        guestName.getText().toString(),
                        String.format(ApiManager.SUBMITTED_GUEST_AVATAR_URL,
                                adapter.getAvatarAt(supportCheckedItemPosition)
                        ));
                UserAuthenticationEvent event
                        = UserAuthenticationEvent.succeedEvent("", params);
                mBus.post(event);
            }
        });
    }
}
