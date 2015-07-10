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

package com.chatwing.whitelabel.helpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.chatwing.whitelabel.tables.CategoryTable;
import com.chatwing.whitelabel.tables.ChatBoxTable;
import com.chatwing.whitelabel.tables.ConversationTable;
import com.chatwing.whitelabel.tables.MessageTable;
import com.chatwing.whitelabel.tables.NotificationMessagesTable;
import com.chatwing.whitelabel.tables.SyncedBookmarkTable;


/**
 * Author: Huy Nguyen
 * Date: 5/30/13
 * Time: 12:20 PM
 */
public class ChatWingSQLiteOpenHelper extends SQLiteOpenHelper {
    protected static final String DATABASE_NAME = "chatwingsdk.db";

    /**
     * Add NotificationMessageTable
     */
    public static final int VERSION_0_4 = 2;

    /**
     * Add last_read in chatbox table
     * Add NotificationSettingsTable
     */
    public static final int VERSION_0_5 = 3;

    public static final int VERSION_1_2 = 10;

    public static final int VERSION_1_2_1 = 11;

    public static final int VERSION_1_5 = 20;

    public static final int DATABASE_VERSION = VERSION_1_5;

    public ChatWingSQLiteOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        ConversationTable.onCreate(db);
        ChatBoxTable.onCreate(db);
        CategoryTable.onCreate(db);
        NotificationMessagesTable.onCreate(db);
        SyncedBookmarkTable.onCreate(db);
        MessageTable.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        CategoryTable.onUpgrade(db, oldVersion, newVersion);
        ConversationTable.onUpgrade(db, oldVersion, newVersion);
        ChatBoxTable.onUpgrade(db, oldVersion, newVersion);
        NotificationMessagesTable.onUpgrade(db, oldVersion, newVersion);
        SyncedBookmarkTable.onUpgrade(db, oldVersion, newVersion);
        MessageTable.onUpgrade(db, oldVersion, newVersion);
    }
}
