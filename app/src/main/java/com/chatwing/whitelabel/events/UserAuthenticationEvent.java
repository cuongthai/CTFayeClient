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

package com.chatwing.whitelabel.events;


import com.chatwing.whitelabel.pojos.params.oauth.AuthenticationParams;

/**
 * Author: Huy Nguyen
 * Date: 5/26/13
 * Time: 10:38 PM
 */
public class UserAuthenticationEvent {

    public static UserAuthenticationEvent succeedEvent(String tag,
                                                       AuthenticationParams params) {
        UserAuthenticationEvent event = new UserAuthenticationEvent();
        event.status = Status.SUCCEED;
        event.tag = tag;
        event.params = params;
        return event;
    }

    public static UserAuthenticationEvent canceledEvent(String tag) {
        UserAuthenticationEvent event = new UserAuthenticationEvent();
        event.status = Status.CANCELED;
        event.tag = tag;
        return event;
    }

    public static UserAuthenticationEvent failedEvent(String tag, Exception exc) {
        UserAuthenticationEvent event = new UserAuthenticationEvent();
        event.status = Status.FAILED;
        event.tag = tag;
        event.exception = exc;
        return event;
    }

    public enum Status {
        SUCCEED,
        CANCELED,
        FAILED
    }

    private Status status;
    private String tag;
    private AuthenticationParams params;
    private Exception exception;

    private UserAuthenticationEvent() {
    }

    public Status getStatus() {
        return status;
    }

    public String getTag() {
        return tag;
    }

    public AuthenticationParams getParams() {
        return params;
    }

    public Exception getException() {
        return exception;
    }
}
