package com.skillstorm.exceptions;

public class MissingAuthorizationHeaderException extends RuntimeException {
    public MissingAuthorizationHeaderException() {
        super("Authorization header is missing.");
    }
}
