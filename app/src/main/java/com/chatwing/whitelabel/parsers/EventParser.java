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

package com.chatwing.whitelabel.parsers;


import com.chatwing.whitelabel.pojos.Event;

import org.json.JSONException;

/**
 * Created by nguyenthanhhuy on 1/6/14.
 */
public interface EventParser {
    String EVENT_NEW_MESSAGE = "new-message";
    String EVENT_NETWORK_NEW_MESSAGE = "network:new-message";
    String EVENT_DELETE_MESSAGE = "delete-message";
    String EVENT_DELETE_MESSAGE_BY_SOCIAL = "delete-messages-by-social-account";
    String EVENT_DELETE_MESSAGE_BY_IP = "delete-messages-by-ip";

    Event parse(String json) throws JSONException;
}
