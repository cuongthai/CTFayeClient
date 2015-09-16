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

package com.chatwing.whitelabel.contentproviders;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.chatwing.whitelabel.BuildConfig;
import com.chatwing.whitelabel.helpers.ChatWingSQLiteOpenHelper;
import com.chatwing.whitelabel.tables.CategoryTable;
import com.chatwing.whitelabel.tables.ChatBoxTable;
import com.chatwing.whitelabel.tables.ConversationTable;
import com.chatwing.whitelabel.tables.DefaultUserTable;
import com.chatwing.whitelabel.tables.MessageTable;
import com.chatwing.whitelabel.tables.NotificationMessagesTable;
import com.chatwing.whitelabel.tables.SyncedBookmarkTable;
import com.chatwing.whitelabel.utils.LogUtils;

import java.util.ArrayList;

/**
 * Author: Huy Nguyen
 * Date: 5/30/13
 * Time: 12:25 PM
 */
public class ChatWingContentProvider extends ContentProvider {

    public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".provider";

    public static final String PATH_CONVERSATIONS = "conversations";
    public static final String PATH_CHAT_BOXES = "chat_boxes";
    public static final String PATH_CATEGORIES = "categories";
    public static final String PATH_MESSAGES = "messages";
    public static final String PATH_CATEGORIZED_CHAT_BOXES = "categorized_chat_boxes";
    public static final String PATH_AGGREGATED_CATEGORIES = "aggregated_categories";
    public static final String PATH_NOTIFICATION_MESSAGES = "notification_messages";
    public static final String PATH_SYNCED_BOOKMARKS = "synced_bookmarks";
    public static final String PATH_MODERATORS = "moderators";


    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    private static final int CODE_CONVERSATIONS = 130;
    private static final int CODE_CONVERSATION = 140;
    private static final int CODE_CONVERSATIONS_ID_MESSAGES = 150;

    private static final int CODE_CATEGORIES = 10;
    private static final int CODE_CATEGORY = 11;


    private static final int CODE_CHAT_BOXES = 20;
    private static final int CODE_CHAT_BOX = 21;
    private static final int CODE_CATEGORIES_TITLE_CHAT_BOXES = 22;
    private static final int CODE_CATEGORIZED_CHAT_BOXES = 23;

    private static final int CODE_MESSAGES = 30;
    private static final int CODE_MESSAGE = 31;
    private static final int CODE_CHAT_BOXES_ID_MESSAGES = 32;

    private static final int CODE_MODERATORS = 50;


    private static final int CODE_AGGREEGATED_CATEGORIES = 90;

    private static final int CODE_NOTIFICATION_MESSAGES = 100;

    private static final int CODE_SYNCED_BOOKMARKS = 160;
    private static final int CODE_SYNCED_BOOKMARK = 161;

    protected static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(AUTHORITY, PATH_CATEGORIES, CODE_CATEGORIES);
        sUriMatcher.addURI(AUTHORITY, PATH_CATEGORIES + "/#", CODE_CATEGORY);

        sUriMatcher.addURI(AUTHORITY, PATH_CONVERSATIONS, CODE_CONVERSATIONS);
        sUriMatcher.addURI(AUTHORITY, PATH_CONVERSATIONS + "/*", CODE_CONVERSATION);
        sUriMatcher.addURI(
                AUTHORITY,
                PATH_CONVERSATIONS + "/*/" + PATH_MESSAGES,
                CODE_CONVERSATIONS_ID_MESSAGES);

        sUriMatcher.addURI(AUTHORITY, PATH_MODERATORS, CODE_MODERATORS);


        sUriMatcher.addURI(AUTHORITY, PATH_CHAT_BOXES, CODE_CHAT_BOXES);
        sUriMatcher.addURI(AUTHORITY, PATH_CHAT_BOXES + "/#", CODE_CHAT_BOX);
        sUriMatcher.addURI(
                AUTHORITY,
                PATH_CATEGORIES + "/*/" + PATH_CHAT_BOXES,
                CODE_CATEGORIES_TITLE_CHAT_BOXES);

        sUriMatcher.addURI(
                AUTHORITY,
                PATH_CATEGORIZED_CHAT_BOXES,
                CODE_CATEGORIZED_CHAT_BOXES
        );

