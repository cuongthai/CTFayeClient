package com.chatwing.whitelabel.managers;

import com.chatwing.whitelabel.ChatWing;
import com.chatwing.whitelabel.Constants;
import com.chatwing.whitelabel.fragments.ExtendChatMessagesFragment;
import com.chatwing.whitelabel.pojos.Message;
import com.chatwing.whitelabel.pojos.OnlineUser;
import com.chatwing.whitelabel.pojos.User;
import com.chatwing.whitelabel.pojos.errors.ChatWingError;
import com.chatwing.whitelabel.pojos.errors.CreateMessageParamsError;
import com.chatwing.whitelabel.pojos.jspojos.JSUserResponse;
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
import com.chatwing.whitelabel.utils.LogUtils;
import com.chatwing.whitelabel.validators.ChatBoxIdValidator;
import com.chatwing.whitelabel.validators.ConversationIdValidator;
import com.chatwing.whitelabel.validators.EmailValidator;
import com.chatwing.whitelabel.validators.MessageIdValidator;
import com.chatwing.whitelabel.validators.PasswordValidator;
import com.github.kevinsawicki.http.HttpRequest;

/**
 * Created by cuongthai on 26/10/2014.
 */
public interface ApiManager {
    String GCM_ACTION_ADD = "add";
    String GCM_ACTION_REMOVE = "remove";

    String API_VERSION = "/api/3";
    String URL_END_POINT = Constants.CHATWING_BASE_URL + API_VERSION;

    String DEFAULT_AVATAR_URL = Constants.CHATWING_BASE_URL + "/images/no-avatar.gif";
    String AVATAR_PATH = Constants.CHATWING_BASE_URL + "/user/avatar/%s/%s?size=" + Constants.AVATAR_SIZE;

    String CHATBOX_URL = Constants.CHATWING_BASE_URL + "/chatbox/%s/message-list";

    String CONVERSATION_CREATE_URL = URL_END_POINT + "/chat-user/conversation/create";
    String CONVERSATION_SUBSCRIBE_URL = URL_END_POINT + "/chat-user/conversation/notification/subscribe";
    String CONVERSATION_UNSUBSCRIBE_URL = URL_END_POINT + "/chat-user/conversation/notification/unsubscribe";
    String CONVERSATION_NOTIFICATION_STATUS_URL = URL_END_POINT + "/chat-user/conversation/notification/status";
    String AUTHENTICATE_URL = URL_END_POINT + "/chat-user/authenticate";
    String CHAT_BOX_LIST_URL = URL_END_POINT + "/chatbox/list";
    String CHAT_BOX_DETAIL_URL = URL_END_POINT + "/chatbox/read";
    String CHAT_BOX_SUBSCRIBE_URL = URL_END_POINT + "/chatbox/notification/subscribe";
    String CHAT_BOX_UNSUBSCRIBE_URL = URL_END_POINT + "/chatbox/notification/unsubscribe";
    String CHAT_BOX_NOTIFICATION_STATUS_URL = URL_END_POINT + "/chatbox/notification/status";


    String CONVERSATION_LIST_URL = URL_END_POINT + "/chat-user/conversation/list";
    String MODERATOR_LIST_URL = URL_END_POINT + "/app/moderator/list";
    String ADD_GCM_URL = URL_END_POINT + "/chat-user/gcm/add";
    String REMOVE_GCM_URL = URL_END_POINT + "/chat-user/gcm/remove";
    String PING_URL = URL_END_POINT + "/chat-user/ping";
    String OFFLINE_URL = URL_END_POINT + "/chat-user/offline";

    String CHAT_BOX_CREATE_MESSAGE_URL = URL_END_POINT + "/chatbox/message/create";
    String CHAT_BOX_MESSAGES_COUNT_URL = URL_END_POINT + "/chatbox/message/unread";
    String CONVERSATION_CREATE_MESSAGE_URL = URL_END_POINT + "/chat-user/conversation/message/create";

