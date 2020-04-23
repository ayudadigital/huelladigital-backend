package com.huellapositiva.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class EmptyTemplateException extends RuntimeException{
    public EmptyTemplateException(String message) {
        super(message);
    }
}
