package com.chatwing.whitelabel.generators;

import android.text.TextUtils;

import com.chatwing.whitelabel.utils.StringUtils;

import java.util.UUID;

import javax.inject.Inject;

/**
 * Created by nguyenthanhhuy on 11/24/13.
 */
public class MessageRandomKeyGenerator {

    @Inject
    MessageRandomKeyGenerator() {
    }

    /**
     * Generates a random key for a message, by joining params and make MD5
     * hash.
     *
     * @param content
     *         of the message.
     * @param createdDate
     *         of the message, in nanosecond.
     *
     * @return the random key to be assigned to a new message.
     */
    public String generate(String content, long createdDate) {
        String joinedString = content + "|" + createdDate;
        String hash = StringUtils.getMd5Hash(joinedString);
        if (TextUtils.isEmpty(hash)) {
            hash = UUID.randomUUID().toString();
        }
        return hash;
    }
}
