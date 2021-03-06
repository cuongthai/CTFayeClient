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

import com.chatwing.whitelabel.pojos.params.CreateConversationParams;

/**
 * Created by steve on 4/4/14.
 */
public class UserSelectedDefaultUsersEvent {
    private CreateConversationParams.SimpleUser simpleUser;

    public UserSelectedDefaultUsersEvent(CreateConversationParams.SimpleUser simpleUser) {
        this.simpleUser = simpleUser;
    }

    public CreateConversationParams.SimpleUser getSimpleUser() {
        return simpleUser;
    }
}
