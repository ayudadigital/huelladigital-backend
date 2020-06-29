package com.huellapositiva.application.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class FailedToPersistUser extends RuntimeException {
    public FailedToPersistUser(String message, Throwable cause) {
        super(message, cause);
    }
}
