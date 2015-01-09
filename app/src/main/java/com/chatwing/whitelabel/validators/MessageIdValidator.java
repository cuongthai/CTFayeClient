package com.chatwing.whitelabel.validators;

import android.text.TextUtils;

import javax.inject.Inject;

/**
 * Created by nguyenthanhhuy on 12/21/13.
 */
public class MessageIdValidator {
    public static class InvalidIdException extends Exception {
    }

    @Inject
    MessageIdValidator() {
    }

    public void validate(String id) throws InvalidIdException {
        if (TextUtils.isEmpty(id)) {
            throw new InvalidIdException();
        }
    }
}
