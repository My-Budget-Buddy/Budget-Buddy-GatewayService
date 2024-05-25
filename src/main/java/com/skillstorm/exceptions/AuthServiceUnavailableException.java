package com.skillstorm.exceptions;

public class AuthServiceUnavailableException extends IllegalStateException {
    public AuthServiceUnavailableException() {
        super("No Auth service instance available.");
    }
}
