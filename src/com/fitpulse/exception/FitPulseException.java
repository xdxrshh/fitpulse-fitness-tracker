package com.fitpulse.exception;

/**
 * Base checked exception for the FitPulse application.
 * Part of Rubric Item 1: OOP Implementation - Exception Handling.
 */
public class FitPulseException extends Exception {
    private final String errorCode;

    public FitPulseException(String message) {
        super(message);
        this.errorCode = "ERR_GENERAL";
    }

    public FitPulseException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public FitPulseException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "ERR_WRAPPED";
    }

    public FitPulseException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
