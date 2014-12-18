package com.chatwing.whitelabel.utils;

import android.text.TextUtils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Author: Huy Nguyen
 * Date: 4/20/13
 * Time: 5:50 PM
 */
public class StringUtils {

    /**
     * Generates md5 hash from input string.
     * Reference: http://androidbridge.blogspot.com/2011/11/how-to-create-md5-hash-in-android.html
     *
     * @param input
     *
     * @return md5 hash
     */
    public static String getMd5Hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger number = new BigInteger(1, messageDigest);
            String md5 = number.toString(16);

            while (md5.length() < 32) {
                md5 = "0" + md5;
            }

            return md5;
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    public static String repeatedString(String src, int times) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < times; ++i) {
            builder.append(src);
        }
        return builder.toString();
    }

    public static String join(CharSequence delimiter, Object[] tokens) {
        return tokens != null && tokens.length > 0
                ? TextUtils.join(delimiter, tokens)
                : null;
    }

    public static String applyFilters(String src, List<String> filters, String replaceSequence) {
        if (!TextUtils.isEmpty(src)) {
            for (String filter : filters) {
                String placeHolder = StringUtils.repeatedString(replaceSequence, filter.length());
                filter = "(?ui)" // append this for Unicode case-insensitive replacement
                        + Pattern.quote(filter); // escape special chars in filter as well
                src = src.replaceAll(filter, placeHolder);
            }
        }
        return src;
    }
}
