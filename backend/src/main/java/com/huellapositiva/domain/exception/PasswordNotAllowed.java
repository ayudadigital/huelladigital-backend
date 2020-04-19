package com.huellapositiva.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PasswordNotAllowed extends RuntimeException {
    public PasswordNotAllowed(String message) {
        super(message);
    }
}
