package com.huellapositiva.application.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.GONE)
public class EmailConfirmationExpired extends RuntimeException{
    public EmailConfirmationExpired(String message) {
        super(message);
    }
}
