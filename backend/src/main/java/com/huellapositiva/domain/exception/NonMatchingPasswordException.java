package com.huellapositiva.domain.exception;

public class NonMatchingPasswordException extends RuntimeException {
    public NonMatchingPasswordException(String message) { super(message); }
}
