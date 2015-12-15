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

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.adapters.CommunicationMessagesAdapter;
import com.chatwing.whitelabel.adapters.EmoticonPackagesAdapter;
import com.chatwing.whitelabel.events.AccessTokenExpiredEvent;
import com.chatwing.whitelabel.events.AppendEmoticonEvent;
import com.chatwing.whitelabel.events.CreateMessageEvent;
import com.chatwing.whitelabel.events.GotMessagesEvent;
import com.chatwing.whitelabel.events.InvalidIdentityEvent;
import com.chatwing.whitelabel.events.MessageEvent;
import com.chatwing.whitelabel.events.UserUnauthenticatedEvent;
import com.chatwing.whitelabel.generators.MessageRandomKeyGenerator;
import com.chatwing.whitelabel.loaders.CommunicationBoxMessagesLoader;
import com.chatwing.whitelabel.managers.ApiManager;
import com.chatwing.whitelabel.managers.UserManager;
import com.chatwing.whitelabel.parsers.BBCodePair;
import com.chatwing.whitelabel.parsers.BBCodeParser;
import com.chatwing.whitelabel.pojos.ChatBox;
import com.chatwing.whitelabel.pojos.CommunicationBoxJson;
import com.chatwing.whitelabel.pojos.Emoticon;
import com.chatwing.whitelabel.pojos.Message;
import com.chatwing.whitelabel.pojos.User;
import com.chatwing.whitelabel.pojos.errors.CreateMessageParamsError;
import com.chatwing.whitelabel.pojos.params.CreateConversationParams;
import com.chatwing.whitelabel.services.CreateMessageIntentService;
import com.chatwing.whitelabel.services.GetMessagesIntentService;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Created by cuongthai on 18/07/2014.
 */
public abstract class CommunicationMessagesFragment extends BaseFragment {
    private static final int MESSAGE_LOADER_ID = 87654;

    @Inject
    protected Bus mBus;
    @Inject
    protected InputMethodManager mInputMethodManager;
    @Inject
    protected Provider<Typeface> mIconicTypefaceProvider;
    @Inject
    protected ErrorMessageView mErrorMessageView;
    @Inject
    protected UserManager mUserManager;
    @Inject
    protected CommunicationMessagesAdapter mAdapter;
    @Inject
    protected MessageRandomKeyGenerator mRandomKeyGenerator;

    private View mBBCodeControlsContainer;
    private View mStickerContainer;
    private View sendBtn;
    private View mComposeContainer;
    private Map<BBCodeParser.BBCode, Button> mBBCodeControls;
    private BBCodeEditText mCommunicationBoxEditText;
    private EmoticonPackagesAdapter adapter;
    private ViewPager emoticonsPager;
    private TabPageIndicator indicator;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private View mLoadMoreBtn;
    private boolean mSyncingBBCodeControls;
    private boolean showingBottomContainer;

    protected CommunicationBoxMessagesLoaderCallbacks mLoaderCallbacks;
    protected boolean mIsNoMoreMessages;
    protected View newContentBtn;
    protected View newEmoBtn;
    protected Delegate mDelegate;
    protected boolean isLoadingMore = false;

    public interface Delegate extends InjectableFragmentDelegate {
        void showColorPickerDialogFragment(BBCodeParser.BBCode code);

        void showNewContentFragment();

        void showPasswordDialogFragment();

        void showBlockUserDialogFragment(Message message);

        void showConversation(CreateConversationParams.SimpleUser simpleUser);

        void inject(BBCodeEditText mCommunicationBoxEditText);
    }

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

    public CommunicationMessagesFragment() {
    }

    protected abstract Message constructMessage(User user, String content,
                                                long createdDate, String randomKey,
                                                Message.Status status);

    protected abstract CommunicationBoxMessagesLoaderCallbacks constructLoaderCallbacks();

    protected abstract boolean canHandle(MessageEvent event);

    protected abstract boolean isInCurrentCommunicationBox(MessageEvent event);

    protected abstract void loadMessagesFromServer(boolean forceLoadLatest);

    protected abstract void updateCommunicationBoxDetail();

    protected abstract boolean hasCurrentCommunication();

    public abstract void deleteMessage(Message message);

    public abstract void deleteMessageByIp(Message message);

