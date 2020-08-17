package com.huellapositiva.domain.exception;

public class InvalidProposalRequestException extends RuntimeException {
    public InvalidProposalRequestException(String message) {
        super(message);
    }
}
