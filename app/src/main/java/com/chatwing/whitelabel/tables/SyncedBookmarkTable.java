package com.chatwing.whitelabel.tables;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.chatwing.whitelabel.Constants;
import com.chatwing.whitelabel.helpers.ChatWingSQLiteOpenHelper;
import com.chatwing.whitelabel.pojos.ChatBox;
import com.chatwing.whitelabel.pojos.SyncedBookmark;
import com.google.gson.Gson;

/**
 * Created by steve on 21/06/2014.
 */
public class SyncedBookmarkTable implements BaseColumns {
    public static final String TABLE = "synced_bookmark";
    public static final String BOOKMARK_ID = "bookmark_id";
    public static final String CHAT_BOX_ID = "chat_box_id";
    public static final String CREATED_DATE = "created_date";
    public static final String SYNCED = "is_synced";

    private static final String DATABASE_CREATE = "CREATE TABLE "
            + TABLE
            + "("
            + CHAT_BOX_ID + " INTEGER PRIMARY KEY, "
            + BOOKMARK_ID + " INTEGER, "
            + SYNCED + " INTEGER, "
            + CREATED_DATE + " TIMESTAMP DEFAULT (strftime('%s', 'now')), "
            + "FOREIGN KEY (" + CHAT_BOX_ID + ") REFERENCES "
            + ChatBoxTable.TABLE_CHAT_BOX + "(" + _ID + ") "
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
            // Upgrade this table step by step. That's why all "fall through"s
            // in this switch are intended.
            if(version < ChatWingSQLiteOpenHelper.VERSION_1_2){
                onCreate(database);
                version = ChatWingSQLiteOpenHelper.VERSION_1_2;
            }else{
                version = ChatWingSQLiteOpenHelper.VERSION_1_5;
            }
            if (version != ChatWingSQLiteOpenHelper.DATABASE_VERSION) {
                database.execSQL("DROP TABLE IF EXISTS " + TABLE + ";");
                onCreate(database);
            }
        }
    }

    public static ContentValues getContentValues(SyncedBookmark bookmark) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(CHAT_BOX_ID, bookmark.getChatBox().getId());
        contentValues.put(BOOKMARK_ID, bookmark.getBookmarkId());
        contentValues.put(CREATED_DATE, bookmark.getCreatedDate());
        contentValues.put(SYNCED, bookmark.isSynced() ? 1 : 0);
        return contentValues;
    }

    public static SyncedBookmark getSyncedBookmark(Cursor cursor) {
        int chatboxIdIndex = cursor.getColumnIndex(CHAT_BOX_ID);
        int syncedIndex = cursor.getColumnIndex(SYNCED);
        int bookmarkIdIndex = cursor.getColumnIndex(BOOKMARK_ID);
        int createdDateIndex = cursor.getColumnIndex(CREATED_DATE);

        int chatboxKey = cursor.getColumnIndex(ChatBoxTable.KEY);
        int chatboxName = cursor.getColumnIndex(ChatBoxTable.NAME);
        int chatboxFayeChannel = cursor.getColumnIndex(ChatBoxTable.FAYE_CHANNEL);
        int dataIndex = cursor.getColumnIndex(ChatBoxTable.DATA);
        if (chatboxIdIndex == -1
                || syncedIndex == -1
                || bookmarkIdIndex == -1
                || createdDateIndex == -1
                || chatboxFayeChannel == -1
                || chatboxKey == -1
                || chatboxName == -1
                || dataIndex == -1){
            throw new IllegalArgumentException("unexpected data column");
        }
        Gson gson = new Gson();
        ChatBox chatBox = gson.fromJson(cursor.getString(dataIndex), ChatBox.class);
        chatBox.setId(cursor.getInt(chatboxIdIndex));
        chatBox.setKey(cursor.getString(chatboxKey));
        chatBox.setName(cursor.getString(chatboxName));
        chatBox.setFayeChannel(cursor.getString(chatboxFayeChannel));

        SyncedBookmark bookmark = new SyncedBookmark();
        bookmark.setBookmarkId(cursor.getInt(bookmarkIdIndex));
        bookmark.setCreatedDate(cursor.getLong(createdDateIndex));
        bookmark.setChatBox(chatBox);
        bookmark.setIsSynced(cursor.getInt(syncedIndex) == 1
                ? true
                : false);

        return bookmark;
    }
}
