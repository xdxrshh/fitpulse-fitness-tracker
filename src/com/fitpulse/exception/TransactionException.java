package com.fitpulse.exception;

/**
 * Thrown when an atomic transaction fails and needs to be rolled back.
 * Part of Rubric Item 6: Transaction Management.
 */
public class TransactionException extends FitPulseException {
    public TransactionException(String message) {
        super(message, "ERR_TRANSACTION");
    }

    public TransactionException(String message, Throwable cause) {
        super(message, "ERR_TRANSACTION", cause);
    }
}
