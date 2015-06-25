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


import com.chatwing.whitelabel.pojos.responses.SubscriptionResponse;

/**
 * Author: Huy Nguyen
 * Date: 5/31/13
 * Time: 2:01 PM
 */
public class UpdateSubscriptionEvent {
    private String action;
    private SubscriptionResponse subscriptionResponse;

    public enum Status {
        STARTED,
        SUCCEED,
        FAILED
    }

    public static UpdateSubscriptionEvent startedEvent() {
        return new UpdateSubscriptionEvent(Status.STARTED);
    }

    public static UpdateSubscriptionEvent succeedEvent(SubscriptionResponse subscriptionResponse, String action) {
        return new UpdateSubscriptionEvent(Status.SUCCEED, subscriptionResponse, action);
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

    private UpdateSubscriptionEvent(Status status, SubscriptionResponse subscriptionResponse, String action) {
        this(status);
        this.subscriptionResponse = subscriptionResponse;
        this.action = action;
    }

    public Status getStatus() {
        return status;
    }

    public Exception getException() {
        return exception;
    }

    public SubscriptionResponse getSubscriptionResponse() {
        return subscriptionResponse;
    }

    public String getAction() {
        return action;
    }
}
