package com.supermarketpos.util;

import static com.mysql.cj.util.StringUtils.isNullOrEmpty;

public final class ValidationUtil {

    private static final int MAX_USERNAME_LENGTH = 50;
    private static final int MAX_PASSWORD_LENGTH = 100;

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
        private static final java.util.regex.Pattern EMAIL_PATTERN =
                java.util.regex.Pattern.compile("^[\\w.+\\-]+@[\\w\\-]+\\.[a-zA-Z]{2,}$");

        private static final java.util.regex.Pattern MOBILE_PATTERN =
                java.util.regex.Pattern.compile("^[6-9]\\d{9}$");

        private static final java.util.regex.Pattern GST_PATTERN =
                java.util.regex.Pattern.compile("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$");

        public static boolean isValidEmail(String email) {
            if (isNullOrEmpty(email)) return true; // optional field
            return EMAIL_PATTERN.matcher(email.trim()).matches();
        }

        public static boolean isValidMobile(String mobile) {
            return !isNullOrEmpty(mobile) && MOBILE_PATTERN.matcher(mobile.trim()).matches();
        }

        public static boolean isValidGstNumber(String gstNumber) {
            if (isNullOrEmpty(gstNumber)) return true; // optional field
            return GST_PATTERN.matcher(gstNumber.trim().toUpperCase()).matches();
        }
        public static void requireTrue(boolean condition, String message) {
            if (!condition) throw new IllegalArgumentException(message);
        }

        public static void requireNonBlank(String value, String fieldName) {
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException(fieldName + " must not be blank.");
            }
        }

        public static String sanitize(String value) {
            return value == null ? "" : value.trim();
        }
    }
}
