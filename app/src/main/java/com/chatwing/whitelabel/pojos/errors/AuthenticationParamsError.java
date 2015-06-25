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

package com.chatwing.whitelabel.pojos.errors;

import com.google.gson.JsonElement;

/**
 * Author: Huy Nguyen
 * Date: 9/5/13
 * Time: 5:13 PM
 */
public class AuthenticationParamsError {
    public static final String NAME_INTERNAL_OAUTH_ERROR = "InternalOAuthError";

    /**
     * Field for GooglePlus
     */
    private String name;

    private String message;

    private JsonElement oauthError;

    public String getName() {
        return name;
    }

    public String getMessage() {
        return message;
    }

    public JsonElement getOauthError() {
        return oauthError;
    }
}
