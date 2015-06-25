package com.chatwing.whitelabel.pojos;

import com.google.gson.annotations.SerializedName;

/**
 * Created by steve on 20/06/2014.
 */
public class SyncedBookmark {
    @SerializedName("id")
    private int bookmarkId;
    @SerializedName("date_created")
    private long createdDate;
    @SerializedName("chatbox")
    private ChatBox chatBox;
    private boolean isSynced;

    public static SyncedBookmark createLocalBookmark(LightWeightChatBox lightWeightChatBox) {
        SyncedBookmark bookmark = new SyncedBookmark();
        bookmark.isSynced = false;
        bookmark.chatBox = new ChatBox(lightWeightChatBox.getId(),
                lightWeightChatBox.getKey(),
                lightWeightChatBox.getName(),
                lightWeightChatBox.getFayeChannel(),
                lightWeightChatBox.getAlias());
        bookmark.createdDate = System.currentTimeMillis();
        return bookmark;
    }

    public void setChatBox(ChatBox chatBox) {
        this.chatBox = chatBox;
    }

    public ChatBox getChatBox() {
        return chatBox;
    }

    public int getBookmarkId() {
        return bookmarkId;
    }

    public long getCreatedDate() {
        return createdDate;
    }

    public void setIsSynced(boolean isSynced) {
        this.isSynced = isSynced;
    }

    public boolean isSynced() {
        return isSynced;
    }

    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }

    public void setBookmarkId(int bookmarkId) {
        this.bookmarkId = bookmarkId;
    }

}
