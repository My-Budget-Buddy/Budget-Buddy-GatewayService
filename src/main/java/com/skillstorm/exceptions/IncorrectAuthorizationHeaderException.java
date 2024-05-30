package com.skillstorm.exceptions;

public class IncorrectAuthorizationHeaderException extends RuntimeException {
    
    public IncorrectAuthorizationHeaderException() {
        super("Authorization header is missing content");
    }
}
