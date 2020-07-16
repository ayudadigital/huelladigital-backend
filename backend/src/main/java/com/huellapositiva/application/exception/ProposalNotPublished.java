package com.huellapositiva.application.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ProposalNotPublished extends RuntimeException {

    public ProposalNotPublished(){
    }

    public ProposalNotPublished(String message) {
        super(message);
    }
}
