package com.supermarketpos.util;

import org.mindrot.jbcrypt.BCrypt;

public final class HashUtil {

    private static final int WORK_FACTOR = 12;

    private HashUtil() {
    }

    public static String hash(String plainText) {
        return BCrypt.hashpw(plainText, BCrypt.gensalt(WORK_FACTOR));
    }

    public static boolean verify(String plainText, String hash) {
        if (plainText == null || hash == null) {
            return false;
        }
        return BCrypt.checkpw(plainText, hash);
    }
}
