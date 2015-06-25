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
 * Created by cuongthai on 25/06/2014.
 */
public class ChatWingError {
    public static final int ERROR_CODE_BAD_REQUEST = 400;
    public static final int ERROR_CODE_ACCESS_DENIED = 403;
    public static final int ERROR_CODE_VALIDATION_ERR = 40000;
    public static final int ERROR_CODE_APPLICATION_ERR = 40001;
    public static final int ERROR_CODE_EXTERNAL_ACCESS_TOKEN = 40002;
    public static final int ERROR_CODE_MISSING_ALL_REQUIRED_PARAMS = 40003;
    public static final int ERROR_CODE_MISSING_ONE_REQUIRED_PARAMS = 40004;
    public static final int ERROR_CODE_UNABLE_TO_SEND_MESSAGE = 40005;
    public static final int ERROR_CODE_MISSING_CLIENT_ID = 40300;
    public static final int ERROR_CODE_INVALID_ACCESS_TOKEN = 40301;
    public static final int ERROR_CODE_INVALID_IDENTITY = 40302;
    public static final int ERROR_CODE_PERMISSION_ERR = 40303;
    public static final int ERROR_CODE_VERIFY_EMAIL_ERR = 40304;


    protected int code;
    protected String message;
    protected JsonElement params;

    public ChatWingError(int code, String message, JsonElement params) {
        this.code = code;
        this.message = message;
        this.params = params;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public JsonElement getParams() {
        return params;
    }

    public static boolean hasExternalInvalidAcessToken(ChatWingError error) {
        if (error == null) return false;
        return error.getCode() == ERROR_CODE_EXTERNAL_ACCESS_TOKEN;
    }

    public static boolean hasValidationError(ChatWingError error) {
        if (error == null) return false;
        return error.getCode() == ERROR_CODE_VALIDATION_ERR;
    }

    public static boolean hasPermissionError(ChatWingError error) {
        if (error == null) return false;
        return error.getCode() == ERROR_CODE_PERMISSION_ERR;
    }
}
