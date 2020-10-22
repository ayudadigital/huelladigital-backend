package com.huellapositiva.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class FileTypeNotSupportedException extends RuntimeException {
    public FileTypeNotSupportedException(String message) {
        super(message);
    }
}
