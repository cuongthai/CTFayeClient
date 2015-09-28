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

package com.chatwing.whitelabel.managers;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.chatwing.whitelabel.ChatWing;
import com.chatwing.whitelabel.Constants;
import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.fragments.ChatMessagesFragment;
import com.chatwing.whitelabel.modules.ForApplication;
import com.chatwing.whitelabel.pojos.Message;
import com.chatwing.whitelabel.pojos.OnlineUser;
import com.chatwing.whitelabel.pojos.User;
import com.chatwing.whitelabel.pojos.errors.ChatWingError;
import com.chatwing.whitelabel.pojos.errors.CreateMessageParamsError;
import com.chatwing.whitelabel.pojos.jspojos.JSUserResponse;
import com.chatwing.whitelabel.pojos.params.BlockUserParams;
import com.chatwing.whitelabel.pojos.params.ChatBoxMessagesParams;
import com.chatwing.whitelabel.pojos.params.ChatboxAckParams;
import com.chatwing.whitelabel.pojos.params.ChatboxNotificationStatus;
import com.chatwing.whitelabel.pojos.params.ConcreteParams;
import com.chatwing.whitelabel.pojos.params.ConversationAckParams;
import com.chatwing.whitelabel.pojos.params.ConversationMessageParams;
import com.chatwing.whitelabel.pojos.params.ConversationNotificationStatus;
import com.chatwing.whitelabel.pojos.params.CreateBookmarkParams;
import com.chatwing.whitelabel.pojos.params.CreateChatBoxParams;
import com.chatwing.whitelabel.pojos.params.CreateConversationMessageParams;
import com.chatwing.whitelabel.pojos.params.CreateConversationParams;
import com.chatwing.whitelabel.pojos.params.CreateMessageParams;
import com.chatwing.whitelabel.pojos.params.DeleteBookmarkParams;
import com.chatwing.whitelabel.pojos.params.DeleteMessageParams;
import com.chatwing.whitelabel.pojos.params.FlagMessageParams;
import com.chatwing.whitelabel.pojos.params.IgnoreUserParams;
import com.chatwing.whitelabel.pojos.params.LoadBookmarkParams;
import com.chatwing.whitelabel.pojos.params.LoadChatBoxDetailsParams;
import com.chatwing.whitelabel.pojos.params.LoadConversationParams;
import com.chatwing.whitelabel.pojos.params.LoadModeratorParams;
import com.chatwing.whitelabel.pojos.params.MessageCountParams;
import com.chatwing.whitelabel.pojos.params.OnlineUserParams;
import com.chatwing.whitelabel.pojos.params.Params;
import com.chatwing.whitelabel.pojos.params.RegisterParams;
import com.chatwing.whitelabel.pojos.params.ResetPasswordParams;
import com.chatwing.whitelabel.pojos.params.SearchChatBoxParams;
import com.chatwing.whitelabel.pojos.params.SubscriptionParams;
import com.chatwing.whitelabel.pojos.params.UpdateGcmParams;
import com.chatwing.whitelabel.pojos.params.UpdateUserProfileParams;
import com.chatwing.whitelabel.pojos.params.oauth.AuthenticationParams;
import com.chatwing.whitelabel.pojos.responses.AuthenticationResponse;
import com.chatwing.whitelabel.pojos.responses.BlackListResponse;
import com.chatwing.whitelabel.pojos.responses.BookmarkResponse;
import com.chatwing.whitelabel.pojos.responses.ChatBoxDetailsResponse;
import com.chatwing.whitelabel.pojos.responses.ChatBoxListResponse;
import com.chatwing.whitelabel.pojos.responses.CreateBookmarkResponse;
import com.chatwing.whitelabel.pojos.responses.CreateChatBoxResponse;
import com.chatwing.whitelabel.pojos.responses.CreateConversationResponse;
import com.chatwing.whitelabel.pojos.responses.CreateMessageResponse;
import com.chatwing.whitelabel.pojos.responses.DeleteBookmarkResponse;
import com.chatwing.whitelabel.pojos.responses.DeleteMessageResponse;
import com.chatwing.whitelabel.pojos.responses.ErrorResponse;
import com.chatwing.whitelabel.pojos.responses.FlagMessageResponse;
import com.chatwing.whitelabel.pojos.responses.IgnoreUserResponse;
import com.chatwing.whitelabel.pojos.responses.LoadConversationsResponse;
import com.chatwing.whitelabel.pojos.responses.LoadModeratorsResponse;
import com.chatwing.whitelabel.pojos.responses.LoadOnlineUsersResponse;
import com.chatwing.whitelabel.pojos.responses.MessagesResponse;
import com.chatwing.whitelabel.pojos.responses.RegisterResponse;
import com.chatwing.whitelabel.pojos.responses.ResetPasswordResponse;
import com.chatwing.whitelabel.pojos.responses.SearchChatBoxResponse;
import com.chatwing.whitelabel.pojos.responses.SubscriptionResponse;
import com.chatwing.whitelabel.pojos.responses.SubscriptionStatusResponse;
import com.chatwing.whitelabel.pojos.responses.UnreadCountResponse;
import com.chatwing.whitelabel.pojos.responses.UpdateGcmResponse;
import com.chatwing.whitelabel.pojos.responses.UpdateUserProfileResponse;
import com.chatwing.whitelabel.pojos.responses.UserResponse;
import com.chatwing.whitelabel.services.UpdateNotificationSettingsService;
import com.chatwing.whitelabel.utils.LogUtils;
import com.chatwing.whitelabel.utils.StatisticTracker;
import com.chatwing.whitelabel.validators.ChatBoxIdValidator;
import com.chatwing.whitelabel.validators.ConversationIdValidator;
import com.chatwing.whitelabel.validators.EmailValidator;
import com.chatwing.whitelabel.validators.MessageIdValidator;
import com.chatwing.whitelabel.validators.PasswordValidator;
import com.chatwing.whitelabel.validators.PermissionsValidator;
import com.github.kevinsawicki.http.HttpRequest;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

/**
 * Created by cuongthai on 28/07/2014.
 */
public class ApiManagerImpl implements ApiManager {
    @Inject
    @ForApplication
    Context mContext;
    @Inject
    ConversationIdValidator mConversationIdValidator;
    @Inject
    EmailValidator mEmailValidator;
    @Inject
    PasswordValidator mPasswordValidator;

    @Inject
    ChatBoxIdValidator mChatBoxIdValidator;
    @Inject
    MessageIdValidator mMessageIdValidator;
    @Inject
    PermissionsValidator mPermissionsValidator;
    @Inject
    BuildManager mBuildManager;

    /**
     * Sets up the request with common requirements.
     * For now, only set accept param to "application.json".
     */
    protected static void setUpRequest(HttpRequest request) {
        setUpRequest(request, null);
    }

    /**
     * Sets up the request with common requirements, like JSON response and
     * set session key as a cookie if provided.
     */
    protected static void setUpRequest(HttpRequest request,
                                       User user) {
        if (user != null && user.isSessionValid()) {
            LogUtils.v("Access Token " + user.getAccessToken());
            request.authorization("Bearer " + user.getAccessToken());
        }
        request.acceptJson();
        request.contentType(HttpRequest.CONTENT_TYPE_JSON);
    }

    protected static void validate(User user)
            throws ApiException,
            UserUnauthenticatedException {
        if (user == null || !user.isSessionValid()) {
            throw new UserUnauthenticatedException();
        }
    }

    protected static void validate(Message message) throws ApiException {
        if (message == null) {
            throw ApiException.createException(new Exception("Message must not be null."));
        }
        if (TextUtils.isEmpty(message.getContent())) {
            throw ApiException.createException(new Exception("Message params has empty content."));
        }
    }

