package com.fitpulse.exception;

/**
 * Exception thrown when database operations or queries fail.
 * Wraps SQLExceptions to insulate higher architectural tiers.
 */
public class DatabaseOperationException extends FitPulseException {
    public DatabaseOperationException(String message) {
        super(message, "ERR_DB_OP");
    }

    public DatabaseOperationException(String message, Throwable cause) {
        super(message, "ERR_DB_OP", cause);
    }
}
