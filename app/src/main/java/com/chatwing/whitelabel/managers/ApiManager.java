package com.chatwing.whitelabel.managers;

import com.chatwing.whitelabel.pojos.OnlineUser;
import com.chatwing.whitelabel.pojos.responses.LoadOnlineUsersResponse;
import com.chatwing.whitelabel.pojos.responses.RegisterResponse;
import com.chatwing.whitelabel.pojos.responses.ResetPasswordResponse;
import com.chatwing.whitelabel.pojos.responses.UpdateUserProfileResponse;
import com.chatwing.whitelabel.validators.EmailValidator;
import com.chatwing.whitelabel.validators.PasswordValidator;
import com.chatwingsdk.Constants;
import com.chatwingsdk.pojos.User;
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


    ResetPasswordResponse resetPassword(String email)
            throws ApiException,
            HttpRequest.HttpRequestException,
            EmailValidator.InvalidEmailException,
            ValidationException;

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

}
