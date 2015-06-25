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
import com.chatwing.whitelabel.pojos.Message;
import com.chatwing.whitelabel.pojos.params.Params;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;

/**
 * Author: Huy Nguyen
 * Date: 4/6/13
 * Time: 3:12 PM
 */
public class EventParserImpl implements EventParser {
    @Inject
    EventParserImpl() {
    }

    public Event parse(String json) throws JSONException {

        JSONObject root = new JSONObject(json);
        String event = root.getString("event");
        Class<? extends Params> c = getParamsClass(event);
        if (c == null) {
            throw new JSONException("Can't identify the event: " + event);
        }

        try {
            Params params = new Gson()
                    .fromJson(root.getJSONObject("params").toString(), c);
            return new Event(event, params);
        } catch (Exception ex) {
            throw new JSONException(ex.getLocalizedMessage());
        }
    }

    /**
     * Returns class of Params to match event name.
     *
     * @return class of Params to match event name. Or null if can't identify
     * the event.
     */
    private Class<? extends Params> getParamsClass(String event) {
        if (event.equals(EVENT_NEW_MESSAGE)
                || event.equals(EVENT_NETWORK_NEW_MESSAGE)
                || event.equals(EVENT_DELETE_MESSAGE)
                || event.equals(EVENT_DELETE_MESSAGE_BY_SOCIAL)
                || event.equals(EVENT_DELETE_MESSAGE_BY_IP)) {
            return Message.class;
        }
        return null;
    }
}
