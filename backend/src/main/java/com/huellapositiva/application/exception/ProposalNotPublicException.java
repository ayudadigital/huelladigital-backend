package com.huellapositiva.application.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * This exception is thrown when a proposal is neither PUBLISHED nor FINISHED
 * @see com.huellapositiva.domain.model.valueobjects.ProposalStatus
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ProposalNotPublicException extends RuntimeException {

    public ProposalNotPublicException(String message) {
        super(message);
    }
}
