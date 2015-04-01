package com.chatwing.whitelabel.managers;

import com.chatwing.whitelabel.fragments.ExtendChatMessagesFragment;
import com.chatwing.whitelabel.pojos.OnlineUser;
import com.chatwing.whitelabel.pojos.responses.BlackListResponse;
import com.chatwing.whitelabel.pojos.responses.BookmarkResponse;
import com.chatwing.whitelabel.pojos.responses.CreateBookmarkResponse;
import com.chatwing.whitelabel.pojos.responses.CreateChatBoxResponse;
import com.chatwing.whitelabel.pojos.responses.DeleteBookmarkResponse;
import com.chatwing.whitelabel.pojos.responses.DeleteMessageResponse;
import com.chatwing.whitelabel.pojos.responses.FlagMessageResponse;
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
import com.chatwingsdk.Constants;
import com.chatwingsdk.pojos.Message;
import com.chatwingsdk.pojos.User;
import com.chatwingsdk.pojos.jspojos.JSUserResponse;
import com.chatwingsdk.pojos.responses.UserResponse;
import com.chatwingsdk.validators.ChatBoxIdValidator;
import com.github.kevinsawicki.http.HttpRequest;

/**
 * Created by cuongthai on 26/10/2014.
 */
public interface ApiManager extends com.chatwingsdk.managers.ApiManager {
    String GUEST_AVATAR_URL = Constants.CHATWING_BASE_URL + "/images/avatars/%s";
    String SUBMITTED_GUEST_AVATAR_URL = "avatars/%s";
    String REGISTER_URL = URL_END_POINT + "/chat-user/register";
    String RESET_PASSWORD_URL = URL_END_POINT + "/user/password/reset";
    String CHAT_BOX_USER_LIST_URL = URL_END_POINT + "/chatbox/user/list";
    String USER_PROFILE_UPDATE_URL = URL_END_POINT + "/chat-user/profile/update";
    String UPLOAD_AVATAR = URL_END_POINT + "/chat-user/avatar/upload";
    String CHAT_BOX_DELETE_MESSAGE_URL = URL_END_POINT + "/chatbox/message/delete";
    String BLACKLIST_CREATE_URL = URL_END_POINT + "/chatbox/blacklist/create";
    String MANAGE_BLACKLIST_URL = Constants.CHATWING_BASE_URL + "/chatbox/%s/control?access_token=%s&client_id="
            + ChatWing.getAppId();
    String USER_IGNORE= URL_END_POINT + "/chat-user/ignore";
    String FLAG_MESSAGE= URL_END_POINT + "/chatbox/message/flag";
    String USER_UNIGNORE= URL_END_POINT + "/chat-user/unignore";
    String USER_DETAIL = URL_END_POINT + "/chat-user/read";
    String USER_VERIFY = URL_END_POINT + "/chat-user/verify";
    String CHAT_BOX_SEARCH_URL = URL_END_POINT + "/chatbox/search";
    String CHAT_BOX_CREATE_URL = URL_END_POINT + "/user/chatbox/create";
    String BOOKMARK_DELETE = URL_END_POINT + "/user/bookmark/delete";
    String BOOKMARK_CREATE = URL_END_POINT + "/user/bookmark/create";
    String BOOKMARK_LIST = URL_END_POINT + "/user/bookmark/list";

    DeleteMessageResponse deleteMessage(User user,
                                        int chatBoxId,
                                        String messageId)
            throws ApiException,
            HttpRequest.HttpRequestException,
            ChatBoxIdValidator.InvalidIdException,
            MessageIdValidator.InvalidIdException,
            UserUnauthenticatedException,
            InvalidIdentityException,
            InvalidAccessTokenException,
            RequiredPermissionException;

    BlackListResponse blockUser(User user,
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
            RequiredPermissionException;

    ResetPasswordResponse resetPassword(String email)
            throws ApiException,
            HttpRequest.HttpRequestException,
            EmailValidator.InvalidEmailException,
            ValidationException;

    IgnoreUserResponse ignoreUser(User user,
                                  String userId,
                                  String userType,
                                  boolean ignored)
            throws UserUnauthenticatedException,
            HttpRequest.HttpRequestException,
            ApiException,
            InvalidAccessTokenException;

    RegisterResponse register(String email,
                              String password,
                              boolean agreeConditions,
                              boolean autoCreateChatBox)
            throws ApiException,
            HttpRequest.HttpRequestException,
            EmailValidator.InvalidEmailException,
            PasswordValidator.InvalidPasswordException,
            ValidationException;

    public String getChatBoxUrl(String chatBoxKey);

    UserResponse loadUserDetails(User user)
            throws UserUnauthenticatedException,
            ApiException,
            HttpRequest.HttpRequestException,
            InvalidAccessTokenException;

    LoadOnlineUsersResponse loadOnlineUsers(int chatBoxId)
            throws ApiException,
            HttpRequest.HttpRequestException,
            ChatBoxIdValidator.InvalidIdException;

    int getLoginTypeImageResId(String type);

    String getAvatarUrl(OnlineUser user);

    public UpdateUserProfileResponse updateUserProfile(User user)
            throws ApiException,
            HttpRequest.HttpRequestException,
            UserUnauthenticatedException,
            ValidationException,
            InvalidAccessTokenException;

    JSUserResponse updateAvatar(User currentUser,
                              String path)
            throws UserUnauthenticatedException,
            HttpRequest.HttpRequestException,
            ApiException;

    void verifyEmail(User user)
            throws UserUnauthenticatedException,
            HttpRequest.HttpRequestException, ApiException;

    SearchChatBoxResponse searchChatBox(String query,
                                        int offset,
                                        int limit)
            throws ApiException,
            ValidationException;

    CreateChatBoxResponse createChatBox(User user,
                                        String name)
            throws UserUnauthenticatedException,
            ApiException,
            InvalidIdentityException,
            InvalidAccessTokenException;

    String getFullChatBoxAliasUrl(String alias);

    DeleteBookmarkResponse deleteBookmark(User user,
                                          Integer bookmarkId)
            throws ApiException,
            HttpRequest.HttpRequestException,
            UserUnauthenticatedException,
            InvalidAccessTokenException,
            InvalidIdentityException;

    CreateBookmarkResponse createBookmark(User user,
                                          int chatboxId)
            throws ApiException,
            HttpRequest.HttpRequestException,
            UserUnauthenticatedException,
            ChatBoxIdValidator.InvalidIdException,
            InvalidAccessTokenException,
            InvalidIdentityException;

    BookmarkResponse loadBookmarks(User user)
            throws ApiException,
            HttpRequest.HttpRequestException,
            UserUnauthenticatedException,
            InvalidAccessTokenException,
            InvalidIdentityException;

    FlagMessageResponse flagMessage(User currentUser, String messageID)
            throws UserUnauthenticatedException,
            HttpRequest.HttpRequestException,
            ApiException,
            InvalidAccessTokenException ;

    public static class RequiredPermissionException extends Exception {
    }
}
