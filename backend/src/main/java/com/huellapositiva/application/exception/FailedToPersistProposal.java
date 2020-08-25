package com.huellapositiva.application.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class FailedToPersistProposal extends RuntimeException {
    public FailedToPersistProposal(String message, Throwable cause) {
        super(message, cause);
    }
    public FailedToPersistProposal(String message) {
        super(message);
    }
}
