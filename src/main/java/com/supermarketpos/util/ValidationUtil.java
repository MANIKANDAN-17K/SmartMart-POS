package com.supermarketpos.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.regex.Pattern;

public final class ValidationUtil {

    private static final int MAX_USERNAME_LENGTH = 50;
    private static final int MAX_PASSWORD_LENGTH = 100;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.+\\-]+@[\\w\\-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern MOBILE_PATTERN = Pattern.compile("^[6-9]\\d{9}$");
    private static final Pattern GST_PATTERN = Pattern
            .compile("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10}$");
    private static final Pattern GST_SIMPLE_PATTERN = Pattern.compile("^[0-9A-Z]{15}$");

    private ValidationUtil() {
    }

    public static ValidationResult validateUsername(String username) {
        if (username == null || username.isEmpty()) {
            return ValidationResult.fail("Username is required");
        }
        if (containsWhitespace(username)) {
            return ValidationResult.fail("Username must not contain whitespace");
        }
        if (username.length() > MAX_USERNAME_LENGTH) {
            return ValidationResult.fail("Username must not exceed " + MAX_USERNAME_LENGTH + " characters");
        }
        return ValidationResult.ok();
    }

    public static ValidationResult validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return ValidationResult.fail("Password is required");
        }
        if (containsWhitespace(password)) {
            return ValidationResult.fail("Password must not contain whitespace");
        }
        if (password.length() > MAX_PASSWORD_LENGTH) {
            return ValidationResult.fail("Password must not exceed " + MAX_PASSWORD_LENGTH + " characters");
        }
        return ValidationResult.ok();
    }

    private static boolean containsWhitespace(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (Character.isWhitespace(value.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    public static boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static String trimOrEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    public static boolean exceedsMaxLength(String value, int maxLength) {
        return value != null && value.length() > maxLength;
    }

    public static boolean isPositive(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) > 0;
    }

    public static boolean isSellingPriceValid(BigDecimal costPrice, BigDecimal sellingPrice) {
        if (costPrice == null || sellingPrice == null)
            return false;
        return sellingPrice.compareTo(costPrice) >= 0;
    }

    public static boolean isValidGst(BigDecimal gst) {
        if (gst == null)
            return false;
        return gst.compareTo(BigDecimal.ZERO) >= 0 && gst.compareTo(new BigDecimal("100")) <= 0;
    }

    public static boolean isValidEmail(String email) {
        if (isNullOrEmpty(email))
            return true; // optional field
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidMobile(String mobile) {
        if (isNullOrEmpty(mobile))
            return false;
        return MOBILE_PATTERN.matcher(mobile.trim()).matches();
    }

    public static boolean isValidPhone(String phone) {
        if (isNullOrEmpty(phone))
            return false;
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    public static boolean isValidGstNumber(String gstNumber) {
        if (isNullOrEmpty(gstNumber))
            return true; // optional field
        return GST_PATTERN.matcher(gstNumber.trim().toUpperCase()).matches()
                || GST_SIMPLE_PATTERN.matcher(gstNumber.trim().toUpperCase()).matches();
    }

    public static void requireTrue(boolean condition, String message) {
        if (!condition)
            throw new IllegalArgumentException(message);
    }

    public static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank.");
        }
    }

    public static String sanitize(String value) {
        return value == null ? "" : value.trim();
    }

    public static boolean isValidDateRange(LocalDate start, LocalDate end) {
        return start != null && end != null && !start.isAfter(end);
    }

    public static boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }

    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static final class ValidationResult {
        private final boolean valid;
        private final String message;

        private ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public static ValidationResult ok() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult fail(String message) {
            return new ValidationResult(false, message);
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }
    }
}
