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


import com.chatwing.whitelabel.pojos.responses.SubscriptionStatusResponse;

/**
 * Author: Huy Nguyen
 * Date: 5/31/13
 * Time: 2:01 PM
 */
public class SubscriptionStatusEvent {
    private SubscriptionStatusResponse subscriptionResponse;

    public enum Status {
        STARTED,
        SUCCEED,
        FAILED
    }

    public static SubscriptionStatusEvent startedEvent() {
        return new SubscriptionStatusEvent(Status.STARTED);
    }

    public static SubscriptionStatusEvent succeedEvent(SubscriptionStatusResponse subscriptionResponse) {
        return new SubscriptionStatusEvent(Status.SUCCEED, subscriptionResponse);
    }

    public static SubscriptionStatusEvent failedEvent(Exception exception) {
        SubscriptionStatusEvent event = new SubscriptionStatusEvent(Status.FAILED);
        event.exception = exception;
        return event;
    }

    private Status status;
    private Exception exception;

    private SubscriptionStatusEvent(Status status) {
        this.status = status;
    }

    private SubscriptionStatusEvent(Status status, SubscriptionStatusResponse subscriptionResponse) {
        this(status);
        this.subscriptionResponse = subscriptionResponse;
    }

    public Status getStatus() {
        return status;
    }

    public Exception getException() {
        return exception;
    }

    public SubscriptionStatusResponse getSubscriptionResponse() {
        return subscriptionResponse;
    }

}
