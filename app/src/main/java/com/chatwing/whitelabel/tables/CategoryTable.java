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
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.chatwing.whitelabel.Constants;
import com.chatwing.whitelabel.helpers.ChatWingSQLiteOpenHelper;
import com.chatwing.whitelabel.pojos.Category;


/**
 * Author: Huy Nguyen
 * Date: 5/30/13
 * Time: 11:35 AM
 * <p/>
 * This class represents "category" ({@link #TABLE_CATEGORY}) table in DB. It
 * has 2 columns: "_id" {@link BaseColumns#_ID} and "title" ({@link #TITLE}).
 */
public class CategoryTable implements BaseColumns {
    public static final String TABLE_CATEGORY = "category";
    public static final String TITLE = "title";

    private static final String DATABASE_CREATE = "CREATE TABLE "
            + TABLE_CATEGORY
            + "("
            + _ID + " INTEGER PRIMARY KEY, "
            + TITLE + " TEXT NOT NULL"
            + ");";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion,
                                 int newVersion) {
        if (Constants.DEBUG) {
            database.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORY);
            onCreate(database);
        } else {
            int version = oldVersion;
            // Upgrade this table step by step. That's why all "fall through"s
            // in this switch are intended.
            switch (version){
                case ChatWingSQLiteOpenHelper.VERSION_0_4:
                    version = ChatWingSQLiteOpenHelper.VERSION_0_5;

            }

            if (version != ChatWingSQLiteOpenHelper.DATABASE_VERSION) {
                database.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORY + ";");
                onCreate(database);
            }
        }
    }

    public static ContentValues getContentValues(Category category) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(TITLE, category.getTitle());
        return contentValues;
    }
}
