package com.huellapositiva.application.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.GONE)
public class ProposalEnrollmentClosed extends RuntimeException {

    public ProposalEnrollmentClosed(){
        super();
    }

    public ProposalEnrollmentClosed(String message) {
        super(message);
    }
}
