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

import java.util.ArrayList;

/**
 * Author: Huy Nguyen
 * Date: 4/29/13
 * Time: 11:32 AM
 */
public class Category {
    @SerializedName("category")
    private String title;
    @SerializedName("chatboxes")
    private ArrayList<ChatBox> chatBoxes;

    public String getTitle() {
        return title;
    }

    public ArrayList<ChatBox> getChatBoxes() {
        return chatBoxes;
    }

    public Category(String title, ArrayList<ChatBox> chatBoxes) {
        this.title = title;
        this.chatBoxes = chatBoxes;
    }
}
