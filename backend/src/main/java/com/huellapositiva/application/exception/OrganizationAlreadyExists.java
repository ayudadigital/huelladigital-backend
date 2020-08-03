package com.huellapositiva.application.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class OrganizationAlreadyExists extends RuntimeException{
    public OrganizationAlreadyExists() {
        super();
    }
}
