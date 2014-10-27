package com.chatwing.whitelabel.validators;

import android.text.TextUtils;

import javax.inject.Inject;

public class PasswordValidator {
    public static class InvalidPasswordException extends Exception {
    }

    @Inject
    PasswordValidator() {
    }

    public void validate(String password) throws InvalidPasswordException {
        if (TextUtils.isEmpty(password)) {
            throw new InvalidPasswordException();
        }
    }
}
