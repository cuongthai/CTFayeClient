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
 * Created by nguyenthanhhuy on 1/4/14.
 */
public class ChatBoxKeyValidator {
    public static class InvalidKeyException extends Exception {
    }

    @Inject
    public ChatBoxKeyValidator() {
    }

    public void validate(String chatBoxKey) throws InvalidKeyException {
        if (!isValid(chatBoxKey)) {
            throw new InvalidKeyException();
        }
    }

    public boolean isValid(String chatBoxKey) {
        return chatBoxKey != null && !chatBoxKey.trim().isEmpty();
    }
}