    public abstract void deleteMessageBySocialAccount(Message message);

    public void onContextMenuClosed(Menu menu) {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_messageview, container, false);
    }

    protected boolean handleException(Exception exception) {
        if (exception instanceof HttpRequest.HttpRequestException) {
            mErrorMessageView.show(R.string.error_network_connection);
            return true;
        }
        return false;
    }

    /**
     * Adds new message to the message list. While adding, the new message is
     * checked against existing ones by using random key ({@link Message#randomKey}.
     * If another message with the same random key already exists in the list,
     * instead of adding (and thus, creating a duplicated item), the existing
     * message is replaced by the new one.
     *
     * @param newMessage the new message being added.
     * @return true if the message is new and is added to the list.
     * false if the message already exists and necessary information
     * of the message is updated.
     */
    public boolean addNewMessage(Message newMessage) {

        String randomKey = newMessage.getRandomKey();
        boolean isAdded = false;
        if (TextUtils.isEmpty(randomKey)) {
            mAdapter.addToHead(newMessage);
            scrollToLast(false);
            isAdded = true;
        } else {
            Message existingMessage = mAdapter.getItemByRandomKey(randomKey);
            LogUtils.v("Check send message " + existingMessage);
            if (existingMessage != null) {
                mAdapter.replace(existingMessage, newMessage);
            } else {
                mAdapter.addToHead(newMessage);
                scrollToLast(false);
                isAdded = true;
            }
        }
        return isAdded;
    }

    private void scrollToLast(boolean force) {
        LogUtils.v("Scroll to last " + mLayoutManager.findFirstCompletelyVisibleItemPosition());
        //Only scroll if looking at to latest
        if (mLayoutManager.findFirstCompletelyVisibleItemPosition() == 0 || force) {
            mRecyclerView.scrollToPosition(0);
        }
    }

    protected void loadMessagesFromDb() {
        LogUtils.v("loadMessagesFromDb");
        if (!hasCurrentCommunication()) {
            // Communication box is required to init the loader.
            // So if it is unavailable by now, don't load.
            return;
        }

        if (mLoaderCallbacks == null) {
            mLoaderCallbacks = constructLoaderCallbacks();
        }
        getLoaderManager().restartLoader(MESSAGE_LOADER_ID, null, mLoaderCallbacks);
    }

    protected void onGotMessagesEvent(GotMessagesEvent event) {
        if (onMessageEvent(event, getString(R.string.error_while_loading_messages))) {
            isLoadingMore = false;
            return;
        }

        List<Message> messagesFromServer = event.getMessagesFromServer();
        if (messagesFromServer == null) {
            isLoadingMore = false;
            LogUtils.e("Both exception and new messages are null. " +
                    "Something is wrong.");
            return;
        }
        if (mIsNoMoreMessages = messagesFromServer.size() == 0) {
            isLoadingMore = false;
            mLoadMoreBtn.setVisibility(View.GONE);
            return;
        }
        mLoadMoreBtn.setVisibility(View.VISIBLE);

        if (event.isLoadMore()) {
            mAdapter.addAllDataToTail(messagesFromServer);
        } else {
            mAdapter.setData(messagesFromServer);
        }
        isLoadingMore = false;
    }

    protected void onCreateMessageEvent(CreateMessageEvent event) {
        Exception exception = event.getException();
        if (exception != null &&
                exception instanceof ApiManager.CreateMessageException) {
            ApiManager.CreateMessageException createMessageException =
                    (ApiManager.CreateMessageException) exception;
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
        } else {
            toastMessage = getString(R.string.error_unknown);
            // Unknown error, just leave the message in the list with a
            // "Failed" label.
            LogUtils.e("Unknown error: " + type);
        }

        mErrorMessageView.show(toastMessage);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDelegate.inject(this);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.message_list);
        emoticonsPager = (ViewPager) view.findViewById(R.id.pager);
        emoticonsPager.setAdapter(new EmoticonPackagesAdapter(getActivity().getSupportFragmentManager(),
                new HashMap<String, Emoticon[]>()));
        mLoadMoreBtn = view.findViewById(R.id.loadMoreBtn);
        indicator = (TabPageIndicator) view.findViewById(R.id.indicator);
        indicator.setViewPager(emoticonsPager);

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

        mLoadMoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLoadMoreBtn.setVisibility(View.GONE);
                isLoadingMore = true;
                loadMessagesFromServer(false);
            }
        });
    }

    protected void doUpdateCommunicationBoxDetail(CommunicationBoxJson communicationBoxJson,
                                                  Map<String, String> emoticons,
                                                  boolean hasAdminPermission) {
        // Reset the same adapter to the list view, to invalidate all items
        // so that they are not reused.
        mAdapter.updateCommunicationBoxDetail(
                communicationBoxJson,
                emoticons,
                hasAdminPermission);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        configRecyclerView();
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
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Clear adapter to make sure we load the whole new messages
        mAdapter.clear();
        getLoaderManager().destroyLoader(MESSAGE_LOADER_ID);
    }

    @Override
    protected void onAttachToContext(Context context) {
        if (context instanceof Delegate) {
            mDelegate = (Delegate) context;
        }
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

    public void appendBBCode(BBCodePair pair) {
        if (mCommunicationBoxEditText.append(pair)) {
            showKeyboard();
            syncBBCodeControlState(pair.getCode());
            saveBBCodesIfNeeded();
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

        if (!hasCurrentCommunication()) {
            // No current communication box to send new message to
            return;
        }

        long createdDate = System.currentTimeMillis();
        String randomKey = mRandomKeyGenerator.generate(content, createdDate);
        Message newMessage = constructMessage(
                user,
                content,
                createdDate,
                randomKey,
                Message.Status.SENDING);
        newMessage.setCreatedDate(System.currentTimeMillis());

        if (addNewMessage(newMessage)) {
            scrollToLast(true);
        }
        mCommunicationBoxEditText.setText("");
        if (!user.getProfile().shouldRememberPreviousStyle()) {
            mCommunicationBoxEditText.clearAllBBCodes();
            syncBBCodeControlsState();
        }

        // Send task to CreateMessageIntentService to make a async request.
        Intent i = new Intent(getActivity(), CreateMessageIntentService.class);
        i.putExtra(CreateMessageIntentService.EXTRA_MESSAGE, newMessage);
        getActivity().startService(i);
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

    public abstract class CommunicationBoxMessagesLoaderCallbacks implements
            LoaderManager.LoaderCallbacks<CommunicationBoxMessagesLoader.Result> {

        @Override
        public void onLoadFinished(Loader<CommunicationBoxMessagesLoader.Result> loader,
                                   CommunicationBoxMessagesLoader.Result data) {
            List<Message> messagesFromDB = data.getMessages();

            LogUtils.v("CommunicationBoxMessagesLoaderCallbacks finished " + loader.getId());
            if (data.getException() != null) {
                mErrorMessageView.show(data.getException());
            } else if (messagesFromDB != null && messagesFromDB.size() > 0) {
                mAdapter.setData(messagesFromDB);
            }
            //Load Remote Messages for up to date
            loadMessagesFromServer(true);
        }

        @Override
        public void onLoaderReset(Loader<CommunicationBoxMessagesLoader.Result> loader) {
        }
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

    private void hideBBCodeControls() {
        mBBCodeControlsContainer.setVisibility(View.GONE);
    }

    private void hideStickerContainer() {
        mStickerContainer.setVisibility(View.GONE);
    }

    private void syncBBCodeControlsState() {
        for (BBCodeParser.BBCode code : mBBCodeControls.keySet()) {
            syncBBCodeControlState(code);
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

    private void configRecyclerView() {
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setReverseLayout(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            int pastVisiblesItems, visibleItemCount, totalItemCount;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_SETTLING
                        || newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    mLoadMoreBtn.setVisibility(View.GONE);
                }

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (mIsNoMoreMessages) {
                        mLoadMoreBtn.setVisibility(View.GONE);
                    } else {
                        mLoadMoreBtn.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                visibleItemCount = mLayoutManager.getChildCount();
                pastVisiblesItems = mLayoutManager.findFirstVisibleItemPosition();
                totalItemCount = mLayoutManager.getItemCount();

                if (!isLoadingMore) {
                    if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                        LogUtils.v("Last Item Wow !");
                        mLoadMoreBtn.setVisibility(View.GONE);
                        isLoadingMore = true;
                        loadMessagesFromServer(false);
                    }
                }
            }
        });
    }
}
