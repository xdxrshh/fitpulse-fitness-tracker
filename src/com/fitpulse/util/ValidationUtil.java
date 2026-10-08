package com.fitpulse.util;

import com.fitpulse.exception.ValidationException;

/**
 * Validates entity attributes and user form inputs.
 * Demonstrates:
 * - OOP: Exception Handling (throws custom ValidationException)
 */
public final class ValidationUtil {

    private ValidationUtil() {}

    public static void requireNonEmpty(String text, String fieldName) throws ValidationException {
        if (text == null || text.trim().isEmpty()) {
            throw new ValidationException(fieldName + " cannot be empty.");
        }
    }

    public static void requireValidEmail(String email) throws ValidationException {
        requireNonEmpty(email, "Email");
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new ValidationException("Invalid email format: " + email);
        }
    }

    public static void requirePositive(double value, String fieldName) throws ValidationException {
        if (value <= 0) {
            throw new ValidationException(fieldName + " must be greater than zero.");
        }
    }

    public static void requireNonNegative(double value, String fieldName) throws ValidationException {
        if (value < 0) {
            throw new ValidationException(fieldName + " cannot be negative.");
        }
    }
}
