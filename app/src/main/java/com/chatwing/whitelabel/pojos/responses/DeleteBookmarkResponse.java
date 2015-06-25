package com.chatwing.whitelabel.pojos.responses;

import com.google.gson.annotations.SerializedName;

/**
 * Created by steve on 23/06/2014.
 */
public class DeleteBookmarkResponse extends BaseResponse {
    private DeletedBookmark data;

    public DeletedBookmark getData() {
        return data;
    }

    public static class DeletedBookmark{
        private int id;
        @SerializedName("chatbox_id")
        private int chatBoxId;

        public int getChatBoxId() {
            return chatBoxId;
        }
    }
}
