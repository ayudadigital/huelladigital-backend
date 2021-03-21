package com.huellapositiva.domain.exception;

public class InvalidProposalStatusException extends RuntimeException {
    public InvalidProposalStatusException(String message) {
        super(message);
    }
}