    String DEFAULT_EMOTICON_URL = "";
    String CONVERSATION_URL = Constants.CHATWING_BASE_URL + "/conversation/%s/message-list";
    String CONVERSATION_ACK_URL = URL_END_POINT + "/chat-user/conversation/ack";
    String CHATBOX_ACK_URL = URL_END_POINT + "/chatbox/ack";

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
            + ChatWing.getClientID();
    String USER_IGNORE = URL_END_POINT + "/chat-user/ignore";
    String FLAG_MESSAGE = URL_END_POINT + "/chatbox/message/flag";
    String USER_UNIGNORE = URL_END_POINT + "/chat-user/unignore";
    String USER_DETAIL = URL_END_POINT + "/chat-user/read";
    String USER_VERIFY = URL_END_POINT + "/chat-user/verify";
    String CHAT_BOX_SEARCH_URL = URL_END_POINT + "/chatbox/search";
    String CHAT_BOX_CREATE_URL = URL_END_POINT + "/user/chatbox/create";
    String BOOKMARK_DELETE = URL_END_POINT + "/user/bookmark/delete";
    String BOOKMARK_CREATE = URL_END_POINT + "/user/bookmark/create";
    String BOOKMARK_LIST = URL_END_POINT + "/user/bookmark/list";
    String CHAT_BOX_MESSAGES_URL = URL_END_POINT + "/chatbox/message/list";
    String CONVERSATION_MESSAGES_URL = URL_END_POINT + "/chat-user/conversation/message/list";

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
            RequiredPermissionException,
            NotVerifiedEmailException, OtherApplicationException;

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
            RequiredPermissionException,
            NotVerifiedEmailException, OtherApplicationException;

    ResetPasswordResponse resetPassword(String email)
            throws ApiException,
            HttpRequest.HttpRequestException,
            EmailValidator.InvalidEmailException,
            ValidationException,
            NotVerifiedEmailException, OtherApplicationException;

    MessagesResponse loadMessages(User user,
                                  int chatBoxId,
                                  Message oldestMessage)
            throws ApiException,
            HttpRequest.HttpRequestException,
            ChatBoxIdValidator.InvalidIdException,
            InvalidAccessTokenException,
            InvalidIdentityException,
            NotVerifiedEmailException, OtherApplicationException;

    MessagesResponse loadMessages(User user,
                                  String conversationId,
                                  Message oldestMessage)
            throws ApiException,
            HttpRequest.HttpRequestException,
            ConversationIdValidator.InvalidIdException,
            UserUnauthenticatedException,
            InvalidAccessTokenException,
            InvalidIdentityException,
            NotVerifiedEmailException, OtherApplicationException;

    IgnoreUserResponse ignoreUser(User user,
                                  String userId,
                                  String userType,
                                  boolean ignored)
            throws UserUnauthenticatedException,
            HttpRequest.HttpRequestException,
            ApiException,
            InvalidAccessTokenException,
            NotVerifiedEmailException, OtherApplicationException;

    RegisterResponse register(String email,
                              String password,
                              boolean agreeConditions,
                              boolean autoCreateChatBox)
            throws ApiException,
            HttpRequest.HttpRequestException,
            EmailValidator.InvalidEmailException,
            PasswordValidator.InvalidPasswordException,
            ValidationException,
            InvalidAgreeConditionsException,
            OtherApplicationException;

    String getChatBoxUrl(String chatBoxKey);

    UserResponse loadUserDetails(User user)
            throws UserUnauthenticatedException,
            ApiException,
            HttpRequest.HttpRequestException,
            InvalidAccessTokenException,
            NotVerifiedEmailException, OtherApplicationException;

    LoadOnlineUsersResponse loadOnlineUsers(int chatBoxId)
            throws ApiException,
            HttpRequest.HttpRequestException,
            ChatBoxIdValidator.InvalidIdException,
            NotVerifiedEmailException, OtherApplicationException;

    int getLoginTypeImageResId(String type);

    String getAvatarUrl(OnlineUser user);

    String getAvatarUrl(Message message);


    UpdateUserProfileResponse updateUserProfile(User user)
            throws ApiException,
            HttpRequest.HttpRequestException,
            UserUnauthenticatedException,
            ValidationException,
            InvalidAccessTokenException,
            NotVerifiedEmailException, OtherApplicationException;

