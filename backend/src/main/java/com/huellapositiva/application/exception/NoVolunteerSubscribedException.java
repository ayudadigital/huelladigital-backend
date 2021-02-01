package com.huellapositiva.application.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NoVolunteerSubscribedException extends RuntimeException {

    public NoVolunteerSubscribedException(String message) {
        super(message);
    }
}