    /**
     * Validates response from the request.
     *
     * @param request the request to be validated
     * @return body of the response if server returned an "OK" response. This
     * is required because
     * {@link HttpRequest#body()}
     * can only be consumed once and it is done inside
     * this method to validate unauthenticated error.
     * @throws ApiException, InvalidAccessTokenException, ValidationException, InvalidIdentityException
     *                       They are general exception that can happen at any api at any time.
     *                       For specific exceptions they will be handle in api itself
     */
    protected static String validate(HttpRequest request)
            throws ApiException,
            InvalidAccessTokenException,
            ValidationException,
            InvalidIdentityException,
            NotVerifiedEmailException,
            OtherApplicationException {
        if (!request.ok()) {
            LogUtils.e("Request failed with code " + request.code() + " url " + request.url());
            throw ApiException.createException(new Exception("Request failed with code " + request.code() + " url " + request.url()));
        }
        String body = request.body();
        if (body.isEmpty()) {
            throw ApiException.createException(new Exception("Response empty url " + request.url()));
        }

        Gson gson = new Gson();
        ErrorResponse errorResponse = gson.fromJson(body, ErrorResponse.class);

        //We handle errors due to developer and throw general exception to user
        //Other specific exceptions will be handled in api itself
        handleError(request, errorResponse);

        return body;
    }

    private static void handleError(HttpRequest request, ErrorResponse errorResponse)
            throws InvalidAccessTokenException,
            ValidationException,
            InvalidIdentityException,
            ApiException,
            NotVerifiedEmailException,
            OtherApplicationException {
        if (errorResponse == null || errorResponse.getError() == null) return;
        ChatWingError error = errorResponse.getError();
        switch (error.getCode()) {
            case ChatWingError.ERROR_CODE_ACCESS_DENIED:
            case ChatWingError.ERROR_CODE_MISSING_ALL_REQUIRED_PARAMS:
            case ChatWingError.ERROR_CODE_MISSING_ONE_REQUIRED_PARAMS:
            case ChatWingError.ERROR_CODE_BAD_REQUEST:
            case ChatWingError.ERROR_CODE_MISSING_CLIENT_ID:
                LogUtils.e("ChatWing Error (" + error.getCode() + "):" + error.getMessage());
                throw ApiException.createException(new Exception("There was an error while processing your request"));
            case ChatWingError.ERROR_CODE_VALIDATION_ERR:
                //Validation should be handle on client side before sending to server
                //If we still receive this, we will try to handle on UI and log it to server
                //we need to update client code to handle it before sending to server
                //Eg. register/authenticate
                //Note: we still recieve this in case invalid username/password because we cant check it on client
                throw new ValidationException(error);
            case ChatWingError.ERROR_CODE_INVALID_ACCESS_TOKEN:
                //This happens because developer forgot to put access_token in the request
                //Or token is expired.
                //Currently it happens only for guest account
                //The account should be re obtain the access_token. In guest case, it should recreate guest
                throw new InvalidAccessTokenException(error);
            case ChatWingError.ERROR_CODE_INVALID_IDENTITY:
                //This is when you are trying to access a resource that requires another login type
                //E.g create message in chatbox that requires facebook account only
                throw new InvalidIdentityException(error);
            case ChatWingError.ERROR_CODE_VERIFY_EMAIL_ERR:
                throw new NotVerifiedEmailException(error);
            case ChatWingError.ERROR_CODE_OTHER_ERR:
                throw new OtherApplicationException(error);

        }
    }

    @Override
    public String getFullEmoticonUrl(String url) {
        if (isEmptyUrl(url)) {
            return DEFAULT_EMOTICON_URL;
        }
        return Constants.CHATWING_BASE_URL + url;
    }

    private static boolean isEmptyUrl(String url) {
        return TextUtils.isEmpty(url) || url.equals("false");
    }

    @Override
    public AuthenticationResponse authenticate(AuthenticationParams params)
            throws ApiException,
            HttpRequest.HttpRequestException,
            ValidationException,
            InvalidExternalAccessTokenException,
            OtherApplicationException {
        StatisticTracker.trackLoginType(params.getType());
        if (params == null) {
            throw ApiException.createException(new Exception("Can't authenticate when params is null."));
        }
        Gson gson = new Gson();
        String paramsString = gson.toJson(params);
        LogUtils.v("Authentication params " + paramsString);
        HttpRequest request = HttpRequest.post(AUTHENTICATE_URL);
        setUpRequest(request);
        request.send(paramsString);
        String responseString = null;
        try {
            responseString = validate(request);

            AuthenticationResponse response = gson.fromJson(
                    responseString,
                    AuthenticationResponse.class);
            if (ChatWingError.hasExternalInvalidAccessToken(response.getError())) {
                throw new InvalidExternalAccessTokenException(response.getError());
            }
            return response;
        } catch (JsonSyntaxException e) {
            throw ApiException.createJsonSyntaxException(e, responseString);
        } catch (InvalidIdentityException e) {
            throw ApiException.createException(e);
        } catch (InvalidAccessTokenException e) {
            throw ApiException.createException(e);
        } catch (NotVerifiedEmailException e) {
            throw ApiException.createJsonSyntaxException(e, responseString);
        }
    }

    @Override
    public CreateConversationResponse createConversation(User user,
                                                         String loginId,
                                                         String loginType)
            throws ApiException,
            UserUnauthenticatedException,
            HttpRequest.HttpRequestException,
            InvalidAccessTokenException,
            InvalidIdentityException,
            NotVerifiedEmailException,
            OtherApplicationException {
        validate(user);

        if (!mPermissionsValidator.canDoConversation(user)) {
            throw new InvalidIdentityException(new ChatWingError(
                    ChatWingError.ERROR_CODE_INVALID_IDENTITY,
                    mContext.getString(R.string.error_required_login_except_guest),
                    null));
        }

        Gson gson = new Gson();
        CreateConversationParams.SimpleUser userParams
                = new CreateConversationParams.SimpleUser(loginId, loginType);
        CreateConversationParams.SimpleUser[] users
                = new CreateConversationParams.SimpleUser[]{userParams};
        CreateConversationParams params = new CreateConversationParams(users);
        String paramsString = gson.toJson(params);

        HttpRequest request = HttpRequest.post(CONVERSATION_CREATE_URL);
        setUpRequest(request, user);
        request.send(paramsString);
        String responseString = null;

        try {
            responseString = validate(request);
            CreateConversationResponse createConversationResponse = gson.fromJson(responseString, CreateConversationResponse.class);
            LogUtils.v("Populate user debug: parse " + responseString);

            return createConversationResponse;
        } catch (JsonSyntaxException e) {
            throw ApiException.createJsonSyntaxException(e, responseString);
        } catch (ValidationException e) {
            throw ApiException.createException(e);
        }
    }

    @Override
    public SubscriptionStatusResponse loadCommunicationSetting(User user,
                                                               int chatboxID)
            throws ApiException,
            HttpRequest.HttpRequestException,
            UserUnauthenticatedException,
            ChatBoxIdValidator.InvalidIdException,
            InvalidAccessTokenException,
            InvalidIdentityException,
            NotVerifiedEmailException,
            OtherApplicationException {
        validate(user);
        mChatBoxIdValidator.validate(chatboxID);

        Gson gson = new Gson();
        ChatboxNotificationStatus params = new ChatboxNotificationStatus(chatboxID);

        HttpRequest request = HttpRequest.get(CHAT_BOX_NOTIFICATION_STATUS_URL + appendParams(params));

        setUpRequest(request, user);
        String responseString = null;

        try {
            responseString = validate(request);

            SubscriptionStatusResponse loadConversationsResponse
                    = gson.fromJson(responseString, SubscriptionStatusResponse.class);
            return loadConversationsResponse;
        } catch (JsonSyntaxException e) {
            throw ApiException.createJsonSyntaxException(e, responseString);
        } catch (ValidationException e) {
            throw ApiException.createException(e);
        }
    }

