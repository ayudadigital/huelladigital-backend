package com.huellapositiva.application.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ConflictPersistingUserException extends RuntimeException {
    public ConflictPersistingUserException(String message, Throwable cause) {
        super(message, cause);
    }
}
