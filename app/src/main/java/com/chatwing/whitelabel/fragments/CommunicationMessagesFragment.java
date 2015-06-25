/*
 * Copyright (C) 2014 ChatWing
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chatwing.whitelabel.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.adapters.EmoticonPackagesAdapter;
import com.chatwing.whitelabel.events.AccessTokenExpiredEvent;
import com.chatwing.whitelabel.events.AppendEmoticonEvent;
import com.chatwing.whitelabel.events.CreateMessageEvent;
import com.chatwing.whitelabel.events.CurrentChatBoxEvent;
import com.chatwing.whitelabel.events.CurrentCommunicationEvent;
import com.chatwing.whitelabel.events.CurrentConversationEvent;
import com.chatwing.whitelabel.events.InvalidIdentityEvent;
import com.chatwing.whitelabel.events.MessageEvent;
import com.chatwing.whitelabel.events.UserUnauthenticatedEvent;
import com.chatwing.whitelabel.interfaces.ChatWingJSInterface;
import com.chatwing.whitelabel.managers.ApiManager;
import com.chatwing.whitelabel.managers.UserManager;
import com.chatwing.whitelabel.parsers.BBCodePair;
import com.chatwing.whitelabel.parsers.BBCodeParser;
import com.chatwing.whitelabel.pojos.ChatBox;
import com.chatwing.whitelabel.pojos.Emoticon;
import com.chatwing.whitelabel.pojos.Message;
import com.chatwing.whitelabel.pojos.User;
import com.chatwing.whitelabel.pojos.errors.CreateMessageParamsError;
import com.chatwing.whitelabel.services.CreateMessageIntentService;
import com.chatwing.whitelabel.utils.LogUtils;
import com.chatwing.whitelabel.validators.PermissionsValidator;
import com.chatwing.whitelabel.views.BBCodeEditText;
import com.chatwing.whitelabel.views.ErrorMessageView;
import com.github.kevinsawicki.http.HttpRequest;
import com.squareup.otto.Bus;
import com.viewpagerindicator.TabPageIndicator;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Created by cuongthai on 18/07/2014.
 */
public abstract class CommunicationMessagesFragment extends Fragment {
    @Inject
    Bus mBus;
    @Inject
    InputMethodManager mInputMethodManager;
    @Inject
    Provider<Typeface> mIconicTypefaceProvider;
    @Inject
    ErrorMessageView mErrorMessageView;
    @Inject
    UserManager mUserManager;
    @Inject
    ChatWingJSInterface mFayeJsInterface;

    protected WebView mWebview;
    private View mBBCodeControlsContainer;
    private View mStickerContainer;
    private Map<BBCodeParser.BBCode, Button> mBBCodeControls;
    protected View newContentBtn;
    protected View newEmoBtn;
    protected Delegate mDelegate;
    private boolean mSyncingBBCodeControls;
    private BBCodeEditText mCommunicationBoxEditText;
    private View sendBtn;
    private View mComposeContainer;
    private boolean showingBottomContainer;


    private EmoticonPackagesAdapter adapter;
    private ViewPager emoticonsPager;
    private TabPageIndicator indicator;


    public boolean isShowingBBControls() {
        return mBBCodeControlsContainer.getVisibility() == View.VISIBLE;
    }


    public boolean isShowingStickers() {
        return mStickerContainer.getVisibility() == View.VISIBLE;
    }

    public void handleComposeView(ChatBox chatBox) {
        if (!chatBox.isReadOnly()
                || chatBox.isAdmin()
                || mUserManager.userHasPermission(chatBox, PermissionsValidator.Permission.SEND_MESSAGE)) {
            mComposeContainer.setVisibility(View.VISIBLE);
        } else {
            mComposeContainer.setVisibility(View.GONE);
        }
    }

    public static interface Delegate extends InjectableFragmentDelegate {
        void showColorPickerDialogFragment(BBCodeParser.BBCode code);

        void showNewContentFragment();

        void showPasswordDialogFragment();

        void inject(BBCodeEditText mCommunicationBoxEditText);
    }

    public CommunicationMessagesFragment() {
    }

    protected abstract Message constructMessage(User user, String content,
                                                long createdDate, String randomKey,
                                                Message.Status status);

    protected abstract boolean canHandle(MessageEvent event);

    protected abstract boolean isInCurrentCommunicationBox(MessageEvent event);