    public SubscriptionStatusResponse loadCommunicationSetting(User user,
                                                               String conversationID)
            throws ApiException,
            HttpRequest.HttpRequestException,
            UserUnauthenticatedException,
            ConversationIdValidator.InvalidIdException,
            InvalidAccessTokenException,
            InvalidIdentityException,
            NotVerifiedEmailException,
            OtherApplicationException {
        validate(user);
        mConversationIdValidator.validate(conversationID);

        Gson gson = new Gson();
        ConversationNotificationStatus params = new ConversationNotificationStatus(conversationID);

        HttpRequest request = HttpRequest.get(CONVERSATION_NOTIFICATION_STATUS_URL + appendParams(params));
        setUpRequest(request, user);
        String responseString = null;

        try {
            responseString = validate(request);
            SubscriptionStatusResponse loadConversationsResponse
                    = gson.fromJson(responseString, SubscriptionStatusResponse.class);
            LogUtils.v("Notification " + responseString);

            return loadConversationsResponse;
        } catch (JsonSyntaxException e) {
            throw ApiException.createJsonSyntaxException(e, responseString);
        } catch (ValidationException e) {
            throw ApiException.createException(e);
        }
    }

    private void test() {
        File latest = new File("/data/data/com.chatwing.demo/cache/cache.json");
        HttpRequest request = HttpRequest.get("http://chatwing.com/chatbox/424ff38c-198a-4ff1-a91a-2924f9331617");
////Copy response to file
//        request.receive(latest);
//Store eTag of response
//        String eTag = request.eTag();
//Later on check if changes exist
        boolean unchanged = HttpRequest.get("http://chatwing.com/chatbox/424ff38c-198a-4ff1-a91a-2924f9331617")
                .notModified();
        LogUtils.v("UnChanged " + unchanged);
    }

    @Override
    public ChatBoxListResponse loadChatBoxes()
            throws ApiException,
            HttpRequest.HttpRequestException,
            NotVerifiedEmailException,
            OtherApplicationException {
        Gson gson = new Gson();

        Params requestChatboxParams = new ConcreteParams();

        HttpRequest request = HttpRequest.get(CHAT_BOX_LIST_URL + appendParams(requestChatboxParams));
        String responseString = null;
        try {
            responseString = validate(request);
            ChatBoxListResponse response = gson.fromJson(
                    responseString,
                    ChatBoxListResponse.class);
            return response;
        } catch (JsonSyntaxException e) {
            throw ApiException.createJsonSyntaxException(e, responseString);
        } catch (InvalidIdentityException e) {
            throw ApiException.createException(e);
        } catch (InvalidAccessTokenException e) {
            throw ApiException.createException(e);
        } catch (ValidationException e) {
            throw ApiException.createException(e);
        }
    }

    @Override
    public CreateMessageResponse createMessage(User user,
                                               Message messageParams)
            throws ApiException,
            HttpRequest.HttpRequestException,
            UserUnauthenticatedException,
            ChatBoxIdValidator.InvalidIdException,
            ConversationIdValidator.InvalidIdException,
            InvalidIdentityException,
            InvalidAccessTokenException,
            CreateMessageException,
            NotVerifiedEmailException,
            OtherApplicationException {
        validate(user);
        validate(messageParams);

        Gson gson = new Gson();
        Object params;
        String url;
        if (!messageParams.isPrivate()) {
            mChatBoxIdValidator.validate(messageParams.getChatBoxId());
            params = new CreateMessageParams(
                    messageParams.getContent(),
                    messageParams.getChatBoxId(),
                    messageParams.getRandomKey());
            url = CHAT_BOX_CREATE_MESSAGE_URL;
        } else {
            mConversationIdValidator.validate(messageParams.getConversationID());
            params = new CreateConversationMessageParams(
                    messageParams.getContent(),
                    messageParams.getConversationID(),
                    messageParams.getRandomKey());
            url = CONVERSATION_CREATE_MESSAGE_URL;
        }
        String paramsString = gson.toJson(params);

        HttpRequest request = HttpRequest.post(url);
        setUpRequest(request, user);
        request.send(paramsString);
        String responseString = null;

        try {
            responseString = validate(request);
            CreateMessageResponse response = gson.fromJson(
                    responseString,
                    CreateMessageResponse.class);

            // Update messages in response and their statuses if needed.
            ChatWingError error = response.getError();
            Message message = response.getMessage();
            if (error != null) {
                if (error.getCode() == ChatWingError.ERROR_CODE_UNABLE_TO_SEND_MESSAGE) {
                    CreateMessageParamsError createMessageParamsError = gson.fromJson(error.getParams(),
                            CreateMessageParamsError.class);
                    // Error occurred, give the params message to response
                    // because it may be used later (to update view, for example).
                    message = messageParams;
                    response.setMessage(message);

                    Message.Status status =
                            createMessageParamsError.getType().equals(CreateMessageParamsError.TYPE_BLOCK)
                                    ? Message.Status.BLOCKED
                                    : Message.Status.FAILED;

                    message.setStatus(status);
                    Message userMessage = createMessageParamsError.getUserMessage();
                    if (userMessage != null) {
                        userMessage.setStatus(status);
                        userMessage.setCreatedDate(message.getCreatedDate());
                    }
                    throw new CreateMessageException(error, createMessageParamsError, message);
                } else {
                    //Out of our scope, server says something strange, report to developer and tell nicely to user
                    throw ApiException.createException(new Exception("Cant handle this error code while sending message"));
                }
            } else if (message != null) {
                // To reduce response size, message from this API misses some
                // fields that the client app obviously knows (#124).
                // Let's set them.
                //Since server wont return messageIP in the response, the only place to set ip is from client
                message.copyUserData(messageParams);
                message.setStatus(Message.Status.PUBLISHED);
            }

            return response;
        } catch (JsonSyntaxException e) {
            throw ApiException.createJsonSyntaxException(e, responseString);
        } catch (ValidationException e) {
            throw ApiException.createException(e);
        } catch (InvalidIdentityException e) {
            throw ApiException.createException(e);
        }
    }

    @Override
    public UpdateGcmResponse updateGcm(User user,
                                       String gcmRegId,
                                       String action)
            throws ApiException,
            HttpRequest.HttpRequestException,
            UserUnauthenticatedException,
            InvalidAccessTokenException,
            NotVerifiedEmailException,
            OtherApplicationException {
        validate(user);
        if (TextUtils.isEmpty(gcmRegId)) {
            throw ApiException.createException(new Exception("GCM reg id is empty."));
        }

        String url;
        if (action.equals(GCM_ACTION_ADD)) {
            url = ADD_GCM_URL;
        } else if (action.equals(GCM_ACTION_REMOVE)) {
            url = REMOVE_GCM_URL;
        } else {
            throw ApiException.createException(new Exception("Invalid GCM action: " + action));
        }

        Gson gson = new Gson();
        UpdateGcmParams params = new UpdateGcmParams(gcmRegId);
        String paramsString = gson.toJson(params);

        HttpRequest request = HttpRequest.post(url);
        setUpRequest(request, user);
        request.send(paramsString);
        String responseString = null;

        try {
            responseString = validate(request);
            LogUtils.v(action + " GCM to server " + gcmRegId);
            return gson.fromJson(responseString, UpdateGcmResponse.class);
        } catch (JsonSyntaxException e) {
            throw ApiException.createJsonSyntaxException(e, responseString);
        } catch (InvalidIdentityException e) {
            throw ApiException.createException(e);
        } catch (ValidationException e) {
            throw ApiException.createException(e);
        }
    }

    @Override
    public ChatBoxDetailsResponse loadChatBoxDetails(User user,
                                                     int chatBoxId)
            throws ApiException,
            HttpRequest.HttpRequestException,
            ChatBoxIdValidator.InvalidIdException,
            ValidationException, InvalidIdentityException,
            NotVerifiedEmailException,
            OtherApplicationException {
        mChatBoxIdValidator.validate(chatBoxId);
        return loadChatBoxDetails(user, new LoadChatBoxDetailsParams(chatBoxId));
    }

