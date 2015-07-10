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
 * Author: Huy Nguyen
 * Date: 5/30/13
 * Time: 12:02 PM
 * <p/>
 * This class represents "message" ({@link #TABLE}) table in DB.
 * <p/>
 * {@link #_ID} is ID of the message and is primary key.
 * {@link #CHAT_BOX_ID} is a foreign key which refer to {@link ChatBoxTable#_ID}.
 * {@link #DATA} is the JSON representation of {@link com.chatwing.whitelabel.pojos.Message}.
 */
public class MessageTable implements BaseColumns {
    public static final String TABLE = "enh_message";
    public static final String CHAT_BOX_ID = "chat_box_id";
    public static final String CONVERSATION_ID = "conversation_id";
    public static final String MESSAGE_IS_PRIVATE = "is_private";
    public static final String DATA = "data";
    public static final String CREATED_DATE = "sending_date";
    public static final String LOGIN_TYPE = "login_type";
    public static final String IP = "ip";

    private static final String DATABASE_CREATE = "CREATE TABLE "
            + TABLE
            + "("
            + _ID + " TEXT PRIMARY KEY, "
            + DATA + " TEXT NOT NULL, "
            + CHAT_BOX_ID + " INTEGER, "
            + CONVERSATION_ID + " INTEGER, "
            + MESSAGE_IS_PRIVATE + " INTEGER, "
            + CREATED_DATE + " INTEGER,"
            + IP + " TEXT,"
            + LOGIN_TYPE + " TEXT,"
            + "FOREIGN KEY (" + CHAT_BOX_ID + ") REFERENCES " +
            ChatBoxTable.TABLE_CHAT_BOX + "(" + ChatBoxTable._ID + ")"
            + " ON DELETE CASCADE,"
            + "FOREIGN KEY (" + CONVERSATION_ID + ") REFERENCES " +
            ConversationTable.TABLE + "(" + ConversationTable._ID + ")"
            + " ON DELETE CASCADE"
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
            switch (version) {
                case ChatWingSQLiteOpenHelper.VERSION_0_4:
                case ChatWingSQLiteOpenHelper.VERSION_0_5:
                case ChatWingSQLiteOpenHelper.VERSION_1_2:
                case ChatWingSQLiteOpenHelper.VERSION_1_2_1:
                    onCreate(database);
                    version = ChatWingSQLiteOpenHelper.VERSION_1_5;
            }
            if (version != ChatWingSQLiteOpenHelper.DATABASE_VERSION) {
                database.execSQL("DROP TABLE IF EXISTS " + TABLE + ";");
                onCreate(database);
            }
        }
    }

    /**
     * Gets the minimum set of columns to be retrieved from DB. Those columns
     * should be enough for constructing {@link com.chatwing.whitelabel.pojos.Message}s later
     * using {@link #getMessage(android.database.Cursor)}.
     */
    public static String[] getMinimumProjection() {
        return new String[]{DATA, CREATED_DATE, LOGIN_TYPE, IP};
    }

    public static ContentValues getContentValues(Message message) {
        ContentValues result = new ContentValues();
        result.put(_ID, message.getId());
        if (message.isPrivate()) {
            result.putNull(CHAT_BOX_ID);
            result.put(CONVERSATION_ID, message.getConversationID());
            result.put(MESSAGE_IS_PRIVATE, 1);
        } else {
            result.put(CHAT_BOX_ID, message.getChatBoxId());
            result.putNull(CONVERSATION_ID);
            result.put(MESSAGE_IS_PRIVATE, 0);
            result.put(IP, message.getIp());
            result.put(LOGIN_TYPE, message.getUserType());
        }

        result.put(DATA, new Gson().toJson(message));
        result.put(CREATED_DATE, message.getCreatedDate());
        return result;
    }

    public static Message getMessage(Cursor cursor) {
        int dataIndex = cursor.getColumnIndex(DATA);
        int createdDateIndex = cursor.getColumnIndex(CREATED_DATE);
        int ipIndex = cursor.getColumnIndex(IP);
        int loginTypeIndex = cursor.getColumnIndex(LOGIN_TYPE);
        if (dataIndex == -1) {
            throw new IllegalArgumentException("data column is expected to " +
                    "transform to message object.");
        }

        if (createdDateIndex == -1) {
            throw new IllegalArgumentException("created_date column is " +
                    "expected to transform to message object.");
        }
        if (ipIndex == -1){
            throw new IllegalArgumentException("ip column is " +
                    "expected to transform to message object.");
        }
        if (loginTypeIndex == -1){
            throw new IllegalArgumentException("login_type column is " +
                    "expected to transform to message object.");
        }

        String data = cursor.getString(dataIndex);
        long createdDate = cursor.getLong(createdDateIndex);
        Message message = new Gson().fromJson(data, Message.class);
        message.setCreatedDate(createdDate);
        message.setIp(cursor.getString(ipIndex));
        message.setLoginType(cursor.getString(loginTypeIndex));
        return message;
    }
}
