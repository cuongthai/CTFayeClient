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

package com.chatwing.whitelabel.pojos.params.oauth;


import com.chatwing.whitelabel.pojos.params.Params;

/**
 * Author: Huy Nguyen
 * Date: 6/14/13
 * Time: 8:51 PM
 */
public abstract class AuthenticationParams extends Params {
    private String type;
    protected Object[] params;

    public AuthenticationParams(String type, Object[] params) {
        this.type = type;
        this.params = params;
    }

    public String getType() {
        return type;
    }
}