        sUriMatcher.addURI(AUTHORITY, PATH_MESSAGES, CODE_MESSAGES);
        sUriMatcher.addURI(AUTHORITY, PATH_MESSAGES + "/*", CODE_MESSAGE);
        sUriMatcher.addURI(
                AUTHORITY,
                PATH_CHAT_BOXES + "/#/" + PATH_MESSAGES,
                CODE_CHAT_BOXES_ID_MESSAGES);

        sUriMatcher.addURI(
                AUTHORITY,
                PATH_AGGREGATED_CATEGORIES,
                CODE_AGGREEGATED_CATEGORIES
        );
        sUriMatcher.addURI(
                AUTHORITY,
                PATH_NOTIFICATION_MESSAGES,
                CODE_NOTIFICATION_MESSAGES
        );

        sUriMatcher.addURI(AUTHORITY, PATH_SYNCED_BOOKMARKS, CODE_SYNCED_BOOKMARKS);
        sUriMatcher.addURI(AUTHORITY, PATH_SYNCED_BOOKMARKS + "/#", CODE_SYNCED_BOOKMARK);
    }

    private static Uri sModeratorsUri;
    private static Uri sConversationsUri;
    private static Uri sChatBoxesUri;
    private static Uri sMessagesUri;
    private static Uri sCategoriesUri;
    private static Uri sAggregatedCategoriesUri;
    private static Uri sCategorizedChatBoxesUri;
    private static Uri sNotificationMessagesUri;
    private static Uri sSyncedBookmarksUri;

    public static Uri getModeratorsUri() {
        if (sModeratorsUri == null) {
            sModeratorsUri = CONTENT_URI.buildUpon()
                    .appendEncodedPath(PATH_MODERATORS)
                    .build();
        }
        return sModeratorsUri;
    }

    public static Uri getConversationsUri() {
        if (sConversationsUri == null) {
            sConversationsUri = CONTENT_URI.buildUpon()
                    .appendEncodedPath(PATH_CONVERSATIONS)
                    .build();
        }
        return sConversationsUri;
    }

    public static Uri getCategoriesUri() {
        if (sCategoriesUri == null) {
            sCategoriesUri = CONTENT_URI.buildUpon()
                    .appendPath(PATH_CATEGORIES)
                    .build();
        }
        return sCategoriesUri;
    }

    public static Uri getAggregatedCategoriesUri() {
        if (sAggregatedCategoriesUri == null) {
            sAggregatedCategoriesUri = CONTENT_URI.buildUpon()
                    .appendPath(PATH_AGGREGATED_CATEGORIES)
                    .build();
        }
        return sAggregatedCategoriesUri;
    }

    public static Uri getCategorizedChatBoxesUri() {
        if (sCategorizedChatBoxesUri == null) {
            sCategorizedChatBoxesUri = CONTENT_URI.buildUpon()
                    .appendEncodedPath(PATH_CATEGORIZED_CHAT_BOXES)
                    .build();
        }
        return sCategorizedChatBoxesUri;
    }

    public static Uri getMessagesUri() {
        if (sMessagesUri == null) {
            sMessagesUri = CONTENT_URI.buildUpon()
                    .appendEncodedPath(PATH_MESSAGES)
                    .build();
        }
        return sMessagesUri;
    }

    public static Uri getMessagesInChatBoxUri(int chatBoxId) {
        return getChatBoxesUri().buildUpon()
                .appendPath(Integer.toString(chatBoxId))
                .appendPath(PATH_MESSAGES)
                .build();
    }

    public static Uri getSyncedBookmarkWithChatBoxIdUri(int chatBoxId) {
        return getSyncedBookmarksUri().buildUpon()
                .appendPath(Integer.toString(chatBoxId))
                .build();
    }

    public static Uri getChatBoxWithIdUri(int chatBoxId) {
        return getChatBoxesUri().buildUpon()
                .appendPath(Integer.toString(chatBoxId))
                .build();
    }

    public static Uri getChatBoxesUri() {
        if (sChatBoxesUri == null) {
            sChatBoxesUri = CONTENT_URI.buildUpon()
                    .appendEncodedPath(PATH_CHAT_BOXES)
                    .build();
        }
        return sChatBoxesUri;
    }

    public static Uri getConversationWithIdUri(String conversationId) {
        return getConversationsUri().buildUpon()
                .appendPath(conversationId)
                .build();
    }

    public static Uri getMessagesInConversationUri(String mConversationId) {
        return getConversationsUri().buildUpon()
                .appendPath(mConversationId)
                .appendPath(PATH_MESSAGES)
                .build();
    }


    public static Uri getNotificationMessagesUri() {
        if (sNotificationMessagesUri == null) {
            sNotificationMessagesUri = CONTENT_URI.buildUpon()
                    .appendEncodedPath(PATH_NOTIFICATION_MESSAGES)
                    .build();
        }
        return sNotificationMessagesUri;
    }

    public static Uri getSyncedBookmarksUri() {
        if (sSyncedBookmarksUri == null) {
            sSyncedBookmarksUri = CONTENT_URI.buildUpon()
                    .appendEncodedPath(PATH_SYNCED_BOOKMARKS)
                    .build();
        }
        return sSyncedBookmarksUri;
    }

    public static ArrayList<ContentProviderOperation> getClearAllDataBatch() {
        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
        batch.add(ContentProviderOperation.newDelete(getConversationsUri()).build());
        batch.add(ContentProviderOperation.newDelete(getChatBoxesUri()).build());
        batch.add(ContentProviderOperation.newDelete(getMessagesUri()).build());
        batch.add(ContentProviderOperation.newDelete(getSyncedBookmarksUri()).build());
        batch.add(ContentProviderOperation.newDelete(getCategoriesUri()).build());
        batch.add(ContentProviderOperation.newDelete(getModeratorsUri()).build());

        return batch;
    }

    public static boolean hasChatBoxInDB(ContentResolver contentResolver,
                                         int chatBoxId) {
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(
                    getChatBoxWithIdUri(chatBoxId),
                    new String[]{ChatBoxTable._ID},
                    null,
                    null,
                    null);
            return cursor.getCount() > 0;
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    public static boolean hasSyncedBookmarkInDB(ContentResolver contentResolver,
                                                int chatBoxId) {
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(
                    getSyncedBookmarkWithChatBoxIdUri(chatBoxId),
                    new String[]{SyncedBookmarkTable.CHAT_BOX_ID},
                    null,
                    null,
                    null);
            return cursor.getCount() > 0;
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    public static int countUnreadMessagesInConversations(ContentResolver contentResolver) {
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(
                    getConversationsUri(),
                    new String[]{"SUM (" + ConversationTable.UNREAD_COUNT + ")"},
                    null,
                    null,
                    null);
            cursor.moveToFirst();
            return cursor.getInt(0);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    private ChatWingSQLiteOpenHelper mOpenHelper;


    @Override
    public boolean onCreate() {
        mOpenHelper = new ChatWingSQLiteOpenHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();

        int uriType = sUriMatcher.match(uri);
        String groupBy = null;
        StringBuilder sb;
        switch (uriType) {
            case CODE_AGGREEGATED_CATEGORIES:
                sb = new StringBuilder(ChatBoxTable.TABLE_CHAT_BOX)
                        .append(" INNER JOIN ")
                        .append(CategoryTable.TABLE_CATEGORY)
                        .append(" ON (")
                        .append(ChatBoxTable.TABLE_CHAT_BOX)
                        .append(".")
                        .append(ChatBoxTable.CATEGORY_TITLE)
                        .append(" = ")
                        .append(CategoryTable.TABLE_CATEGORY)
                        .append(".")
                        .append(CategoryTable.TITLE)
                        .append(")");
                groupBy = ChatBoxTable.CATEGORY_TITLE;
                builder.setTables(sb.toString());
                break;
            case CODE_CATEGORIES:
                builder.setTables(CategoryTable.TABLE_CATEGORY);
                break;
            case CODE_CATEGORY:
                builder.appendWhere(
                        CategoryTable.TITLE + "=\"" + uri.getLastPathSegment() + "\"");
                builder.setTables(CategoryTable.TABLE_CATEGORY);
                break;
            case CODE_CONVERSATIONS:
                builder.setTables(ConversationTable.TABLE);
                break;
            case CODE_CONVERSATION:
                builder.appendWhere(
                        ConversationTable.CONVERSATION_ID + "=\"" + uri.getLastPathSegment() + "\"");
                builder.setTables(ConversationTable.TABLE);
                break;
            case CODE_CONVERSATIONS_ID_MESSAGES:
                builder.appendWhere(
                        MessageTable.CONVERSATION_ID + "=\"" + uri.getPathSegments().get(1) + "\"");
                builder.setTables(MessageTable.TABLE);
                break;
            case CODE_CHAT_BOXES:
                builder.setTables(ChatBoxTable.TABLE_CHAT_BOX);
                break;
            case CODE_CHAT_BOX:
                builder.appendWhere(
                        ChatBoxTable._ID + "=" + uri.getLastPathSegment());
                builder.setTables(ChatBoxTable.TABLE_CHAT_BOX);
                break;
            case CODE_CATEGORIZED_CHAT_BOXES:
                builder.appendWhere(
                        ChatBoxTable.CATEGORY_TITLE + " IS NOT NULL AND " +
                                "TRIM(" + ChatBoxTable.CATEGORY_TITLE + ") != ''"
                );
                builder.setTables(ChatBoxTable.TABLE_CHAT_BOX);
                break;
            case CODE_MESSAGES:
                builder.setTables(MessageTable.TABLE);
                break;
            case CODE_MESSAGE:
                builder.appendWhere(
                        MessageTable._ID + "=" + uri.getLastPathSegment());
                builder.setTables(MessageTable.TABLE);
                break;
            case CODE_CHAT_BOXES_ID_MESSAGES:
                builder.appendWhere(
                        MessageTable.CHAT_BOX_ID + "=" + uri.getPathSegments().get(1));
                builder.setTables(MessageTable.TABLE);
                break;
            case CODE_CATEGORIES_TITLE_CHAT_BOXES:
                builder.appendWhere(
                        ChatBoxTable.CATEGORY_TITLE + "=\"" + uri.getPathSegments().get(1) + "\"");
                builder.setTables(ChatBoxTable.TABLE_CHAT_BOX);
                break;
            case CODE_NOTIFICATION_MESSAGES:
                builder.setTables(NotificationMessagesTable.TABLE);
                break;
            case CODE_SYNCED_BOOKMARKS:
                sb = new StringBuilder(SyncedBookmarkTable.TABLE)
                        .append(" INNER JOIN ")
                        .append(ChatBoxTable.TABLE_CHAT_BOX)
                        .append(" ON (")
                        .append(SyncedBookmarkTable.TABLE)
                        .append(".")
                        .append(SyncedBookmarkTable.CHAT_BOX_ID)
                        .append(" = ")
                        .append(ChatBoxTable.TABLE_CHAT_BOX)
                        .append(".")
                        .append(ChatBoxTable._ID)
                        .append(")");
                builder.setTables(sb.toString());
                break;
            case CODE_MODERATORS:
                builder.setTables(DefaultUserTable.TABLE);
                break;
            case CODE_SYNCED_BOOKMARK:
                builder.appendWhere(
                        SyncedBookmarkTable.CHAT_BOX_ID + "=" + uri.getLastPathSegment());
                builder.setTables(SyncedBookmarkTable.TABLE);
                break;
            default:
                throw new IllegalArgumentException("Unknown query URI: " + uri);
        }
        LogUtils.v("Query for "+uri);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        try {
            Cursor c = builder.query(db, projection, selection, selectionArgs,
                    groupBy, null, sortOrder);

            c.setNotificationUri(getContext().getContentResolver(), uri);
            return c;
        }catch (RuntimeException re) {
            LogUtils.v("Well, we got you "+re.getMessage());
            throw re;
        }
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = sUriMatcher.match(uri);
        String path;
        long id;
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        switch (uriType) {
            case CODE_CATEGORIES:
                id = db.insert(CategoryTable.TABLE_CATEGORY, null, values);
                path = PATH_CATEGORIES;
                break;
            case CODE_CONVERSATIONS:
                id = db.insert(ConversationTable.TABLE, null, values);
                path = PATH_CONVERSATIONS;
                break;
            case CODE_MODERATORS:
                id = db.insert(DefaultUserTable.TABLE, null, values);
                path = PATH_MODERATORS;
                break;
            case CODE_MESSAGES:
                id = db.insertWithOnConflict(MessageTable.TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
                path = PATH_MESSAGES;
                break;
            case CODE_CHAT_BOXES:
                id = db.insertWithOnConflict(ChatBoxTable.TABLE_CHAT_BOX, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                path = PATH_CHAT_BOXES;
                break;
            case CODE_NOTIFICATION_MESSAGES:
                id = db.insertWithOnConflict(NotificationMessagesTable.TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
                path = PATH_NOTIFICATION_MESSAGES;
                break;
            case CODE_SYNCED_BOOKMARKS:
                id = db.insertWithOnConflict(SyncedBookmarkTable.TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
                path = PATH_SYNCED_BOOKMARKS;
                break;
            default:
                throw new IllegalArgumentException("Unknown insert URI: " + uri);
        }

        if (id != -1) {
            getContext().getContentResolver().notifyChange(uri, null);
        } else {
            // TODO determine the error and handle it correctly.
        }
        return Uri.parse(path.toString().isEmpty() ? null : path.toString() + "/" + id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // For now, only support to delete all records in the table.
        int uriType = sUriMatcher.match(uri);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int rowsDeleted;
        switch (uriType) {
            case CODE_CATEGORIES:
                rowsDeleted = db.delete(CategoryTable.TABLE_CATEGORY,
                        selection, selectionArgs);
                break;
            case CODE_MESSAGES:
                rowsDeleted = db.delete(MessageTable.TABLE,
                        selection, selectionArgs);
                break;
            case CODE_CATEGORIZED_CHAT_BOXES:
                rowsDeleted = db.delete(ChatBoxTable.TABLE_CHAT_BOX,
                        ChatBoxTable.CATEGORY_TITLE + " IS NOT NULL AND " +
                                "TRIM(" + ChatBoxTable.CATEGORY_TITLE + ") != ''",
                        selectionArgs);
                break;
            case CODE_CONVERSATIONS:
                rowsDeleted = db.delete(ConversationTable.TABLE,
                        selection, selectionArgs);
                break;
            case CODE_MODERATORS:
                rowsDeleted = db.delete(DefaultUserTable.TABLE,
                        selection, selectionArgs);
                break;
            case CODE_CONVERSATION:
                String deleteSelection;
                String id = uri.getLastPathSegment();
                String idSelection = ConversationTable.CONVERSATION_ID + "=\"" + id + "\"";
                if (TextUtils.isEmpty(selection)) {
                    deleteSelection = idSelection;
                } else {
                    deleteSelection = idSelection + " AND " + selection;
                }
                rowsDeleted = db.delete(ConversationTable.TABLE,
                        deleteSelection, selectionArgs);
                break;
            case CODE_CHAT_BOXES_ID_MESSAGES:
                String deleteMessageSelection;
                String chatboxId = uri.getPathSegments().get(1);
                String chatboxIdSelection = MessageTable.CHAT_BOX_ID + "=" + chatboxId;
                if (TextUtils.isEmpty(selection)) {
                    deleteMessageSelection = chatboxIdSelection;
                } else {
                    deleteMessageSelection = chatboxIdSelection + " AND " + selection;
                }
                rowsDeleted = db.delete(MessageTable.TABLE,
                        deleteMessageSelection, selectionArgs);
                break;
            case CODE_CONVERSATIONS_ID_MESSAGES:
                String conversationId = uri.getPathSegments().get(1);
                String conversationIdSelection = MessageTable.CONVERSATION_ID + "=\"" + conversationId + "\"";
                if (TextUtils.isEmpty(selection)) {
                    deleteMessageSelection = conversationIdSelection;
                } else {
                    deleteMessageSelection = conversationIdSelection + " AND " + selection;
                }
                rowsDeleted = db.delete(MessageTable.TABLE,
                        deleteMessageSelection, selectionArgs);
                break;
            case CODE_CHAT_BOXES:
                rowsDeleted = db.delete(ChatBoxTable.TABLE_CHAT_BOX,
                        selection, selectionArgs);
                break;
            case CODE_NOTIFICATION_MESSAGES:
                rowsDeleted = db.delete(NotificationMessagesTable.TABLE,
                        selection, selectionArgs);
                break;
            case CODE_SYNCED_BOOKMARKS:
                rowsDeleted = db.delete(SyncedBookmarkTable.TABLE,
                        selection, selectionArgs);
                break;
            case CODE_SYNCED_BOOKMARK:
                idSelection = SyncedBookmarkTable.CHAT_BOX_ID + "=" + uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    deleteSelection = idSelection;
                } else {
                    deleteSelection = idSelection + " AND " + selection;
                }
                rowsDeleted = db.delete(SyncedBookmarkTable.TABLE,
                        deleteSelection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown delete URI: " + uri);
        }

        if (rowsDeleted > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int uriType = sUriMatcher.match(uri);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        String table;
        String updatedSelection = null;
        switch (uriType) {
            case CODE_CATEGORIES:
                table = CategoryTable.TABLE_CATEGORY;
                break;
            case CODE_CATEGORY:
                String title = uri.getLastPathSegment();
                String titleSelection =
                        CategoryTable.TITLE + "=\"" + title + "\"";
                if (TextUtils.isEmpty(selection)) {
                    updatedSelection = titleSelection;
                } else {
                    updatedSelection = titleSelection + " AND " + selection;
                }
                table = CategoryTable.TABLE_CATEGORY;
                break;
            case CODE_CONVERSATIONS_ID_MESSAGES:
                updatedSelection =
                        MessageTable.CONVERSATION_ID + "=\"" + uri.getPathSegments().get(1) + "\"";
                table = MessageTable.TABLE;
                break;
            case CODE_CONVERSATION:
                String id = uri.getLastPathSegment();
                String idSelection = ConversationTable.CONVERSATION_ID + "=\"" + id + "\"";
                if (TextUtils.isEmpty(selection)) {
                    updatedSelection = idSelection;
                } else {
                    updatedSelection = idSelection + " AND " + selection;
                }
                table = ConversationTable.TABLE;
                break;
            case CODE_CHAT_BOXES:
                table = ChatBoxTable.TABLE_CHAT_BOX;
                break;
            case CODE_CHAT_BOX:
                id = uri.getLastPathSegment();
                idSelection = ChatBoxTable._ID + "=" + id;
                if (TextUtils.isEmpty(selection)) {
                    updatedSelection = idSelection;
                } else {
                    updatedSelection = idSelection + " AND " + selection;
                }
                table = ChatBoxTable.TABLE_CHAT_BOX;
                break;
            case CODE_CATEGORIES_TITLE_CHAT_BOXES:
                String categoryTitle = uri.getPathSegments().get(1);
                String categorySelection = ChatBoxTable.CATEGORY_TITLE + "=\"" + categoryTitle + "\"";
                if (TextUtils.isEmpty(selection)) {
                    updatedSelection = categorySelection;
                } else {
                    updatedSelection = categorySelection + " AND " + selection;
                }
                table = ChatBoxTable.TABLE_CHAT_BOX;
                break;
            case CODE_MESSAGES:
                table = MessageTable.TABLE;
                break;
            case CODE_MESSAGE:
                id = uri.getLastPathSegment();
                idSelection = MessageTable._ID + "=" + id;
                if (TextUtils.isEmpty(selection)) {
                    updatedSelection = idSelection;
                } else {
                    updatedSelection = idSelection + " AND " + selection;
                }
                table = MessageTable.TABLE;
                break;
            case CODE_CHAT_BOXES_ID_MESSAGES:
                String chatBoxId = uri.getPathSegments().get(1);
                String chatBoxIdSelection = MessageTable._ID + "=" + chatBoxId;
                if (TextUtils.isEmpty(selection)) {
                    updatedSelection = chatBoxIdSelection;
                } else {
                    updatedSelection = chatBoxIdSelection + " AND " + selection;
                }
                table = MessageTable.TABLE;
                break;
            case CODE_SYNCED_BOOKMARKS:
                updatedSelection = selection;
                table = SyncedBookmarkTable.TABLE;
                break;
            case CODE_SYNCED_BOOKMARK:
                idSelection = SyncedBookmarkTable.CHAT_BOX_ID + "=" + uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    updatedSelection = idSelection;
                } else {
                    updatedSelection = idSelection + " AND " + selection;
                }
                table = SyncedBookmarkTable.TABLE;
                break;
            default:
                throw new IllegalArgumentException("Unknown update URI: " + uri);
        }

        int rowsUpdated = db.update(table,
                values,
                updatedSelection,
                null);
        if (rowsUpdated > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }


}
