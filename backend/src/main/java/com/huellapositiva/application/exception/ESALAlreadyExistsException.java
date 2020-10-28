package com.huellapositiva.application.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ESALAlreadyExistsException extends RuntimeException{
    public ESALAlreadyExistsException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
