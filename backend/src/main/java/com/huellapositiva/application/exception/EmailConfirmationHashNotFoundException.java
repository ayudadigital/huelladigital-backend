package com.huellapositiva.application.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class EmailConfirmationHashNotFoundException extends RuntimeException {

    public EmailConfirmationHashNotFoundException(String message) {
        super(message);
    }
}