    JSUserResponse updateAvatar(User currentUser,
                                String path)
            throws UserUnauthenticatedException,
            HttpRequest.HttpRequestException,
            ApiException,
            NotVerifiedEmailException, OtherApplicationException;

    void verifyEmail(User user)
            throws UserUnauthenticatedException,
            HttpRequest.HttpRequestException,
            ApiException,
            NotVerifiedEmailException, OtherApplicationException;

    SearchChatBoxResponse searchChatBox(String query,
                                        int offset,
                                        int limit)
            throws ApiException,
            ValidationException, NotVerifiedEmailException, OtherApplicationException;

    CreateChatBoxResponse createChatBox(User user,
                                        String name)
            throws UserUnauthenticatedException,
            ApiException,
            InvalidIdentityException,
            InvalidAccessTokenException,
            NotVerifiedEmailException, OtherApplicationException;

    String getFullChatBoxAliasUrl(String alias);

    DeleteBookmarkResponse deleteBookmark(User user,
                                          Integer bookmarkId)
            throws ApiException,
            HttpRequest.HttpRequestException,
            UserUnauthenticatedException,
            InvalidAccessTokenException,
            InvalidIdentityException,
            NotVerifiedEmailException, OtherApplicationException;

    CreateBookmarkResponse createBookmark(User user,
                                          int chatboxId)
            throws ApiException,
            HttpRequest.HttpRequestException,
            UserUnauthenticatedException,
            ChatBoxIdValidator.InvalidIdException,
            InvalidAccessTokenException,
            InvalidIdentityException,
            NotVerifiedEmailException, OtherApplicationException;

    BookmarkResponse loadBookmarks(User user)
            throws ApiException,
            HttpRequest.HttpRequestException,
            UserUnauthenticatedException,
            InvalidAccessTokenException,
            InvalidIdentityException,
            NotVerifiedEmailException, OtherApplicationException;

    FlagMessageResponse flagMessage(User currentUser, String messageID)
            throws UserUnauthenticatedException,
            HttpRequest.HttpRequestException,
            ApiException,
            InvalidAccessTokenException,
            NotVerifiedEmailException, OtherApplicationException;

    UpdateGcmResponse updateGcm(User user,
                                String gcmRegId,
                                String action)
            throws ApiException,
            HttpRequest.HttpRequestException,
            UserUnauthenticatedException,
            InvalidAccessTokenException,
            NotVerifiedEmailException, OtherApplicationException;


    ChatBoxListResponse loadChatBoxes()
            throws ApiException,
            HttpRequest.HttpRequestException,
            NotVerifiedEmailException, OtherApplicationException;

    String getFullEmoticonUrl(String emoticonPath);

    AuthenticationResponse authenticate(AuthenticationParams params)
            throws ApiException,
            HttpRequest.HttpRequestException,
            ValidationException,
            InvalidExternalAccessTokenException, OtherApplicationException;

    CreateConversationResponse createConversation(User user,
                                                  String loginId,
                                                  String loginType)
            throws ApiException,
            UserUnauthenticatedException,
            HttpRequest.HttpRequestException,
            InvalidAccessTokenException,
            InvalidIdentityException,
            NotVerifiedEmailException, OtherApplicationException;

    ChatBoxDetailsResponse loadChatBoxDetails(User user,
                                              int chatBoxId)
            throws ApiException,
            HttpRequest.HttpRequestException,
            ChatBoxIdValidator.InvalidIdException,
            ValidationException,
            InvalidIdentityException,
            NotVerifiedEmailException, OtherApplicationException;


