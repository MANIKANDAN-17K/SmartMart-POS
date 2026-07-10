package com.supermarketpos.util;

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
    }
}
