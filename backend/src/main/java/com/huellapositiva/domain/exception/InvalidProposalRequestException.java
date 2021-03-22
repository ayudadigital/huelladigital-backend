package com.huellapositiva.domain.exception;

import com.huellapositiva.application.exception.InvalidFieldException;

public class InvalidProposalRequestException extends InvalidFieldException {
    public InvalidProposalRequestException(String message) {
        super(message);
    }
}
