package com.chatwing.whitelabel.managers;

import com.chatwing.whitelabel.pojos.responses.RegisterResponse;
import com.chatwing.whitelabel.pojos.responses.ResetPasswordResponse;
import com.chatwing.whitelabel.validators.EmailValidator;
import com.chatwing.whitelabel.validators.PasswordValidator;
import com.chatwingsdk.Constants;
import com.github.kevinsawicki.http.HttpRequest;

/**
 * Created by cuongthai on 26/10/2014.
 */
public interface ApiManager extends com.chatwingsdk.managers.ApiManager {
    String GUEST_AVATAR_URL = Constants.CHATWING_BASE_URL + "/images/avatars/%s";
    String SUBMITTED_GUEST_AVATAR_URL = "avatars/%s";
    String REGISTER_URL = URL_END_POINT + "/chat-user/register";
    String RESET_PASSWORD_URL = URL_END_POINT + "/user/password/reset";


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


}
