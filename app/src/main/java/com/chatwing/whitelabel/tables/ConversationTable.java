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
import com.chatwing.whitelabel.pojos.Conversation;
import com.google.gson.Gson;

/**
 * Created by steve on 4/2/14.
 */
public class ConversationTable implements BaseColumns {
    public static final String TABLE = "conversation";

    public static final String CONVERSATION_ID = "conversation_id";
    public static final String CREATED_DATE = "created_date";
    public static final String LAST_DATE = "last_message_date";
    public static final String DATA = "data";
    public static final String UNREAD_COUNT = "unread_count";
    public static final String DATE_UPDATED = "date_updated";

    private static final String DATABASE_CREATE = "CREATE TABLE "
            + TABLE
            + "("
            + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + CONVERSATION_ID + " TEXT UNIQUE, "
            + UNREAD_COUNT + " INTEGER NOT NULL DEFAULT 0, "
            + CREATED_DATE + " INTEGER NOT NULL, "
            + LAST_DATE + " INTEGER NOT NULL, "
            + DATE_UPDATED + " INTEGER NOT NULL, "
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
        }else {
            int version = oldVersion;
            // Upgrade this table step by step. That's why all "fall through"s
            // in this switch are intended.
            switch (version){
                case ChatWingSQLiteOpenHelper.VERSION_0_4:
                    version = ChatWingSQLiteOpenHelper.VERSION_0_5;
            }

            if (version != ChatWingSQLiteOpenHelper.DATABASE_VERSION) {
                database.execSQL("DROP TABLE IF EXISTS " + TABLE + ";");
                onCreate(database);
            }
        }
    }

    public static String[] getMinimumProjection() {
        return new String[]{_ID, CONVERSATION_ID, CREATED_DATE, DATA, UNREAD_COUNT, DATE_UPDATED, LAST_DATE};
    }

    public static Conversation getConversation(Cursor cursor) {
        int dataIndex = cursor.getColumnIndex(DATA);
        int unreadCountIndex = cursor.getColumnIndex(UNREAD_COUNT);
        int conversationIdIndex = cursor.getColumnIndex(CONVERSATION_ID);
        int createdDateIndex = cursor.getColumnIndex(CREATED_DATE);
        int lastDateIndex = cursor.getColumnIndex(LAST_DATE);
        int dateUpdatedIndex = cursor.getColumnIndex(DATE_UPDATED);
        if (dataIndex == -1) {
            throw new IllegalArgumentException("data column is " +
                    "expected to transform to conversation object.");
        }

        String data = cursor.getString(dataIndex);
        Conversation conversation = new Gson().fromJson(data, Conversation.class);
        conversation.setUnreadCount(cursor.getLong(unreadCountIndex));
        conversation.setId(cursor.getString(conversationIdIndex));
        conversation.setCreatedDate(cursor.getLong(createdDateIndex));
        conversation.setLastDate(cursor.getLong(lastDateIndex));
        conversation.setDateUpdated(cursor.getLong(dateUpdatedIndex));
        return conversation;
    }

    public static ContentValues getContentValues(Conversation conversation) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(CONVERSATION_ID, conversation.getId());
        contentValues.put(CREATED_DATE, conversation.getCreatedDate());
        contentValues.put(LAST_DATE, conversation.getLastMessageDate());
        contentValues.put(UNREAD_COUNT, conversation.getUnreadCount());
        contentValues.put(DATE_UPDATED, conversation.getDateUpdated());
        contentValues.put(DATA, new Gson().toJson(conversation));
        return contentValues;
    }
}
