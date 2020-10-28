package com.huellapositiva.application.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class FailedToPersistProposalException extends RuntimeException {
    public FailedToPersistProposalException(String message, Throwable cause) {
        super(message, cause);
    }
    public FailedToPersistProposalException(String message) {
        super(message);
    }
}
