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

package com.chatwingsdk.events.internal;

/**
 * Author: Huy Nguyen
 * Date: 5/31/13
 * Time: 2:01 PM
 */
public class UpdateSubscriptionEvent {
    public enum Status {
        STARTED,
        SUCCEED,
        FAILED
    }

    public static UpdateSubscriptionEvent startedEvent() {
        return new UpdateSubscriptionEvent(Status.STARTED);
    }

    public static UpdateSubscriptionEvent succeedEvent() {
        return new UpdateSubscriptionEvent(Status.SUCCEED);
    }

    public static UpdateSubscriptionEvent failedEvent(Exception exception) {
        UpdateSubscriptionEvent event = new UpdateSubscriptionEvent(Status.FAILED);
        event.exception = exception;
        return event;
    }

    private Status status;
    private Exception exception;

    private UpdateSubscriptionEvent(Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }

    public Exception getException() {
        return exception;
    }
}
