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
 * Time: 12:03 AM
 */
public class ChannelSubscriptionChangedEvent {

    public static ChannelSubscriptionChangedEvent succeedEvent(String channel) {
        ChannelSubscriptionChangedEvent event = new ChannelSubscriptionChangedEvent();
        event.status = Status.SUCCEED;
        event.channel = channel;
        return event;
    }

    public static ChannelSubscriptionChangedEvent failedEvent(String channel,
                                                              String error) {
        ChannelSubscriptionChangedEvent event = new ChannelSubscriptionChangedEvent();
        event.status = Status.FAILED;
        event.channel = channel;
        event.error = error;
        return event;
    }

    public enum Status {
        SUCCEED,
        FAILED
    }

    private ChannelSubscriptionChangedEvent() {
    }

    private Status status;
    private String channel;
    private String error;

    public Status getStatus() {
        return status;
    }

    public String getChannel() {
        return channel;
    }

    public String getError() {
        return error;
    }
}
