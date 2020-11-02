package com.huellapositiva.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class TimeForRecoveringPasswordExpiredException extends RuntimeException {
    public TimeForRecoveringPasswordExpiredException(String message) {
        super(message);
    }
}
