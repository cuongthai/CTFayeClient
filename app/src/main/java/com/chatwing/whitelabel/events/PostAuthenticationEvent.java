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

/**
 * Created by cuongthai on 25/08/2014.
 */
public class PostAuthenticationEvent {
    private final Status mStatus;

    public PostAuthenticationEvent(Status status) {
        mStatus = status;
    }

    public static PostAuthenticationEvent succeededEvent(){
        PostAuthenticationEvent postAuthenticationEvent = new PostAuthenticationEvent(Status.SUCCEED);
        return postAuthenticationEvent;
    }

    public static Object failedEvent() {
        PostAuthenticationEvent postAuthenticationEvent = new PostAuthenticationEvent(Status.FAILED);
        return postAuthenticationEvent;
    }

    public static Object cancelledEvent() {
        PostAuthenticationEvent postAuthenticationEvent = new PostAuthenticationEvent(Status.CANCELLED);
        return postAuthenticationEvent;
    }

    public enum Status {
        SUCCEED,
        FAILED,
        CANCELLED
    }

    public Status getStatus() {
        return mStatus;
    }
}
