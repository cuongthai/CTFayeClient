package com.chatwing.whitelabel.managers;

import android.content.Context;

import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.pojos.params.ResetPasswordParams;
import com.chatwing.whitelabel.pojos.responses.RegisterResponse;
import com.chatwing.whitelabel.pojos.responses.ResetPasswordResponse;
import com.chatwing.whitelabel.validators.EmailValidator;
import com.chatwing.whitelabel.validators.PasswordValidator;
import com.chatwingsdk.managers.ApiManagerImpl;
import com.chatwingsdk.modules.ForApplication;
import com.chatwingsdk.pojos.params.RegisterParams;
import com.github.kevinsawicki.http.HttpRequest;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

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
}
