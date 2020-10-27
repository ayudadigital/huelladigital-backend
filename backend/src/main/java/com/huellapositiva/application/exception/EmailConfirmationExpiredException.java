package com.huellapositiva.application.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.GONE)
public class EmailConfirmationExpiredException extends RuntimeException{
    public EmailConfirmationExpiredException(String message) {
        super(message);
    }
}
