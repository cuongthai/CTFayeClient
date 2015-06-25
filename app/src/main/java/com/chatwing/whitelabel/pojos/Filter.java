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

package com.chatwing.whitelabel.pojos;

import com.google.gson.annotations.SerializedName;

/**
 * Author: Huy Nguyen
 * Date: 7/24/13
 * Time: 2:42 PM
 */
public class Filter {
    private String name;
    @SerializedName("filter_name")
    private boolean filterName;
    @SerializedName("filter_message")
    private boolean filterMessage;

    public String getName() {
        return name;
    }

    public boolean isFilterName() {
        return filterName;
    }

    public boolean isFilterMessage() {
        return filterMessage;
    }
}
