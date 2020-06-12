package com.huellapositiva.infrastructure.exception;

public class RequestAuthenticationUserException extends RuntimeException{
    public RequestAuthenticationUserException(String message, Throwable cause) {
        super(message, cause);
    }
}
