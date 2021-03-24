package com.huellapositiva.domain.exception;

import com.huellapositiva.application.exception.InvalidFieldException;

public class InvalidProposalCategoryException extends InvalidFieldException {
    public InvalidProposalCategoryException(String message) {
        super(message);
    }
}
