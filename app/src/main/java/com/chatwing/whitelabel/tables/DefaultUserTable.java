package com.chatwing.whitelabel.tables;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import com.chatwing.whitelabel.Constants;
import com.chatwing.whitelabel.helpers.ChatWingSQLiteOpenHelper;
import com.chatwing.whitelabel.pojos.responses.LoadModeratorsResponse;

/**
 * Created by steve on 22/07/2015.
 */
public class DefaultUserTable implements BaseColumns {
    public static final String TABLE = "moderators";

    public static final String LOGIN_TYPE = "login_type";
    public static final String LOGIN_ID = "login_id";
    public static final String NAME = "name";
    public static final String TARGET_USER_IDENTIFIER = "user_identifier";

    private static final String DATABASE_CREATE = "CREATE TABLE "
            + TABLE
            + "("
            + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + LOGIN_TYPE + " TEXT, "
            + LOGIN_ID + " TEXT, "
            + TARGET_USER_IDENTIFIER + " TEXT, "
            + NAME + " TEXT"
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
                case ChatWingSQLiteOpenHelper.VERSION_1_5:
                    version = ChatWingSQLiteOpenHelper.VERSION_1_5;
            }

            if (version != ChatWingSQLiteOpenHelper.DATABASE_VERSION) {
                database.execSQL("DROP TABLE IF EXISTS " + TABLE + ";");
                onCreate(database);
            }
        }
    }

    public static String[] getMinimumProjection() {
        return new String[]{TABLE + "." + _ID, LOGIN_ID, LOGIN_TYPE, NAME, TABLE + "." +TARGET_USER_IDENTIFIER};
    }

    public static LoadModeratorsResponse.Moderator getModerator(Cursor cursor) {
        int loginId = cursor.getColumnIndex(LOGIN_ID);
        int loginType = cursor.getColumnIndex(LOGIN_TYPE);
        int name = cursor.getColumnIndex(NAME);


        LoadModeratorsResponse.Moderator moderator = new LoadModeratorsResponse.Moderator();
        moderator.setLoginID(cursor.getString(loginId));
        moderator.setLoginType(cursor.getString(loginType));
        moderator.setName(cursor.getString(name));
        return moderator;
    }

    public static ContentValues getContentValues(LoadModeratorsResponse.Moderator moderator) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(LOGIN_ID, moderator.getLoginId());
        contentValues.put(LOGIN_TYPE, moderator.getLoginType());
        contentValues.put(NAME, moderator.getName());
        contentValues.put(TARGET_USER_IDENTIFIER, moderator.getIdentifier());
        return contentValues;
    }


}
