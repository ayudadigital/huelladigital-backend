package com.huellapositiva.domain.exception;

public class InvalidNewPasswordException extends RuntimeException {
    public InvalidNewPasswordException(String message) { super(message); }
}
