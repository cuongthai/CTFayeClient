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

import javax.inject.Inject;

/**
 * User: nguyenthanhhuy
 * Date: 11/24/13
 * Time: 12:17 PM
 */
public class ChatBoxIdValidator {
    public static class InvalidIdException extends Exception {
    }

    @Inject
    public ChatBoxIdValidator() {
    }

    /**
     * Validates the given id and throw InvalidIdException if it is invalid.
     * @param id the id to be validated
     * @throws InvalidIdException then the id is invalid.
     */
    public void validate(int id) throws InvalidIdException {
        if (!isValid(id)) {
            throw new InvalidIdException();
        }
    }

    /**
     * Validates the given id.
     * @param id the id to be validated
     * @return true if the id is valid, false otherwise.
     */
    public boolean isValid(int id) {
        return id > 0;
    }
}
