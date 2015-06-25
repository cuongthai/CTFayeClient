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

package com.chatwing.whitelabel.validators;

import android.text.TextUtils;

import javax.inject.Inject;

/**
 * Created by cuongthai on 4/2/14.
 */
public class ConversationIdValidator {
    public static class InvalidIdException extends Exception {
    }

    @Inject
    public ConversationIdValidator() {
    }

    /**
     * Validates the given key and throw InvalidIdException if it is invalid.
     * @param key the key to be validated
     * @throws InvalidIdException then the key is invalid.
     */
    public void validate(String key) throws InvalidIdException {
        if (!isValid(key)) {
            throw new InvalidIdException();
        }
    }

    /**
     * Validates the given id.
     * @param key the id to be validated
     * @return true if the id is valid, false otherwise.
     */
    public boolean isValid(String key) {
        return !TextUtils.isEmpty(key);
    }
}
