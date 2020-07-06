package com.huellapositiva.application.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class EmailConfirmationAlreadyConfirmed extends RuntimeException{
    public EmailConfirmationAlreadyConfirmed(String message) {
        super(message);
    }
}