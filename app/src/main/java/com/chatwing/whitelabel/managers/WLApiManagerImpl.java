package com.chatwing.whitelabel.managers;

import android.content.Context;
import android.text.TextUtils;

import com.chatwing.whitelabel.Constants;
import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.fragments.ExtendChatMessagesFragment;
import com.chatwing.whitelabel.pojos.OnlineUser;
import com.chatwing.whitelabel.pojos.params.BlockUserParams;
import com.chatwing.whitelabel.pojos.params.CreateBookmarkParams;
import com.chatwing.whitelabel.pojos.params.CreateChatBoxParams;
import com.chatwing.whitelabel.pojos.params.DeleteBookmarkParams;
import com.chatwing.whitelabel.pojos.params.DeleteMessageParams;
import com.chatwing.whitelabel.pojos.params.IgnoreUserParams;
import com.chatwing.whitelabel.pojos.params.LoadBookmarkParams;
import com.chatwing.whitelabel.pojos.params.OnlineUserParams;
import com.chatwing.whitelabel.pojos.params.ResetPasswordParams;
import com.chatwing.whitelabel.pojos.params.SearchChatBoxParams;
import com.chatwing.whitelabel.pojos.params.UpdateUserProfileParams;
import com.chatwing.whitelabel.pojos.responses.BlackListResponse;
import com.chatwing.whitelabel.pojos.responses.BookmarkResponse;
import com.chatwing.whitelabel.pojos.responses.CreateBookmarkResponse;
import com.chatwing.whitelabel.pojos.responses.CreateChatBoxResponse;
import com.chatwing.whitelabel.pojos.responses.DeleteBookmarkResponse;
import com.chatwing.whitelabel.pojos.responses.DeleteMessageResponse;
import com.chatwing.whitelabel.pojos.responses.IgnoreUserResponse;
import com.chatwing.whitelabel.pojos.responses.LoadOnlineUsersResponse;
import com.chatwing.whitelabel.pojos.responses.RegisterResponse;
import com.chatwing.whitelabel.pojos.responses.ResetPasswordResponse;
import com.chatwing.whitelabel.pojos.responses.SearchChatBoxResponse;
import com.chatwing.whitelabel.pojos.responses.UpdateUserProfileResponse;
import com.chatwing.whitelabel.validators.EmailValidator;
import com.chatwing.whitelabel.validators.MessageIdValidator;
import com.chatwing.whitelabel.validators.PasswordValidator;
import com.chatwingsdk.ChatWing;
import com.chatwingsdk.managers.ApiManagerImpl;
import com.chatwingsdk.modules.ForApplication;
import com.chatwingsdk.pojos.Message;
import com.chatwingsdk.pojos.User;
import com.chatwingsdk.pojos.errors.ChatWingError;
import com.chatwingsdk.pojos.jspojos.JSUserResponse;
import com.chatwingsdk.pojos.params.ConcreteParams;
import com.chatwingsdk.pojos.params.RegisterParams;
import com.chatwingsdk.pojos.responses.UserResponse;
import com.chatwingsdk.validators.ChatBoxIdValidator;
import com.chatwingsdk.validators.PermissionsValidator;
import com.github.kevinsawicki.http.HttpRequest;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.File;

import javax.inject.Inject;

/**
 * Created by cuongthai on 27/10/2014.
 */
public class WLApiManagerImpl extends ApiManagerImpl implements ApiManager {
    @Inject
    EmailValidator mEmailValidator;
    @Inject
    PasswordValidator mPasswordValidator;
    @Inject
    @ForApplication
    Context mContext;
    @Inject
    ChatBoxIdValidator mChatBoxIdValidator;
    @Inject
    MessageIdValidator mMessageIdValidator;
    @Inject
    PermissionsValidator mPermissionsValidator;

    @Override
    public ResetPasswordResponse resetPassword(String email)
            throws ApiException,
            HttpRequest.HttpRequestException,
            EmailValidator.InvalidEmailException,
            ValidationException {
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
    public RegisterResponse register(String email,
                                     String password,
                                     boolean agreeConditions,
                                     boolean autoCreateChatBox)
            throws ApiException,
            HttpRequest.HttpRequestException,
            EmailValidator.InvalidEmailException,
            PasswordValidator.InvalidPasswordException,
            ValidationException {
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
            InvalidAccessTokenException {
        validate(user);

        Gson gson = new Gson();
        ConcreteParams paramsString = new ConcreteParams();
        HttpRequest request = HttpRequest.get(USER_DETAIL + appendParams(paramsString));
        setUpRequest(request, user);
        String responseString;
        try {
            responseString = validate(request);
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
            ChatBoxIdValidator.InvalidIdException {
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
            InvalidAccessTokenException {
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
            ApiException {
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
    public IgnoreUserResponse ignoreUser(User user,
                                         String userId,
                                         String userType,
                                         boolean ignored)
            throws UserUnauthenticatedException,
            HttpRequest.HttpRequestException,
            ApiException,
            InvalidAccessTokenException {
        validate(user);

        Gson gson = new Gson();

        IgnoreUserParams params = new IgnoreUserParams(userId, userType);
        String paramsString = gson.toJson(params);

        HttpRequest request;
        if (ignored) {
            request = HttpRequest.post(USER_UNIGNORE);
        } else {
            request = HttpRequest.post(USER_IGNORE);
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
            RequiredPermissionException {
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

    @Override
    public BlackListResponse blockUser(User user,
                                       ExtendChatMessagesFragment.BLOCK block,
                                       Message message,
                                       boolean clearMessage,
                                       String reason,
                                       long duration)
            throws ApiException,
            HttpRequest.HttpRequestException,
            UserUnauthenticatedException,
            ValidationException,
            InvalidAccessTokenException,
            RequiredPermissionException {
        validate(user);
        Gson gson = new Gson();

        HttpRequest request = HttpRequest.post(BLACKLIST_CREATE_URL);
        setUpRequest(request, user);
        BlockUserParams params;
        if (block == ExtendChatMessagesFragment.BLOCK.TYPE) {
            params = new BlockUserParams(
                    message.getUserId(),
                    message.getUserType(),
                    BlockUserParams.METHOD_SOCIAL,
                    clearMessage,
                    String.valueOf(message.getChatBoxId()),
                    duration,
                    reason);
        } else {
            params = new BlockUserParams(
                    message.getId(),
                    BlockUserParams.METHOD_IP,
                    clearMessage,
                    String.valueOf(message.getChatBoxId()),
                    duration,
                    reason);
        }
        request.send(gson.toJson(params));
        String responseString = null;
        try {
            responseString = validate(request);
            BlackListResponse blackListResponse = gson.fromJson(responseString, BlackListResponse.class);
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
            ApiException {
        validate(user);

        HttpRequest request = HttpRequest.post(UPLOAD_AVATAR);
        setUpRequest(request, user);
        //We have to specify this temp_avatar.jpg in order to upload the file...
        //https://github.com/kevinsawicki/http-request/issues/22
        request.part("avatar", "temp_avatar.jpg", new File(path));
        request.part("client_id", ChatWing.getAppId());
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
            ValidationException {
        if (TextUtils.isEmpty(query)) {
            throw new ValidationException(
                    new ChatWingError(ChatWingError.ERROR_CODE_VALIDATION_ERR,
                            mContext.getString(R.string.error_required_chat_wing_login_to_create_chat_boxes),
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
            InvalidIdentityException {
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
            InvalidIdentityException {
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
            InvalidIdentityException {
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
            InvalidIdentityException {
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
}
