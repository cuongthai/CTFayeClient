package com.chatwing.whitelabel.validators;

import javax.inject.Inject;

/**
 * User: nguyenthanhhuy
 * Date: 11/24/13
 * Time: 12:21 PM
 */
public class EmailValidator {
    public static class InvalidEmailException extends Exception {
    }

    @Inject
    EmailValidator() {
    }

    public void validate(String email) throws InvalidEmailException {
        if (email == null || (email = email.trim()).isEmpty() || !email.contains("@")) {
            throw new InvalidEmailException();
        }
    }
}