    /**
     * Creates a new message.
     * Reference: http://docs.chatwing.com/api/1/chatbox/message/create.html
     *
     * @param user          current user.
     * @param messageParams information of the new message. It should only contains
     *                      chat box id, content and a random key (optional). If random key
     *                      is not provided, a new random key will be created and returned
     *                      by the server.
     * @return response from server.
     * If succeed, the response contains a new {@link com.chatwing.whitelabel.pojos.Message} which
     * contains updated information from the server.
     * If failed, appropriate exception will be thrown.
     * The exception contains general {@link com.chatwing.whitelabel.pojos.errors.ChatWingError}
     * which contains params can be parsed to {@link com.chatwing.whitelabel.pojos.errors.CreateMessageParamsError}.
     * The original provided {@link com.chatwing.whitelabel.pojos.Message} (params) will be
     * manually added to the response. It can be used while handling
     * the error (remove from view, for example). Thus, it is important
     * that the client of this method checks for error before message.
     * @throws UserUnauthenticatedException
     * @throws ApiException
     * @throws HttpRequest.HttpRequestException
     * @throws ChatBoxIdValidator.InvalidIdException
     * @throws com.chatwing.whitelabel.validators.ConversationIdValidator.InvalidIdException
     * @throws InvalidIdentityException
     * @throws InvalidAccessTokenException
     * @throws CreateMessageException
     */
    CreateMessageResponse createMessage(User user,
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
            OtherApplicationException;

    LoadConversationsResponse loadConversations(User user,
                                                int limit,
                                                int offset)
            throws ApiException,
            HttpRequest.HttpRequestException,
            UserUnauthenticatedException,
            InvalidAccessTokenException,
            InvalidIdentityException,
            NotVerifiedEmailException,
            OtherApplicationException;

    LoadModeratorsResponse loadModerators(User user,
                                          int limit,
                                          int offset)
            throws ApiException,
            HttpRequest.HttpRequestException,
            UserUnauthenticatedException,
            InvalidAccessTokenException,
            InvalidIdentityException,
            NotVerifiedEmailException,
            OtherApplicationException;

    String getAvatarUrl(User user);

    String getAvatarUrl(String userType, String userID, String avatar);

    String getUserProfileUrl(String loginId, String loginType);

    String getDisplayUserLoginType(String loginType);

    void ackConversation(User user,
                         String conversationId)
            throws ApiException,
            HttpRequest.HttpRequestException,
            UserUnauthenticatedException,
            ConversationIdValidator.InvalidIdException,
            InvalidAccessTokenException,
            InvalidIdentityException,
            NotVerifiedEmailException, OtherApplicationException;

    void ackChatbox(User user,
                    Integer chatboxID)
            throws ApiException,
            HttpRequest.HttpRequestException,
            UserUnauthenticatedException,
            ChatBoxIdValidator.InvalidIdException,
            InvalidAccessTokenException,
            InvalidIdentityException,
            NotVerifiedEmailException,
            OtherApplicationException;


    SubscriptionStatusResponse loadCommunicationSetting(User user,
                                                        int chatboxID)
            throws ApiException,
            HttpRequest.HttpRequestException,
            UserUnauthenticatedException,
            ChatBoxIdValidator.InvalidIdException,
            InvalidAccessTokenException,
            InvalidIdentityException,
            NotVerifiedEmailException,
            OtherApplicationException;

    SubscriptionStatusResponse loadCommunicationSetting(User user,
                                                        String conversationID)
            throws ApiException,
            HttpRequest.HttpRequestException,
            UserUnauthenticatedException,
            ConversationIdValidator.InvalidIdException,
            InvalidAccessTokenException,
            InvalidIdentityException,
            NotVerifiedEmailException, OtherApplicationException;

    SubscriptionResponse updateNotificationSubscription(User user,
                                                        String action,
                                                        int chatboxID,
                                                        String target)
            throws ApiException,
            HttpRequest.HttpRequestException,
            UserUnauthenticatedException,
            ChatBoxIdValidator.InvalidIdException,
            InvalidAccessTokenException,
            NotVerifiedEmailException, OtherApplicationException;

    SubscriptionResponse updateNotificationSubscription(User user,
                                                        String action,
                                                        String conversationID,
                                                        String target)
            throws ApiException,
            HttpRequest.HttpRequestException,
            UserUnauthenticatedException,
            ConversationIdValidator.InvalidIdException,
            InvalidAccessTokenException,
            NotVerifiedEmailException, OtherApplicationException;

    void ping(User user)
            throws ApiException,
            HttpRequest.HttpRequestException,
            UserUnauthenticatedException, OtherApplicationException;

    void offline(User currentUser)
            throws ApiException,
            HttpRequest.HttpRequestException,
            UserUnauthenticatedException, OtherApplicationException;

    UnreadCountResponse getUnreadCountForChatbox(User user,
                                                 Integer chatboxId)
            throws ApiException,
            HttpRequest.HttpRequestException,
            UserUnauthenticatedException,
            NotVerifiedEmailException,
            OtherApplicationException;

    ////////////////////////////////////////////////////////////////
    //                  Server side logic exceptions
    ///////////////////////////////////////////////////////////////
    /**
     * thrown when access token is expired.
     * Since access token is not expired for normal account but Guest.
     * The account associate with this access token need to be recreated
     */
    class InvalidAccessTokenException extends ChatWingException {
        public InvalidAccessTokenException(ChatWingError error) {
            super(error);
        }
    }

    /**
     * thrown when user needs another identity to access resource.
     * Eg. Needs different login type
     */
    class InvalidIdentityException extends ChatWingException {
        public InvalidIdentityException(ChatWingError error) {
            super(error);
        }
    }

    /**
     * throws when user input doesn't follow format
     */
    class ValidationException extends ChatWingException {
        public ValidationException(ChatWingError error) {
            super(error);
        }
    }

    /**
     * thrown when server fails to fetch data from social accounts
     */
    class InvalidExternalAccessTokenException extends ChatWingException {
        public InvalidExternalAccessTokenException(ChatWingError error) {
            super(error);
        }
    }

    /**
     * thrown when there was an error from server.
     * We need to pass necessary to the exception to update the view
     */
    class CreateMessageException extends ChatWingException {
        private final CreateMessageParamsError createMessageParamsError;
        private final Message message;

        public CreateMessageException(ChatWingError error, CreateMessageParamsError createMessageParamsError,
                                      Message message) {
            super(error);
            this.createMessageParamsError = createMessageParamsError;
            this.message = message;
        }

        public CreateMessageParamsError getCreateMessageParamsError() {
            return createMessageParamsError;
        }

        public Message getCommunicationMessage() {
            return message;
        }
    }

    /**
     * thrown when user haven't verified his/her email
     */
    class NotVerifiedEmailException extends ChatWingException {
        public NotVerifiedEmailException(ChatWingError error) {
            super(error);
        }
    }

    /**
     * throws when server want to tell something directly to user! Yay!!
     */
    class OtherApplicationException extends ChatWingException {
        public OtherApplicationException(ChatWingError error) {
            super(error);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                  Client Side/API Exception
    ///////////////////////////////////////////////////////////////////

    /**
     * ChatwingException wrap known chatwing server error
     */
    class ChatWingException extends Exception {
        private final ChatWingError error;

        public ChatWingException(ChatWingError error) {
            super(error.getMessage());
            this.error = error;
        }

        public ChatWingError getError() {
            return error;
        }
    }

    /**
     * This exception should be due to HTTP response code is not 200.
     * When this happens, we should not give any detail to user since it's likely due to api/server failure
     */
    class ApiException extends Exception {
        /**
         * Server returns stupid thing so we can't parse. Let's report it
         * @param e
         * @param json stupid json
         * @return
         */
        public static ApiException createJsonSyntaxException(Exception e, String json) {
            if (json != null) {
                //hmm... We cant parse json, something wrong, let's report to server
                LogUtils.e("Failed to parse this json " + json);
            }
            return createException(e);
        }

        public static ApiException createException(Exception e) {
            if (Constants.DEBUG) {
                return new ApiException(e.getMessage());
            }
            return new ApiException("There was an error while requesting to ChatWing. Please try again later");
        }

        private ApiException(String detailMessage) {
            super(detailMessage);
        }
    }

    /**
     * throws when client does something but doesn't have permission to do that
     */
    class RequiredPermissionException extends Exception {
    }

    /**
     * thrown when there is no account associate with the app.
     * This is for client-side checking. Usually, a friendly message would be displayed to user
     */
    class UserUnauthenticatedException extends Exception {
    }

    class InvalidAgreeConditionsException extends Exception {
        public InvalidAgreeConditionsException(String msg) {
            super(msg);
        }
    }
}
