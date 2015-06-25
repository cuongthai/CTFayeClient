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

package com.chatwing.whitelabel.events.faye;

/**
 * Author: Huy Nguyen
 * Date: 5/27/13
 * Time: 12:10 AM
 */
public class FayePublishEvent {

    public static FayePublishEvent succeedEvent() {
        FayePublishEvent event = new FayePublishEvent();
        event.status = Status.SUCCEED;
        return event;
    }

    public static FayePublishEvent failedEvent(String error) {
        FayePublishEvent event = new FayePublishEvent();
        event.status = Status.FAILED;
        event.error = error;
        return event;
    }

    public enum Status {
        SUCCEED,
        FAILED
    }

    private Status status;
    private String error;

    private FayePublishEvent() {
    }

    public Status getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }
}
