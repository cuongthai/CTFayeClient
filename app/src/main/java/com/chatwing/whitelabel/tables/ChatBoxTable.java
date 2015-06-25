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
import android.text.TextUtils;

import com.chatwing.whitelabel.Constants;
import com.chatwing.whitelabel.helpers.ChatWingSQLiteOpenHelper;
import com.chatwing.whitelabel.pojos.ChatBox;
import com.google.gson.Gson;

/**
 * Author: Huy Nguyen
 * Date: 5/30/13
 * Time: 11:43 AM
 * <p/>
 * This class represents "chat_box" ({@link #TABLE_CHAT_BOX}) table in DB.
 * <p/>
 * {@link BaseColumns#_ID} is ID of the chat box and is the  primary key.
 * {@link #CATEGORY_TITLE} is a foreign key which refers to {@link CategoryTable#TITLE}.
 * {@link #DATA} is the JSON representation of {@link com.chatwing.whitelabel.pojos.ChatBox}.
 * {@link #NAME} and {@link #KEY} are name and key of a chat box. This can be
 * retrieved from {@link #DATA}. However, since they are frequently used,
 * they should be in separate columns.
 */
public class ChatBoxTable implements BaseColumns {
    public static final String TABLE_CHAT_BOX = "chat_box";
    public static final String CATEGORY_TITLE = "category_title";
    public static final String DATA = "data";
    public static final String UNREAD_COUNT = "unread_count";
    public static final String NAME = "name";
    public static final String KEY = "key";
    public static final String FAYE_CHANNEL = "faye_channel";
    public static final String ALIAS = "alias";
    public static final String LAST_READ = "last_read";

    private static final String DATABASE_CREATE = "CREATE TABLE "
            + TABLE_CHAT_BOX
            + "("
            + _ID + " INTEGER PRIMARY KEY, "
            + DATA + " TEXT NOT NULL, "
            + UNREAD_COUNT + " INTEGER NOT NULL DEFAULT 0, "
            + NAME + " TEXT NOT NULL, "
            + KEY + " TEXT NOT NULL, "
            + FAYE_CHANNEL + " TEXT NOT NULL, "
            + CATEGORY_TITLE + " TEXT, "
            + ALIAS + " TEXT NULL,"
            + LAST_READ + " INTEGER,"
            + "FOREIGN KEY (" + CATEGORY_TITLE + ") REFERENCES "
            + CategoryTable.TABLE_CATEGORY + " (" + CategoryTable.TITLE + ")"
            + " ON DELETE CASCADE"
            + ");";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion,
                                 int newVersion) {
        if (Constants.DEBUG) {
            database.execSQL("DROP TABLE IF EXISTS " + TABLE_CHAT_BOX);
            onCreate(database);
        } else {
            int version = oldVersion;
            switch (version){
                case ChatWingSQLiteOpenHelper.VERSION_0_4:
                    database.execSQL("ALTER TABLE " + TABLE_CHAT_BOX
                            + " ADD COLUMN" + LAST_READ + " INTEGER DEFAULT 0");
                    version = ChatWingSQLiteOpenHelper.VERSION_0_5;

            }

            if (version != ChatWingSQLiteOpenHelper.DATABASE_VERSION) {
                database.execSQL("DROP TABLE IF EXISTS " + TABLE_CHAT_BOX);
                onCreate(database);
            }
        }
    }

    /**
     * Gets the minimum set of columns to be retrieved from DB. Those columns
     * should be enough for constructing {@link ChatBox}s later
     * using {@link #getChatBox(Cursor)}.
     * TODO: Together with ConversationTable, this and getChatBox may give out of dated data because DATA override UNREAD_COUNT e.g
     */
    public static String[] getMinimumProjection() {
        return new String[]{_ID, DATA, UNREAD_COUNT, NAME, FAYE_CHANNEL, KEY, ALIAS, LAST_READ};
    }


    /**
     * Constructs a {@link ContentValues} based on the given chatBox.
     *
     * @param chatBox       to be used for construction.
     * @param categoryTitle that the chat box belongs to.
     *                      It can't be empty while inserting a record to the DB.
     *                      It can be empty and won't be included in the result while
     *                      updating a record in the DB.
     */
    public static ContentValues getContentValues(ChatBox chatBox,
                                                 String categoryTitle) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(_ID, chatBox.getId());
        contentValues.put(DATA, new Gson().toJson(chatBox));
        if (!TextUtils.isEmpty(categoryTitle)) {
            contentValues.put(CATEGORY_TITLE, categoryTitle);
        }
        contentValues.put(UNREAD_COUNT, chatBox.getUnreadCount());
        contentValues.put(NAME, chatBox.getName());
        contentValues.put(KEY, chatBox.getKey());
        contentValues.put(FAYE_CHANNEL, chatBox.getFayeChannel());
        contentValues.put(ALIAS, chatBox.getAlias());
        return contentValues;
    }

    public static ChatBox getChatBox(Cursor cursor) {
        int dataIndex = cursor.getColumnIndex(DATA);
        int unreadCountIndex = cursor.getColumnIndex(UNREAD_COUNT);
        int nameIndex = cursor.getColumnIndex(NAME);
        int keyIndex = cursor.getColumnIndex(KEY);
        int fayeIndex = cursor.getColumnIndex(FAYE_CHANNEL);
        int aliasIndex = cursor.getColumnIndex(ALIAS);
        int lastReadIndex = cursor.getColumnIndex(LAST_READ);
        if (dataIndex == -1) {
            throw new IllegalArgumentException("data column is expected to " +
                    "transform to ChatBox object.");
        }
        if (unreadCountIndex == -1) {
            throw new IllegalArgumentException("unread_count column is expected.");
        }
        if (nameIndex == -1) {
            throw new IllegalArgumentException("name column is expected.");
        }
        if (keyIndex == -1) {
            throw new IllegalArgumentException("key column is expected.");
        }
        if (fayeIndex == -1) {
            throw new IllegalArgumentException("faye_channel column is expected.");
        }
        if (aliasIndex == -1) {
            throw new IllegalArgumentException("alias column is expected.");
        }
        if(lastReadIndex == -1){
            throw new IllegalArgumentException("last_read column is expected.");
        }

        String data = cursor.getString(dataIndex);
        int unreadCount = cursor.getInt(unreadCountIndex);
        ChatBox chatBox = new Gson().fromJson(data, ChatBox.class);
        chatBox.setUnreadCount(unreadCount);
        chatBox.setName(cursor.getString(nameIndex));
        chatBox.setKey(cursor.getString(keyIndex));
        chatBox.setFayeChannel(cursor.getString(fayeIndex));
        chatBox.setAlias(cursor.getString(aliasIndex));
        return chatBox;
    }
}