    protected abstract boolean hasCurrentCommunication();

    public void onContextMenuClosed(Menu menu) {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_messageview, container, false);
    }

    private void setupWebView() {
        LogUtils.v("Test Chatbox not display, setupWebView " + mWebview);

        mWebview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                try {
                    view.getContext().startActivity(
                            new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                //TODO should be when page start rendering chatbox
                LogUtils.v("Test Chatbox not display, Loading page DONE "+url);
                if(!"about:blank".equals(url)) {
                    mBus.post(new CurrentChatBoxEvent(CurrentCommunicationEvent.Status.UI_LOADED, null));
                    mBus.post(new CurrentConversationEvent(CurrentCommunicationEvent.Status.UI_LOADED, null));
                }
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                LogUtils.v("Loading page Oh no! " + description);
            }
        });

        mWebview.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onConsoleMessage(String message, int lineNumber, String sourceID) {
                LogUtils.v(message + " -- From line " + lineNumber + " of " + sourceID);
                super.onConsoleMessage(message, lineNumber, sourceID);
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                LogUtils.v("Loading page " + newProgress + ":" + mWebview);
            }
        });
        WebSettings webSettings = mWebview.getSettings();
        webSettings.setAppCacheEnabled(false);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            webSettings.setDatabasePath("/data/data/" + getActivity().getPackageName() + "/databases/");
        }

        mWebview.addJavascriptInterface(mFayeJsInterface,
                ChatWingJSInterface.CHATWING_JS_NAME);


    }

    protected boolean handleException(Exception exception) {
        if (exception instanceof HttpRequest.HttpRequestException) {
            mErrorMessageView.show(R.string.error_network_connection);
            return true;
        }
        return false;
    }

    protected void onCreateMessageEvent(CreateMessageEvent event) {
        Exception exception = event.getException();
        if (exception != null &&
                exception instanceof ApiManager.CreateMessageException) {
            ApiManager.CreateMessageException createMessageException = (ApiManager.CreateMessageException) exception;
            handle(createMessageException.getCreateMessageParamsError(),
                    createMessageException.getCommunicationMessage());
            return;
        }
        //Handle general errors
        if (onMessageEvent(event, getString(R.string.error_while_sending_message))) {
            return;
        }

        //Yahooo! no error, get message from REST response and add to view.
        addNewMessage(event.getResponse().getMessage());
    }

    public abstract boolean addNewMessage(Message newMessage);

    /**
     * Checks and pre-processes a {@link com.chatwing.whitelabel.events.MessageEvent} for common cases.
     *
     * @param event the event to be checked and processed.
     * @return {@code true} if the event is already checked, processed and can
     * be ignored. {@code false} otherwise and the event processing can proceed.
     */
    protected boolean onMessageEvent(MessageEvent event, String detailMessasge) {
        if (!canHandle(event)) {
            return true;
        }

        Exception exception = event.getException();
        //Handle known exception
        if (exception instanceof ApiManager.UserUnauthenticatedException) {
            mBus.post(new UserUnauthenticatedEvent());
            return true;
        }

        if (exception instanceof ApiManager.InvalidIdentityException) {
            mBus.post(new InvalidIdentityEvent((ApiManager.InvalidIdentityException) event.getException()));
            return true;
        }

        if (exception instanceof ApiManager.InvalidAccessTokenException) {
            mBus.post(new AccessTokenExpiredEvent());
            return true;
        }

        //Handle unknown exception
        if (exception != null) {
            mErrorMessageView.show(exception, detailMessasge);
            return true;
        }

        // Ignore this event (return true) if it is not related to current communication box.
        return !isInCurrentCommunicationBox(event);
    }

    /**
     * Handles a {@link com.chatwing.whitelabel.pojos.errors.CreateMessageParamsError} which is returned from server.
     *
     * @param error           required.
     * @param originalMessage optional.
     */
    protected void handle(CreateMessageParamsError error, Message originalMessage) {
        String type = error.getType();
        String toastMessage;
        boolean removeOriginalMessage = true;
        if (type.equals(CreateMessageParamsError.TYPE_EMPTY_MESSAGE)) {
            // This shouldn't happen because the message was checked in sendMessage
            // before the request to server is made.
            toastMessage = getString(R.string.error_empty_message);
        } else if (type.equals(CreateMessageParamsError.TYPE_LONG_MESSAGE)) {
            // TODO: may retry with the first 512 chars or split the message into 2.
            toastMessage = getString(R.string.error_too_long_message);
            if (originalMessage != null) {
                String content = originalMessage.getContent();
                mCommunicationBoxEditText.setText(content);
                mCommunicationBoxEditText.setSelection(content.length());
            }
        } else if (type.equals(CreateMessageParamsError.TYPE_ILLEGAL_INPUT)) {
            toastMessage = getString(R.string.error_illegal_input);
        } else if (type.equals(CreateMessageParamsError.TYPE_BLOCK)) {
            toastMessage = error.getErrorMessage();
            // even if the user is unable to send the message,
            // the message is still appended to the message list (but not stored in the database)
            removeOriginalMessage = true;
        } else {
            toastMessage = getString(R.string.error_unknown);
            // Unknown error, just leave the message in the list with a
            // "Failed" label.
            removeOriginalMessage = false;
            LogUtils.e("Unknown error: " + type);
        }

        mErrorMessageView.show(toastMessage);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDelegate.inject(this);

        emoticonsPager = (ViewPager) view.findViewById(R.id.pager);
        emoticonsPager.setAdapter(new EmoticonPackagesAdapter(getActivity().getSupportFragmentManager(),
                new HashMap<String, Emoticon[]>()));
        indicator = (TabPageIndicator) view.findViewById(R.id.indicator);
        indicator.setViewPager(emoticonsPager);

        mWebview = (WebView) view.findViewById(R.id.webView);
        setupWebView();

        newContentBtn = view.findViewById(R.id.btn_new_content);
        newContentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDelegate.showNewContentFragment();
            }
        });


        newEmoBtn = view.findViewById(R.id.btn_emo);
        newEmoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isShowingStickers()) {
                    hideStickerContainer();
                } else {
                    showStickerContainer();
                }
            }
        });


        mComposeContainer = view.findViewById(R.id.chat_box_container);
        mCommunicationBoxEditText = (BBCodeEditText) view.findViewById(R.id.chat_box);
        mDelegate.inject(mCommunicationBoxEditText);

        // BBCode controls
        mBBCodeControlsContainer = view.findViewById(R.id.bbcode_controls_container);
        mStickerContainer = view.findViewById(R.id.emoticons_container);
        hideBBCodeControls();
        hideStickerContainer();
        mBBCodeControlsContainer.findViewById(R.id.cancel_bbcode_controls)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        hideBBCodeControls();
                    }
                });
        view.findViewById(R.id.cancel_stickers)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        hideStickerContainer();
                    }
                });
        mBBCodeControls = new TreeMap<BBCodeParser.BBCode, Button>();
        mBBCodeControls.put(
                BBCodeParser.BBCode.BOLD,
                (Button) mBBCodeControlsContainer.findViewById(R.id.btn_bbcode_bold));
        mBBCodeControls.put(
                BBCodeParser.BBCode.ITALIC,
                (Button) mBBCodeControlsContainer.findViewById(R.id.btn_bbcode_italic));
        mBBCodeControls.put(
                BBCodeParser.BBCode.UNDERLINE,
                (Button) mBBCodeControlsContainer.findViewById(R.id.btn_bbcode_underline));
        mBBCodeControls.put(
                BBCodeParser.BBCode.STRIKE_THROUGH,
                (Button) mBBCodeControlsContainer.findViewById(R.id.btn_bbcode_strike_through));
        mBBCodeControls.put(
                BBCodeParser.BBCode.COLOR,
                (Button) mBBCodeControlsContainer.findViewById(R.id.btn_bbcode_color));
        mBBCodeControls.put(
                BBCodeParser.BBCode.BACKGROUND_COLOR,
                (Button) mBBCodeControlsContainer.findViewById(R.id.btn_bbcode_background_color));
        mBBCodeControls.put(
                BBCodeParser.BBCode.IMAGE,
                (Button) mBBCodeControlsContainer.findViewById(R.id.btn_bbcode_image));
        mBBCodeControls.put(
                BBCodeParser.BBCode.VIDEO,
                (Button) mBBCodeControlsContainer.findViewById(R.id.btn_bbcode_video));
        mBBCodeControls.put(
                BBCodeParser.BBCode.URL,
                (Button) mBBCodeControlsContainer.findViewById(R.id.btn_bbcode_url));
        mBBCodeControls.put(
                BBCodeParser.BBCode.EMAIL,
                (Button) mBBCodeControlsContainer.findViewById(R.id.btn_bbcode_email));
        configBBCodeControls();

        //Setup typeface for format buttons
        Typeface iconicTypeface = mIconicTypefaceProvider.get();
        for (final BBCodeParser.BBCode code : mBBCodeControls.keySet()) {
            Button button = mBBCodeControls.get(code);
            if (button.getTypeface() == null
                    || !button.getTypeface().equals(iconicTypeface)) {
                button.setTypeface(iconicTypeface);
            }
        }

        // Compose view
        mCommunicationBoxEditText = (BBCodeEditText) view.findViewById(R.id.chat_box);
        sendBtn = view.findViewById(R.id.btn_send);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        // Set style to chat box edit text and sync BBCode controls
        mCommunicationBoxEditText.clearAllBBCodes();
        Set<BBCodePair> pairs = mUserManager.getStyle();
        if (pairs != null && !pairs.isEmpty()) {
            for (BBCodePair pair : pairs) {
                mCommunicationBoxEditText.append(pair);
            }
        }
        syncBBCodeControlsState();
    }

    @Override
    public void onResume() {
        super.onResume();
        mBus.register(this);
    }

    public void onAppendEmoticonEvent(AppendEmoticonEvent event) {
        mCommunicationBoxEditText.append(event.getEmoticon());
        mCommunicationBoxEditText.requestFocus();
    }

    @Override
    public void onPause() {
        super.onPause();
        mBus.unregister(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.v("Test Chatbox not display, destroy webview");

        if (mWebview != null) {
            mWebview.destroy();
            mWebview = null;
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mDelegate = (Delegate) activity;

    }

    private void hideBBCodeControls() {
        mBBCodeControlsContainer.setVisibility(View.GONE);
    }

    private void hideStickerContainer() {
        mStickerContainer.setVisibility(View.GONE);
    }

    public void showBBCodeControls() {
        if (showingBottomContainer) {
            hideBBCodeControls();
            hideStickerContainer();
        }
        showingBottomContainer = true;
        mBBCodeControlsContainer.setVisibility(View.VISIBLE);
    }

    public void showStickerContainer() {
        if (showingBottomContainer) {
            hideBBCodeControls();
            hideStickerContainer();
        }
        showingBottomContainer = true;
        mStickerContainer.setVisibility(View.VISIBLE);
    }

    private void configBBCodeControls() {
        for (final BBCodeParser.BBCode code : mBBCodeControls.keySet()) {
            Button button = mBBCodeControls.get(code);
            // Config listener accordingly
            if (button instanceof ToggleButton) {
                // If it is a toggle button, set checked change listener.
                // When unchecked, the code is removed.
                ToggleButton toggle = (ToggleButton) button;
                if (code == BBCodeParser.BBCode.COLOR
                        || code == BBCodeParser.BBCode.BACKGROUND_COLOR) {
                    // When checked, show color picker.
                    toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            // Check mSyncingBBCodeControls to avoid recursion.
                            if (!mSyncingBBCodeControls) {
                                if (isChecked) {
                                    mDelegate.showColorPickerDialogFragment(code);
                                    // Invalidate the checked property now,
                                    // because user can cancel the action later.
                                    // The toggle will be updated later when user
                                    // confirmed the action.
                                    buttonView.setChecked(false);
                                } else {
                                    removeBBCode(code);
                                }
                            }
                        }
                    });
                } else {
                    // When checked, append the code with an empty value.
                    toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            // Check mSyncingBBCodeControls to avoid recursion.
                            if (!mSyncingBBCodeControls) {
                                if (isChecked) {
                                    appendBBCode(new BBCodePair(code));
                                } else {
                                    removeBBCode(code);
                                }
                            }
                        }
                    });
                }
            } else {
                // It is a normal button, set on click listener. When clicked,
                // append the code with empty value.
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        appendBBCode(new BBCodePair(code));
                    }
                });
            }
        }
    }

    public void appendBBCode(BBCodePair pair) {
        if (mCommunicationBoxEditText.append(pair)) {
            showKeyboard();
            syncBBCodeControlState(pair.getCode());
            saveBBCodesIfNeeded();
        }
    }

    private void removeBBCode(BBCodeParser.BBCode code) {
        if (mCommunicationBoxEditText.remove(code)) {
            syncBBCodeControlState(code);
            saveBBCodesIfNeeded();
        }
    }

    private void showKeyboard() {
        mInputMethodManager.showSoftInput(mCommunicationBoxEditText, InputMethodManager.SHOW_IMPLICIT);
    }

    /**
     * Syncs state of the {@link BBCodeParser.BBCode} in {@link #mCommunicationBoxEditText}
     * with BBCode controls. Only {@link ToggleButton}s are supported by now.
     * <p/>
     * If the code is used in {@link #mCommunicationBoxEditText}, the corresponding
     * {@link ToggleButton} will be checked. Otherwise, it will be unchecked.
     *
     * @param code the code to be synced.
     */
    private void syncBBCodeControlState(BBCodeParser.BBCode code) {
        View control = mBBCodeControls.get(code);
        if (!(control instanceof ToggleButton)) {
            return;
        }

        mSyncingBBCodeControls = true;
        ((ToggleButton) control).setChecked(mCommunicationBoxEditText.contains(code));
        mSyncingBBCodeControls = false;
    }

    private void saveBBCodesIfNeeded() {
        if (mUserManager.shouldRememberPreviousStyle()) {
            Set<BBCodePair> pairs = new TreeSet<BBCodePair>(mCommunicationBoxEditText.getAllBBCodes());
            mUserManager.saveStyle(pairs);
        }
    }

    //////////////////////////////////////////////////////////////
    // Instance methods
    //////////////////////////////////////////////////////////////
    private void sendMessage() {
        if (!mUserManager.userCanSendMessage()) {
            mErrorMessageView.show(R.string.error_required_login);
            return;
        }
        User user = mUserManager.getCurrentUser();

        String content = mCommunicationBoxEditText.getFullText();
        if (TextUtils.isEmpty(content)) {
            // Nothing to send
            return;
        }

        content = autoAppendBBCode(content);
        LogUtils.v("autoAppendBBCode " + content);

        if (!hasCurrentCommunication()) {
            // No current communication box to send new message to
            return;
        }

        long createdDate = System.currentTimeMillis();
        Message newMessage = constructMessage(
                user,
                content,
                createdDate,
                null,
                Message.Status.SENDING);
        newMessage.setSendingDate(System.currentTimeMillis());

        addNewMessage(newMessage);
        mCommunicationBoxEditText.setText("");
        if (!user.getProfile().shouldRememberPreviousStyle()) {
            mCommunicationBoxEditText.clearAllBBCodes();
            syncBBCodeControlsState();
        }

        // Send task to CreateMessageIntentService to make a async request.
        Intent i = new Intent(getActivity(), CreateMessageIntentService.class);
        i.putExtra(CreateMessageIntentService.EXTRA_MESSAGE, newMessage);
        i.putExtra(CreateMessageIntentService.EXTRA_SENDING_DATE, newMessage.getSendingDate());
        getActivity().startService(i);
    }

    private String autoAppendBBCode(String content) {
        return content.replaceAll("(http(s?):/)(/[^/]+)+" + "\\.(?:jpg|gif|png)", "[img]$0[/img]");
    }

    private void syncBBCodeControlsState() {
        for (BBCodeParser.BBCode code : mBBCodeControls.keySet()) {
            syncBBCodeControlState(code);
        }
    }

    protected void loadEmoticons(Emoticon[] emoticons) {
        //There is a case multiple emoticons code mapped to the same link
        //It's used in case of rendering emoticons code to image but since we let js side render the view
        //we dont need to care about it.
        Set<Emoticon> emoticonSet = new TreeSet<Emoticon>(new Comparator<Emoticon>() {
            @Override
            public int compare(Emoticon emoticon, Emoticon emoticon2) {
                return emoticon.getImage().compareTo(emoticon2.getImage());
            }
        });
        emoticonSet.addAll(Arrays.asList(emoticons));
        Map<String, Emoticon[]> packages = new HashMap<String, Emoticon[]>();
        packages.put("Emoticons", emoticonSet.toArray(new Emoticon[emoticonSet.size()]));

        adapter = new EmoticonPackagesAdapter(getActivity().getSupportFragmentManager(), packages);
        emoticonsPager.setAdapter(adapter);
        indicator.setViewPager(emoticonsPager);
        indicator.notifyDataSetChanged();
    }

}
