package com.fitpulse.exception;

/**
 * Thrown when entity input or business constraints are violated.
 */
public class ValidationException extends FitPulseException {
    public ValidationException(String message) {
        super(message, "ERR_VALIDATION");
    }
}