    @Override
    public void ackChatbox(User user,
                           Integer chatboxID)
            throws ApiException,
            HttpRequest.HttpRequestException,
            UserUnauthenticatedException,
            ChatBoxIdValidator.InvalidIdException,
            InvalidAccessTokenException,
            InvalidIdentityException,
            NotVerifiedEmailException,
            OtherApplicationException {
        validate(user);
        mChatBoxIdValidator.validate(chatboxID);
        Gson gson = new Gson();
        ChatboxAckParams params = new ChatboxAckParams(chatboxID);

        String paramsString = gson.toJson(params);
        HttpRequest request = HttpRequest.post(CHATBOX_ACK_URL);
        setUpRequest(request, user);
        request.send(paramsString);
        String responseString = null;

        try {
            validate(request);
        } catch (JsonSyntaxException e) {
            throw ApiException.createJsonSyntaxException(e, responseString);
        } catch (ValidationException e) {
            throw ApiException.createException(e);
        }
    }

    @Override
    public void ackConversation(User user,
                                String conversationId)
            throws ApiException,
            HttpRequest.HttpRequestException,
            UserUnauthenticatedException,
            ConversationIdValidator.InvalidIdException,
            InvalidAccessTokenException,
            InvalidIdentityException,
            NotVerifiedEmailException, OtherApplicationException {
        validate(user);
        mConversationIdValidator.validate(conversationId);
        Gson gson = new Gson();
        ConversationAckParams params = new ConversationAckParams(conversationId);

        String paramsString = gson.toJson(params);
        HttpRequest request = HttpRequest.post(CONVERSATION_ACK_URL);
        setUpRequest(request, user);
        request.send(paramsString);
        String responseString = null;

        try {
            validate(request);
        } catch (JsonSyntaxException e) {
            throw ApiException.createJsonSyntaxException(e, responseString);
        } catch (ValidationException e) {
            throw ApiException.createException(e);
        }
    }

    @Override
    public SubscriptionResponse updateNotificationSubscription(User user,
                                                               String action,
                                                               String conversationID,
                                                               String target)
            throws ApiException,
            HttpRequest.HttpRequestException,
            UserUnauthenticatedException,
            ConversationIdValidator.InvalidIdException,
            InvalidAccessTokenException,
            NotVerifiedEmailException, OtherApplicationException {
        validate(user);
        mConversationIdValidator.validate(conversationID);
        Gson gson = new Gson();
        SubscriptionParams params = new SubscriptionParams(target, conversationID);
        String url = null;
        if (UpdateNotificationSettingsService.ACTION_SUBSCRIBE.equals(action)) {
            url = CONVERSATION_SUBSCRIBE_URL;
        } else if (UpdateNotificationSettingsService.ACTION_UNSUBSCRIBE.equals(action)) {
            url = CONVERSATION_UNSUBSCRIBE_URL;
        }

        String paramsString = gson.toJson(params);
        HttpRequest request = HttpRequest.post(url);
        setUpRequest(request, user);
        request.send(paramsString);
        String responseString = null;

        try {
            responseString = validate(request);
            LogUtils.v("Error responseString " + responseString);
            SubscriptionResponse response = gson.fromJson(
                    responseString,
                    SubscriptionResponse.class);
            //TODO We should return different code
            if (response.getError() != null && response.getError().getCode() == ChatWingError.ERROR_CODE_APPLICATION_ERR) {
                throw new NotVerifiedEmailException(new ChatWingError(ChatWingError.ERROR_CODE_VERIFY_EMAIL_ERR, "Email is not veried", response.getError().getParams()));
            }
            return response;
        } catch (JsonSyntaxException e) {
            throw ApiException.createJsonSyntaxException(e, responseString);
        } catch (ValidationException e) {
            throw ApiException.createException(e);
        } catch (InvalidIdentityException e) {
            throw ApiException.createException(e);
        }
    }

    @Override
    public SubscriptionResponse updateNotificationSubscription(User user,
                                                               String action,
                                                               int chatboxID,
                                                               String target)
            throws ApiException,
            HttpRequest.HttpRequestException,
            UserUnauthenticatedException,
            ChatBoxIdValidator.InvalidIdException,
            InvalidAccessTokenException,
            NotVerifiedEmailException,
            OtherApplicationException {
        validate(user);
        mChatBoxIdValidator.validate(chatboxID);
        Gson gson = new Gson();
        SubscriptionParams params = new SubscriptionParams(target, chatboxID);
        String url = null;
        if (UpdateNotificationSettingsService.ACTION_SUBSCRIBE.equals(action)) {
            url = CHAT_BOX_SUBSCRIBE_URL;
        } else if (UpdateNotificationSettingsService.ACTION_UNSUBSCRIBE.equals(action)) {
            url = CHAT_BOX_UNSUBSCRIBE_URL;
        }

        String paramsString = gson.toJson(params);
        HttpRequest request = HttpRequest.post(url);
        setUpRequest(request, user);
        request.send(paramsString);
        String responseString = null;

        try {
            responseString = validate(request);
            SubscriptionResponse response = gson.fromJson(
                    responseString,
                    SubscriptionResponse.class);
            //TODO We should return different code
            if (response.getError() != null && response.getError().getCode() == ChatWingError.ERROR_CODE_APPLICATION_ERR) {
                throw new NotVerifiedEmailException(new ChatWingError(ChatWingError.ERROR_CODE_VERIFY_EMAIL_ERR, "Email is not veried", response.getError().getParams()));
            }
            return response;
        } catch (JsonSyntaxException e) {
            throw ApiException.createJsonSyntaxException(e, responseString);
        } catch (ValidationException e) {
            throw ApiException.createException(e);
        } catch (InvalidIdentityException e) {
            throw ApiException.createException(e);
        }
    }

