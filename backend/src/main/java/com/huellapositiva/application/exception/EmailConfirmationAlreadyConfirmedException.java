package com.huellapositiva.application.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class EmailConfirmationAlreadyConfirmedException extends RuntimeException{
    public EmailConfirmationAlreadyConfirmedException(String message) {
        super(message);
    }
}