package com.fitpulse.exception;

/**
 * Thrown on invalid login credentials or unauthorized actions.
 */
public class AuthenticationException extends FitPulseException {
    public AuthenticationException(String message) {
        super(message, "ERR_AUTH");
    }
}