    @Override
    public void ping(User user)
            throws ApiException,
            HttpRequest.HttpRequestException,
            UserUnauthenticatedException,
            OtherApplicationException {
        validate(user);

        HttpRequest request = HttpRequest.get(PING_URL + "?id=" + user.getId());
        String responseString = null;

        try {
            responseString = validate(request);
        } catch (JsonSyntaxException e) {
            throw ApiException.createJsonSyntaxException(e, responseString);
        } catch (ValidationException e) {
            throw ApiException.createException(e);
        } catch (InvalidAccessTokenException e) {
            e.printStackTrace();
        } catch (InvalidIdentityException e) {
            e.printStackTrace();
        } catch (NotVerifiedEmailException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void offline(User user)
            throws ApiException,
            HttpRequest.HttpRequestException,
            UserUnauthenticatedException,
            OtherApplicationException {
        validate(user);

        HttpRequest request = HttpRequest.get(OFFLINE_URL + appendParams(new ConcreteParams()));
        setUpRequest(request, user);

        String responseString = null;

        try {
            responseString = validate(request);
        } catch (JsonSyntaxException e) {
            throw ApiException.createJsonSyntaxException(e, responseString);
        } catch (ValidationException e) {
            throw ApiException.createException(e);
        } catch (InvalidAccessTokenException e) {
            e.printStackTrace();
        } catch (InvalidIdentityException e) {
            e.printStackTrace();
        } catch (NotVerifiedEmailException e) {
            e.printStackTrace();
        }
    }

    @Override
    public UnreadCountResponse getUnreadCountForChatbox(User user,
                                        Integer chatboxId)
            throws ApiException,
            HttpRequest.HttpRequestException,
            UserUnauthenticatedException,
            NotVerifiedEmailException,
            OtherApplicationException {
        validate(user);
        MessageCountParams params = new MessageCountParams(chatboxId);
        HttpRequest request = HttpRequest.get(CHAT_BOX_MESSAGES_COUNT_URL + appendParams(params));
        setUpRequest(request, user);

        String responseString = null;

        try {
            responseString = validate(request);
            UnreadCountResponse unreadCountResponse = new Gson().fromJson(responseString, UnreadCountResponse.class);
            return unreadCountResponse;
        } catch (JsonSyntaxException e) {
            throw ApiException.createJsonSyntaxException(e, responseString);
        } catch (ValidationException e) {
            throw ApiException.createException(e);
        } catch (InvalidAccessTokenException e) {
            throw ApiException.createException(e);
        } catch (InvalidIdentityException e) {
            throw ApiException.createException(e);
        }
    }


    @Override
    public LoadConversationsResponse loadConversations(User user,
                                                       int limit,
                                                       int offset)
            throws ApiException,
            HttpRequest.HttpRequestException,
            UserUnauthenticatedException,
            InvalidAccessTokenException,
            InvalidIdentityException,
            NotVerifiedEmailException,
            OtherApplicationException {
        validate(user);

        if (!mPermissionsValidator.canDoConversation(user)) {
            throw new InvalidIdentityException(new ChatWingError(
                    ChatWingError.ERROR_CODE_INVALID_IDENTITY,
                    mContext.getString(R.string.error_required_login_except_guest),
                    null));
        }
        Gson gson = new Gson();
        LoadConversationParams params = new LoadConversationParams(limit, offset);

        HttpRequest request = HttpRequest.get(CONVERSATION_LIST_URL + appendParams(params));
        setUpRequest(request, user);
        String responseString = null;

        try {
            responseString = validate(request);
            LoadConversationsResponse loadConversationsResponse
                    = gson.fromJson(responseString, LoadConversationsResponse.class);
            return loadConversationsResponse;
        } catch (JsonSyntaxException e) {
            throw ApiException.createJsonSyntaxException(e, responseString);
        } catch (ValidationException e) {
            throw ApiException.createException(e);
        }
    }

    @Override
    public LoadModeratorsResponse loadModerators(User user,
                                                 int limit,
                                                 int offset)
            throws ApiException,
            HttpRequest.HttpRequestException,
            UserUnauthenticatedException,
            InvalidAccessTokenException,
            InvalidIdentityException,
            NotVerifiedEmailException,
            OtherApplicationException {
        if (!mBuildManager.isCustomLoginType()) {
            return new LoadModeratorsResponse();
        }
        validate(user);


        Gson gson = new Gson();
        LoadModeratorParams params = new LoadModeratorParams(limit, offset);

        HttpRequest request = HttpRequest.get(MODERATOR_LIST_URL + appendParams(params));
        setUpRequest(request, user);
        String responseString = null;

        try {
            responseString = validate(request);
            LoadModeratorsResponse loadModeratorsResponse
                    = gson.fromJson(responseString, LoadModeratorsResponse.class);
            return loadModeratorsResponse;
        } catch (JsonSyntaxException e) {
            throw ApiException.createJsonSyntaxException(e, responseString);
        } catch (ValidationException e) {
            throw ApiException.createException(e);
        }
    }

    private ChatBoxDetailsResponse loadChatBoxDetails(User user,
                                                      LoadChatBoxDetailsParams params)
            throws ApiException,
            HttpRequest.HttpRequestException,
            ValidationException,
            InvalidIdentityException,
            NotVerifiedEmailException,
            OtherApplicationException {
        Gson gson = new Gson();
        LogUtils.v("loadChatBoxDetails");
        HttpRequest request = HttpRequest.get(CHAT_BOX_DETAIL_URL + appendParams(params));
        setUpRequest(request, user);
        String responseString = null;
        try {
            responseString = validate(request);
            LogUtils.v("loadChatBoxDetails " + responseString);
            ChatBoxDetailsResponse chatBoxDetailsResponse = gson.fromJson(responseString,
                    ChatBoxDetailsResponse.class);
            return chatBoxDetailsResponse;
        } catch (JsonSyntaxException e) {
            throw ApiException.createJsonSyntaxException(e, responseString);
        } catch (InvalidAccessTokenException e) {
            throw ApiException.createException(e);
        }
    }

    protected String appendParams(Object params) {
        StringBuilder builder = new StringBuilder();
        builder.append("?");
        Gson gson = new Gson();
        JsonObject gsonObject = gson.toJsonTree(params).getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : gsonObject.entrySet()) {
            JsonElement value = entry.getValue();
            if (value != null && value.isJsonPrimitive()) {
                try {
                    builder.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                    builder.append("=");
                    builder.append(URLEncoder.encode(value.getAsString(), "UTF-8"));
                    builder.append("&");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            }
        }
        return builder.toString();
    }

    @Override
    public String getAvatarUrl(User user) {
        if (user == null) {
            return DEFAULT_AVATAR_URL;
        }
        //Handle Guest Type
        if (user.isGuest()) {
            return Constants.CHATWING_BASE_URL + user.getAvatar();
        }
        //Handle other login types
        return getAvatarUrl(user.getLoginType(), user.getLoginId());
    }

    @Override
    public String getAvatarUrl(String userType, String userID, String avatar) {
        if (userType == null || userID == null || avatar == null) {
            return DEFAULT_AVATAR_URL;
        }

        if (Constants.TYPE_GUEST.equals(userType)) {
            return Constants.CHATWING_BASE_URL + avatar;
        }
        //Handle other login types
        return getAvatarUrl(userType, userID);
    }

    protected String getAvatarUrl(String loginType, String loginId) {
        if (TextUtils.isEmpty(loginType) || TextUtils.isEmpty(loginId)) {
            return DEFAULT_AVATAR_URL;
        }
        return ensureAbsoluteUrl(String.format(AVATAR_PATH, loginType, loginId),
                DEFAULT_AVATAR_URL);
    }

    @Override
    public String getUserProfileUrl(String userType, String userId) {
        if (userType == null || userId == null) {
            return Constants.CHATWING_BASE_URL;
        }
        if (TextUtils.isEmpty(userType) || TextUtils.isEmpty(userId)) {
            return Constants.CHATWING_BASE_URL;
        }
        if (userType.equals(Constants.TYPE_TWITTER)) {
            return "https://twitter.com/#!/" + userId;
        }
        if (userType.equals(Constants.TYPE_FACEBOOK)) {
            return "https://www.facebook.com/" + userId;
        }
        if (userType.equals(Constants.TYPE_YAHOO)) {
            return "http://profile.yahoo.com/" + userId;
        }
        if (userType.equals(Constants.TYPE_GOOGLE)) {
            return "https://plus.google.com/" + userId;
        }
        return Constants.CHATWING_BASE_URL;
    }


    private static String ensureAbsoluteUrl(String url, String defaultUrl) {
        if (isEmptyUrl(url)) {
            return defaultUrl;
        }
        if (url.startsWith("/")) {
            return Constants.CHATWING_BASE_URL + url;
        }
        if (Uri.parse(url).getHost() == null) {
            // Looks like the URL is malformed.
            return defaultUrl;
        }
        // The URL is absolute and valid, just return it then
        return url;
    }

    private MessagesResponse doLoadMessages(User user,
                                            String url,
                                            Object params)
            throws ApiException,
            InvalidAccessTokenException,
            InvalidIdentityException,
            NotVerifiedEmailException,
            OtherApplicationException {
        Gson gson = new Gson();

        HttpRequest request = HttpRequest.get(url + appendParams(params));
        setUpRequest(request, user);
        String responseString = null;

        try {
            responseString = validate(request);
            MessagesResponse response = gson.fromJson(
                    responseString,
                    MessagesResponse.class);
            List<Message> messages = response.getMessages();
            for (Message m : messages) {
                m.setStatus(Message.Status.PUBLISHED);
            }
            return response;
        } catch (JsonSyntaxException e) {
            throw ApiException.createJsonSyntaxException(e, responseString);
        } catch (ValidationException e) {
            throw ApiException.createException(e);
        }
    }

    @Override
    public ResetPasswordResponse resetPassword(String email)
            throws ApiException,
            HttpRequest.HttpRequestException,
            EmailValidator.InvalidEmailException,
            ValidationException,
            NotVerifiedEmailException,
            OtherApplicationException {
        mEmailValidator.validate(email);

        Gson gson = new Gson();
        ResetPasswordParams params = new ResetPasswordParams(email);
        String paramsString = gson.toJson(params);

        HttpRequest request = HttpRequest.post(RESET_PASSWORD_URL);
        setUpRequest(request);
        request.send(paramsString);
        String responseString = null;

        try {
            responseString = validate(request);
            return gson.fromJson(responseString, ResetPasswordResponse.class);
        } catch (JsonSyntaxException e) {
            throw ApiException.createJsonSyntaxException(e, responseString);
        } catch (InvalidIdentityException e) {
            throw ApiException.createException(e);
        } catch (InvalidAccessTokenException e) {
            throw ApiException.createException(e);
        }
    }


    @Override
    public MessagesResponse loadMessages(User user,
                                         int chatBoxId,
                                         Message oldestMessage)
            throws ApiException,
            HttpRequest.HttpRequestException,
            ChatBoxIdValidator.InvalidIdException,
            InvalidAccessTokenException,
            InvalidIdentityException,
            NotVerifiedEmailException,
            OtherApplicationException {
        mChatBoxIdValidator.validate(chatBoxId);

        ChatBoxMessagesParams params = new ChatBoxMessagesParams(chatBoxId);
        if (oldestMessage != null) {
            params.setDateCreated(oldestMessage.getCreatedDate());
        }

        return doLoadMessages(user, CHAT_BOX_MESSAGES_URL, params);
    }

    @Override
    public MessagesResponse loadMessages(User user,
                                         String conversationId,
                                         Message oldestMessage)
            throws ApiException,
            HttpRequest.HttpRequestException,
            ConversationIdValidator.InvalidIdException,
            UserUnauthenticatedException,
            InvalidAccessTokenException,
            InvalidIdentityException,
            NotVerifiedEmailException,
            OtherApplicationException {
        validate(user);
        mConversationIdValidator.validate(conversationId);

        ConversationMessageParams params = new ConversationMessageParams(conversationId);
        if (oldestMessage != null) {
            params.setCreatedDate(oldestMessage.getCreatedDate());
        }

        return doLoadMessages(user, CONVERSATION_MESSAGES_URL, params);
    }

    @Override
    public RegisterResponse register(String email,
                                     String password,
                                     boolean agreeConditions,
                                     boolean autoCreateChatBox)
            throws ApiException,
            HttpRequest.HttpRequestException,
            EmailValidator.InvalidEmailException,
            PasswordValidator.InvalidPasswordException,
            ValidationException,
            OtherApplicationException,
            InvalidAgreeConditionsException {
        mEmailValidator.validate(email);
        mPasswordValidator.validate(password);
        if (!agreeConditions) {
            throw new InvalidAgreeConditionsException(
                    mContext.getString(R.string.error_required_agree_conditions));
        }

        Gson gson = new Gson();
        RegisterParams params = new RegisterParams(
                email,
                password,
                agreeConditions,
                autoCreateChatBox);
        String paramsString = gson.toJson(params);

        HttpRequest request = HttpRequest.post(REGISTER_URL);
        setUpRequest(request);
        request.send(paramsString);
        String responseString = null;

        try {
            responseString = validate(request);
            return gson.fromJson(responseString, RegisterResponse.class);
        } catch (JsonSyntaxException e) {
            throw ApiException.createJsonSyntaxException(e, responseString);
        } catch (InvalidIdentityException e) {
            throw ApiException.createException(e);
        } catch (InvalidAccessTokenException e) {
            throw ApiException.createException(e);
        } catch (NotVerifiedEmailException e) {
            e.printStackTrace();
            throw ApiException.createException(new Exception("No way"));
            //Noway
        }
    }

    @Override
    public String getChatBoxUrl(String chatBoxKey) {
        return Constants.CHATWING_BASE_URL + "/chatbox/" + chatBoxKey;
    }

    @Override
    public UserResponse loadUserDetails(User user)
            throws UserUnauthenticatedException,
            HttpRequest.HttpRequestException,
            ApiException,
            InvalidAccessTokenException,
            NotVerifiedEmailException,
            OtherApplicationException {
        validate(user);

        Gson gson = new Gson();
        ConcreteParams paramsString = new ConcreteParams();
        HttpRequest request = HttpRequest.get(USER_DETAIL + appendParams(paramsString));
        setUpRequest(request, user);
        String responseString;
        try {
            LogUtils.v("Syncing user detail before load");
            responseString = validate(request);
            LogUtils.v("Syncing user detail " + responseString);
            return gson.fromJson(responseString, UserResponse.class);
        } catch (ValidationException e) {
            throw ApiException.createException(e);
        } catch (InvalidIdentityException e) {
            throw ApiException.createException(e);
        }
    }

    @Override
    public LoadOnlineUsersResponse loadOnlineUsers(int chatBoxId)
            throws ApiException,
            HttpRequest.HttpRequestException,
            ChatBoxIdValidator.InvalidIdException,
            NotVerifiedEmailException,
            OtherApplicationException {
        mChatBoxIdValidator.validate(chatBoxId);

        Gson gson = new Gson();
        OnlineUserParams params = new OnlineUserParams(chatBoxId);

        HttpRequest request = HttpRequest.get(CHAT_BOX_USER_LIST_URL + appendParams(params));
        String responseString = null;

        try {
            responseString = validate(request);
            return gson.fromJson(responseString, LoadOnlineUsersResponse.class);
        } catch (JsonSyntaxException e) {
            throw ApiException.createJsonSyntaxException(e, responseString);
        } catch (InvalidIdentityException e) {
            throw ApiException.createException(e);
        } catch (InvalidAccessTokenException e) {
            throw ApiException.createException(e);
        } catch (ValidationException e) {
            throw ApiException.createException(e);
        }
    }

    @Override
    public UpdateUserProfileResponse updateUserProfile(User user)
            throws ApiException,
            HttpRequest.HttpRequestException,
            UserUnauthenticatedException,
            ValidationException,
            InvalidAccessTokenException,
            NotVerifiedEmailException,
            OtherApplicationException {
        validate(user);

        Gson gson = new Gson();
        UpdateUserProfileParams params = new UpdateUserProfileParams(user.getProfile());
        String paramsString = gson.toJson(params);

        HttpRequest request = HttpRequest.post(USER_PROFILE_UPDATE_URL);
        setUpRequest(request, user);
        request.send(paramsString);
        String responseString = null;

        try {
            responseString = validate(request);
            return gson.fromJson(responseString, UpdateUserProfileResponse.class);
        } catch (JsonSyntaxException e) {
            throw ApiException.createJsonSyntaxException(e, responseString);
        } catch (InvalidIdentityException e) {
            throw ApiException.createException(e);
        }
    }

    public void verifyEmail(User user)
            throws UserUnauthenticatedException,
            HttpRequest.HttpRequestException,
            ApiException,
            NotVerifiedEmailException,
            OtherApplicationException {
        validate(user);

        Gson gson = new Gson();
        ConcreteParams params = new ConcreteParams();
        String paramsString = gson.toJson(params);

        HttpRequest request = HttpRequest.post(USER_VERIFY);
        setUpRequest(request, user);
        request.send(paramsString);
        String responseString = null;

        try {
            responseString = validate(request);
        } catch (JsonSyntaxException e) {
            throw ApiException.createJsonSyntaxException(e, responseString);
        } catch (InvalidIdentityException e) {
            throw ApiException.createException(e);
        } catch (InvalidAccessTokenException e) {
            throw ApiException.createException(e);
        } catch (ValidationException e) {
            throw ApiException.createException(e);
        }
    }


    @Override
    public int getLoginTypeImageResId(String type) {
        if (TextUtils.isEmpty(type)) {
            return 0;
        }
        if (type.equals(Constants.TYPE_CHATWING)) {
            return R.drawable.ic_launcher;
        }
        if (type.equals(Constants.TYPE_FACEBOOK)) {
            return R.drawable.login_type_facebook;
        }
        if (type.equals(Constants.TYPE_TWITTER)) {
            return R.drawable.login_type_twitter;
        }
        if (type.equals(Constants.TYPE_GOOGLE)) {
            return R.drawable.login_type_google;
        }
        if (type.equals(Constants.TYPE_YAHOO)) {
            return R.drawable.login_type_yahoo;
        }
        if (type.equals(Constants.TYPE_GUEST)) {
            return R.drawable.login_type_guest;
        }
        if (type.equals(Constants.TYPE_TUMBLR)) {
            return R.drawable.login_type_tumblr;
        }
        return 0;
    }

    @Override
    public String getAvatarUrl(OnlineUser user) {
        if (user == null) {
            return DEFAULT_AVATAR_URL;
        }
        return getAvatarUrl(user.getLoginType(), user.getLoginId());
    }

    @Override
    public String getAvatarUrl(Message message) {
        if (message == null) {
            return DEFAULT_AVATAR_URL;
        }
        return ensureAbsoluteUrl(message.getAvatar(), DEFAULT_AVATAR_URL) + "?size=" + Constants.AVATAR_SIZE;
    }

    @Override
    public IgnoreUserResponse ignoreUser(User user,
                                         String userId,
                                         String userType,
                                         boolean requestIgnore)
            throws UserUnauthenticatedException,
            HttpRequest.HttpRequestException,
            ApiException,
            InvalidAccessTokenException,
            NotVerifiedEmailException,
            OtherApplicationException {
        validate(user);

        Gson gson = new Gson();

        IgnoreUserParams params = new IgnoreUserParams(userId, userType);
        String paramsString = gson.toJson(params);

        HttpRequest request;
        if (requestIgnore) {
            request = HttpRequest.post(USER_IGNORE);
        } else {
            request = HttpRequest.post(USER_UNIGNORE);
        }
        setUpRequest(request, user);
        request.send(paramsString);
        String responseString;
        try {
            responseString = validate(request);
            return gson.fromJson(responseString, IgnoreUserResponse.class);
        } catch (ValidationException e) {
            throw ApiException.createException(e);
        } catch (InvalidIdentityException e) {
            throw ApiException.createException(e);
        }
    }

    @Override
    public DeleteMessageResponse deleteMessage(User user,
                                               int chatBoxId,
                                               String messageId)
            throws ApiException,
            HttpRequest.HttpRequestException,
            ChatBoxIdValidator.InvalidIdException,
            MessageIdValidator.InvalidIdException,
            UserUnauthenticatedException,
            InvalidIdentityException,
            InvalidAccessTokenException,
            RequiredPermissionException,
            NotVerifiedEmailException,
            OtherApplicationException {
        validate(user);
        mChatBoxIdValidator.validate(chatBoxId);
        mMessageIdValidator.validate(messageId);

        Gson gson = new Gson();
        DeleteMessageParams params = new DeleteMessageParams(chatBoxId, messageId);
        String paramsString = gson.toJson(params);

        HttpRequest request = HttpRequest.post(CHAT_BOX_DELETE_MESSAGE_URL);
        setUpRequest(request, user);
        request.send(paramsString);
        String responseString = null;

        try {
            responseString = validate(request);
            DeleteMessageResponse deleteMessageResponse = gson.fromJson(responseString, DeleteMessageResponse.class);
            if (ChatWingError.hasPermissionError(deleteMessageResponse.getError())) {
                throw new RequiredPermissionException();
            }
            return deleteMessageResponse;
        } catch (JsonSyntaxException e) {
            throw ApiException.createJsonSyntaxException(e, responseString);
        } catch (ValidationException e) {
            throw ApiException.createException(e);
        }
    }

    /***
     *
     * @param user
     * @param blockType by account type or user ip
     * @param messageToBlock
     * @param shouldRemoveMessage true when client wants server to broadcast remove events
     * @param blockReason
     * @param blockDuration
     * @return
     * @throws ApiException
     * @throws HttpRequest.HttpRequestException
     * @throws UserUnauthenticatedException
     * @throws ValidationException
     * @throws InvalidAccessTokenException
     * @throws RequiredPermissionException
     * @throws NotVerifiedEmailException
     * @throws OtherApplicationException
     */
    @Override
    public BlackListResponse blockUser(User user,
                                       ChatMessagesFragment.BLOCK blockType,
                                       Message messageToBlock,
                                       boolean shouldRemoveMessage,
                                       String blockReason,
                                       long blockDuration)
            throws ApiException,
            HttpRequest.HttpRequestException,
            UserUnauthenticatedException,
            ValidationException,
            InvalidAccessTokenException,
            RequiredPermissionException,
            NotVerifiedEmailException,
            OtherApplicationException {
        validate(user);
        Gson gson = new Gson();

        HttpRequest request = HttpRequest.post(BLACKLIST_CREATE_URL);
        setUpRequest(request, user);
        BlockUserParams params;
        if (blockType == ChatMessagesFragment.BLOCK.ACCOUNT_TYPE) {
            params = new BlockUserParams(
                    messageToBlock.getUserId(),
                    messageToBlock.getUserType(),
                    BlockUserParams.METHOD_SOCIAL,
                    shouldRemoveMessage,
                    String.valueOf(messageToBlock.getChatBoxId()),
                    blockDuration,
                    blockReason);
        } else {
            params = new BlockUserParams(
                    messageToBlock.getId(),
                    BlockUserParams.METHOD_IP,
                    shouldRemoveMessage,
                    String.valueOf(messageToBlock.getChatBoxId()),
                    blockDuration,
                    blockReason);
        }
        request.send(gson.toJson(params));
        String responseString = null;
        try {
            responseString = validate(request);
            BlackListResponse blackListResponse = gson.fromJson(responseString, BlackListResponse.class);
            //You don't have permission to block
            if (ChatWingError.hasPermissionError(blackListResponse.getError())) {
                throw new RequiredPermissionException();
            }
            return blackListResponse;
        } catch (JsonSyntaxException e) {
            throw ApiException.createJsonSyntaxException(e, responseString);
        } catch (InvalidIdentityException e) {
            throw ApiException.createException(e);
        }
    }

    @Override
    public JSUserResponse updateAvatar(User user,
                                       String path)
            throws UserUnauthenticatedException,
            HttpRequest.HttpRequestException,
            ApiException,
            NotVerifiedEmailException,
            OtherApplicationException {
        validate(user);

        HttpRequest request = HttpRequest.post(UPLOAD_AVATAR);
        LogUtils.v("Access token " + user.getAccessToken());
        setUpRequest(request, user);
        //We have to specify this temp_avatar.jpg in order to upload the file...
        //https://github.com/kevinsawicki/http-request/issues/22
        request.part("avatar", "temp_avatar.jpg", new File(path));
        request.part("client_id", ChatWing.getClientID());
        request.part("app_id", ChatWing.getAppId());
        String responseString;

        try {
            responseString = validate(request);
            return new Gson().fromJson(responseString, JSUserResponse.class);
        } catch (InvalidAccessTokenException e) {
            throw ApiException.createException(e);
        } catch (InvalidIdentityException e) {
            throw ApiException.createException(e);
        } catch (ValidationException e) {
            throw ApiException.createException(e);
        }
    }

    @Override
    public SearchChatBoxResponse searchChatBox(String query,
                                               int offset,
                                               int limit)
            throws ApiException,
            HttpRequest.HttpRequestException,
            ValidationException,
            NotVerifiedEmailException,
            OtherApplicationException {
        if (TextUtils.isEmpty(query)) {
            throw new ValidationException(
                    new ChatWingError(ChatWingError.ERROR_CODE_VALIDATION_ERR,
                            mContext.getString(R.string.error_blank_chatbox),
                            null));
        }

        Gson gson = new Gson();
        SearchChatBoxParams params = new SearchChatBoxParams(query, offset, limit);

        HttpRequest request = HttpRequest.get(CHAT_BOX_SEARCH_URL + appendParams(params));
        String responseString = null;
        try {
            responseString = validate(request);
            return gson.fromJson(responseString, SearchChatBoxResponse.class);
        } catch (JsonSyntaxException e) {
            throw ApiException.createJsonSyntaxException(e, responseString);
        } catch (InvalidAccessTokenException e) {
            throw ApiException.createException(e);
        } catch (InvalidIdentityException e) {
            throw ApiException.createException(e);
        }
    }

    @Override
    public CreateChatBoxResponse createChatBox(User user,
                                               String name)
            throws UserUnauthenticatedException,
            ApiException,
            HttpRequest.HttpRequestException,
            InvalidAccessTokenException,
            InvalidIdentityException,
            NotVerifiedEmailException,
            OtherApplicationException {
        validate(user);
        if (!mPermissionsValidator.canCreateChatBox(user)) {
            throw new InvalidIdentityException(new ChatWingError(
                    ChatWingError.ERROR_CODE_INVALID_IDENTITY,
                    mContext.getString(R.string.error_required_chat_wing_login_to_create_chat_boxes),
                    null));
        }

        Gson gson = new Gson();
        CreateChatBoxParams params = new CreateChatBoxParams(name);
        String paramsString = gson.toJson(params);

        HttpRequest request = HttpRequest.post(CHAT_BOX_CREATE_URL);
        setUpRequest(request, user);
        request.send(paramsString);
        String responseString = null;

        try {
            responseString = validate(request);
            CreateChatBoxResponse createChatBoxResponse = gson.fromJson(responseString, CreateChatBoxResponse.class);
            return createChatBoxResponse;
        } catch (JsonSyntaxException e) {
            throw ApiException.createJsonSyntaxException(e, responseString);
        } catch (ValidationException e) {
            throw ApiException.createException(e);
        }
    }

    @Override
    public String getFullChatBoxAliasUrl(String alias) {
        return Constants.CHATWING_BASE_URL + "/" + alias;
    }

    @Override
    public DeleteBookmarkResponse deleteBookmark(User user,
                                                 Integer bookmarkId)
            throws ApiException,
            HttpRequest.HttpRequestException,
            UserUnauthenticatedException,
            InvalidAccessTokenException,
            InvalidIdentityException,
            NotVerifiedEmailException,
            OtherApplicationException {
        validate(user);

        if (bookmarkId == null) {
            throw ApiException.createException(new Exception(mContext.getString(R.string.error_while_deleting_bookmark)));
        }

        if (!mPermissionsValidator.canBookmark(user)) {
            throw new InvalidIdentityException(new ChatWingError(
                    ChatWingError.ERROR_CODE_INVALID_IDENTITY,
                    mContext.getString(R.string.error_required_chat_wing_login),
                    null));
        }

        Gson gson = new Gson();

        DeleteBookmarkParams params = new DeleteBookmarkParams();
        params.setId(bookmarkId);
        String paramsString = gson.toJson(params);
        LogUtils.v("Bookmark delete params " + paramsString + ":" + user.getAccessToken());
        HttpRequest request = HttpRequest.post(BOOKMARK_DELETE);
        setUpRequest(request, user);
        request.send(paramsString);
        String responseString = null;
        try {
            responseString = validate(request);
            DeleteBookmarkResponse deleteBookmarkResponse = gson.fromJson(responseString,
                    DeleteBookmarkResponse.class);
            return deleteBookmarkResponse;
        } catch (JsonSyntaxException e) {
            throw ApiException.createJsonSyntaxException(e, responseString);
        } catch (ValidationException e) {
            throw ApiException.createException(e);
        }
    }

    @Override
    public CreateBookmarkResponse createBookmark(User user,
                                                 int chatboxId)
            throws ApiException,
            HttpRequest.HttpRequestException,
            UserUnauthenticatedException,
            ChatBoxIdValidator.InvalidIdException,
            InvalidAccessTokenException,
            InvalidIdentityException,
            NotVerifiedEmailException,
            OtherApplicationException {
        validate(user);
        mChatBoxIdValidator.validate(chatboxId);

        if (!mPermissionsValidator.canBookmark(user)) {
            throw new InvalidIdentityException(new ChatWingError(
                    ChatWingError.ERROR_CODE_INVALID_IDENTITY,
                    mContext.getString(R.string.error_required_chat_wing_login),
                    null));
        }

        Gson gson = new Gson();

        CreateBookmarkParams params = new CreateBookmarkParams();
        params.setChatboxId(chatboxId);
        String paramsString = gson.toJson(params);

        HttpRequest request = HttpRequest.post(BOOKMARK_CREATE);
        setUpRequest(request, user);
        request.send(paramsString);

        String responseString = null;
        try {
            responseString = validate(request);
            CreateBookmarkResponse createBookmarkResponse = gson.fromJson(responseString,
                    CreateBookmarkResponse.class);
            return createBookmarkResponse;
        } catch (JsonSyntaxException e) {
            throw ApiException.createJsonSyntaxException(e, responseString);
        } catch (ValidationException e) {
            throw ApiException.createException(e);
        }
    }

    @Override
    public BookmarkResponse loadBookmarks(User user)
            throws ApiException,
            HttpRequest.HttpRequestException,
            UserUnauthenticatedException,
            InvalidAccessTokenException,
            InvalidIdentityException,
            NotVerifiedEmailException,
            OtherApplicationException {
        validate(user);

        if (!mPermissionsValidator.canBookmark(user)) {
            throw new InvalidIdentityException(new ChatWingError(
                    ChatWingError.ERROR_CODE_INVALID_IDENTITY,
                    mContext.getString(R.string.error_required_chat_wing_login),
                    null));
        }

        LoadBookmarkParams params = new LoadBookmarkParams();
        Gson gson = new Gson();
        HttpRequest request = HttpRequest.get(BOOKMARK_LIST + appendParams(params));
        setUpRequest(request, user);
        String responseString = null;

        try {
            responseString = validate(request);
            BookmarkResponse bookmarksResponse = gson.fromJson(responseString, BookmarkResponse.class);
            return bookmarksResponse;
        } catch (JsonSyntaxException e) {
            throw ApiException.createJsonSyntaxException(e, responseString);
        } catch (ValidationException e) {
            throw ApiException.createException(e);
        }
    }

    @Override
    public FlagMessageResponse flagMessage(User user, String messageID)
            throws UserUnauthenticatedException,
            HttpRequest.HttpRequestException,
            ApiException,
            InvalidAccessTokenException,
            NotVerifiedEmailException,
            OtherApplicationException {
        validate(user);

        Gson gson = new Gson();

        FlagMessageParams params = new FlagMessageParams(messageID);
        String paramsString = gson.toJson(params);

        HttpRequest request = HttpRequest.post(FLAG_MESSAGE);
        setUpRequest(request, user);
        request.send(paramsString);
        String responseString;
        try {
            responseString = validate(request);
            return gson.fromJson(responseString, FlagMessageResponse.class);
        } catch (ValidationException e) {
            throw ApiException.createException(e);
        } catch (InvalidIdentityException e) {
            throw ApiException.createException(e);
        }
    }

    @Override
    public String getDisplayUserLoginType(String loginType) {
        if (mBuildManager.isCustomLoginType()) {
            return mContext.getString(R.string.app_name);
        }

        if (Constants.TYPE_CHATWING.equals(loginType)) {
            return mContext.getString(R.string.app_name);
        }
        return loginType;
    }
}
