package com.huellapositiva.application.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class SomeFieldIsNullException extends RuntimeException{
    public SomeFieldIsNullException(String message) {
        super(message);
    }
}
