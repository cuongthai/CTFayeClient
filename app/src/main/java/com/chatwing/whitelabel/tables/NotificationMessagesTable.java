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

package com.chatwing.whitelabel.tables;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.chatwing.whitelabel.Constants;
import com.chatwing.whitelabel.helpers.ChatWingSQLiteOpenHelper;
import com.chatwing.whitelabel.pojos.Message;
import com.google.gson.Gson;

/**
 * Created by steve on 19/01/2015.
 */
public class NotificationMessagesTable implements BaseColumns {
    public static final String TABLE = "notification_messages";

    public static final String CHAT_BOX_ID = "chatbox_id";
    public static final String CONVERSATION_ID = "conversation_id";
    public static final String CREATED_DATE = "created_date";
    public static final String DATA = "data";

    private static final String DATABASE_CREATE = "CREATE TABLE "
            + TABLE
            + "("
            + _ID + " TEXT PRIMARY KEY,"
            + CHAT_BOX_ID + " INTEGER, "
            + CONVERSATION_ID + " TEXT, "
            + CREATED_DATE + " INTEGER NOT NULL, "
            + DATA + " TEXT NOT NULL "
            + ");";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion,
                                 int newVersion) {
        if (Constants.DEBUG) {
            database.execSQL("DROP TABLE IF EXISTS " + TABLE);
            onCreate(database);
        } else {
            int version = oldVersion;
            switch (version){
                case ChatWingSQLiteOpenHelper.VERSION_0_4:
                case ChatWingSQLiteOpenHelper.VERSION_0_5:
                case ChatWingSQLiteOpenHelper.VERSION_1_2:
                case ChatWingSQLiteOpenHelper.VERSION_1_2_1:
                    version = ChatWingSQLiteOpenHelper.VERSION_1_5;

            }

            if (version != ChatWingSQLiteOpenHelper.DATABASE_VERSION) {
                database.execSQL("DROP TABLE IF EXISTS " + TABLE);
                onCreate(database);
            }
        }
    }

    public static String[] getMinimumProjection() {
        return new String[]{_ID, DATA, CHAT_BOX_ID, CONVERSATION_ID, CREATED_DATE};
    }

    public static ContentValues getContentValues(Message message) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(_ID, message.getId());
        contentValues.put(DATA, new Gson().toJson(message));
        contentValues.put(CHAT_BOX_ID, message.getChatBoxId());
        contentValues.put(CONVERSATION_ID, message.getConversationID());
        contentValues.put(CREATED_DATE, message.getCreatedDate());
        return contentValues;
    }

    public static Message getMessage(Cursor cursor) {
        int dataIndex = cursor.getColumnIndex(DATA);
        String data = cursor.getString(dataIndex);
        Message message = new Gson().fromJson(data, Message.class);
        return message;
    }
}
