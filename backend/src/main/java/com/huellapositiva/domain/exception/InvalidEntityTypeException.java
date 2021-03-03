package com.huellapositiva.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidEntityTypeException extends RuntimeException {
    public InvalidEntityTypeException(String message) { super(message); }
}
