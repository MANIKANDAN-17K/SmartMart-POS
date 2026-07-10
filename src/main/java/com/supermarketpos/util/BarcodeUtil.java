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
    public static String normalize(String raw) {
        if (raw == null) return "";
        return raw.trim();
    }

    /** Basic EAN-13 / UPC-A length check (not a full checksum). */
    public static boolean looksValid(String barcode) {
        if (barcode == null) return false;
        String s = barcode.trim();
        return s.matches("\\d{8,14}") || s.length() >= 3;
    }
    public static String generateBarcode() {
        long timestampPart = Instant.now().toEpochMilli();
        int randomPart = ThreadLocalRandom.current().nextInt(100, 999);
        return Long.toString(timestampPart, 36).toUpperCase() + randomPart;
    }

    public static boolean isValidFormat(String barcode) {
        return barcode != null && BARCODE_PATTERN.matcher(barcode.trim()).matches();
    }
}