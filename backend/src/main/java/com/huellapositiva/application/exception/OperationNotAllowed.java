package com.huellapositiva.application.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class OperationNotAllowed extends RuntimeException{
    public OperationNotAllowed(String message) {
        super(message);
    }
}
