package com.huellapositiva.application.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.GONE)
public class ProposalEnrollmentClosedException extends RuntimeException {

    public ProposalEnrollmentClosedException(){
        super();
    }

    public ProposalEnrollmentClosedException(String message) {
        super(message);
    }
}
