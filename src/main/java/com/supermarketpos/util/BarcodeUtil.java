package com.supermarketpos.util;

import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

public class BarcodeUtil {

    private static final Pattern BARCODE_PATTERN = Pattern.compile("^[A-Za-z0-9\\-]{4,50}$");

    private BarcodeUtil() {
    }

    /**
     * Generates a unique-ish barcode: timestamp (ms, base36) + 3 random digits.
     * Uniqueness must still be enforced at DB/service level.
     */
    public static String generateBarcode() {
        long timestampPart = Instant.now().toEpochMilli();
        int randomPart = ThreadLocalRandom.current().nextInt(100, 999);
        return Long.toString(timestampPart, 36).toUpperCase() + randomPart;
    }

    public static boolean isValidFormat(String barcode) {
        return barcode != null && BARCODE_PATTERN.matcher(barcode.trim()).matches();
    }
}