package com.chatwing.whitelabel.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.chatwing.whitelabel.Constants;
import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.views.DynamicTextLoginButton;
import com.chatwingsdk.events.internal.UserAuthenticationEvent;
import com.chatwingsdk.fragments.InjectableFragmentDelegate;
import com.chatwingsdk.pojos.params.oauth.AuthenticationParams;
import com.chatwingsdk.pojos.params.oauth.OAuth2Params;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.SessionState;
import com.squareup.otto.Bus;

import javax.inject.Inject;

/**
 * Created by nguyenthanhhuy on 4/14/14.
 */
public class AuthenticateFragment extends Fragment implements View.OnClickListener {

    public interface Delegate extends InjectableFragmentDelegate {
        public void login(String loginType);

        public Info getInfo();
    }

    public static class Info {
        public final int mFbAuthText;
        public final int mGooglePlusAuthText;
        public final int mTumblrAuthText;
        public final int mTwitterAuthText;
        public final int mYahooAuthText;
        public final int mGuestAuthText;

        public Info(int fbAuthText, int googlePlusAuthText, int tumblrAuthText,
                    int twitterAuthText, int yahooAuthText, int guestAuthText) {
            mFbAuthText = fbAuthText;
            mGooglePlusAuthText = googlePlusAuthText;
            mTumblrAuthText = tumblrAuthText;
            mTwitterAuthText = twitterAuthText;
            mYahooAuthText = yahooAuthText;
            mGuestAuthText = guestAuthText;
        }
    }

    @Inject
    Bus mBus;
    private Delegate mDelegate;
    private Session.StatusCallback mSessionStatusCallback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            UserAuthenticationEvent event = null;
            if (exception != null) {
                if (exception instanceof FacebookOperationCanceledException) {
                    event = UserAuthenticationEvent.canceledEvent("");
                } else {
                    event = UserAuthenticationEvent.failedEvent("", exception);
                }
            } else if (state.isOpened()) {
                AuthenticationParams oAuth2Params
                        = new OAuth2Params(Constants.TYPE_FACEBOOK,
                        session.getAccessToken());
                event = UserAuthenticationEvent.succeedEvent("", oAuth2Params);
            }
            if (event != null) {
                mBus.post(event);
            }
        }
    };

    public AuthenticateFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mDelegate = (Delegate) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_authenticate, container, false);
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {
        Info info = mDelegate.getInfo();
        DynamicTextLoginButton fbLoginBtn = (DynamicTextLoginButton) v.findViewById(R.id.btn_authenticate_fb);
        Button twitterLoginBtn = (Button) v.findViewById(R.id.btn_authenticate_twitter);
        Button yahooLoginBtn = (Button) v.findViewById(R.id.btn_authenticate_yahoo);
        Button googlePlusLoginBtn = (Button) v.findViewById(R.id.btn_authenticate_google_plus);
        Button tumblrLoginBtn = (Button) v.findViewById(R.id.btn_authenticate_tumblr);
        Button guestLoginBtn = (Button) v.findViewById(R.id.btn_authenticate_guest);

        fbLoginBtn.setReadPermissions(Constants.FB_READ_PERMISSIONS);
        fbLoginBtn.setFragment(this);
        fbLoginBtn.setSessionStatusCallback(mSessionStatusCallback);
        fbLoginBtn.setLoginText(getString(info.mFbAuthText));

        googlePlusLoginBtn.setOnClickListener(this);
        googlePlusLoginBtn.setText(info.mGooglePlusAuthText);

        tumblrLoginBtn.setOnClickListener(this);
        tumblrLoginBtn.setText(info.mTumblrAuthText);

        twitterLoginBtn.setOnClickListener(this);
        twitterLoginBtn.setText(info.mTwitterAuthText);

        yahooLoginBtn.setOnClickListener(this);
        yahooLoginBtn.setText(info.mYahooAuthText);

        if (info.mGuestAuthText != 0) {
            guestLoginBtn.setVisibility(View.VISIBLE);
            guestLoginBtn.setOnClickListener(this);
            guestLoginBtn.setText(info.mGuestAuthText);
        } else {
            guestLoginBtn.setVisibility(View.GONE);
        }

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mDelegate.inject(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session session = Session.getActiveSession();
        if (session != null) {
            session.onActivityResult(getActivity(), requestCode,
                    resultCode, data);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mDelegate = null;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_authenticate_twitter:
                mDelegate.login(Constants.TYPE_TWITTER);
                break;
            case R.id.btn_authenticate_yahoo:
                mDelegate.login(Constants.TYPE_YAHOO);
                break;
            case R.id.btn_authenticate_google_plus:
                mDelegate.login(Constants.TYPE_GOOGLE);
                break;
            case R.id.btn_authenticate_tumblr:
                mDelegate.login(Constants.TYPE_TUMBLR);
                break;
            case R.id.btn_authenticate_guest:
                mDelegate.login(Constants.TYPE_GUEST);
            default:
                break;
        }
    }
}
