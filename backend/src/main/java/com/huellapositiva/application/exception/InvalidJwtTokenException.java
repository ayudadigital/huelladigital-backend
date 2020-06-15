package com.huellapositiva.application.exception;

public class InvalidJwtTokenException extends Exception {

    public InvalidJwtTokenException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public InvalidJwtTokenException(String message) {
        super(message);
    }
}
