package com.huellapositiva.application.exception;

/**
 * This exception is thrown when a proposal is neither PUBLISHED nor FINISHED
 * @see com.huellapositiva.domain.model.valueobjects.ProposalStatus
 */
public class ProposalNotPublicException extends RuntimeException {

    public ProposalNotPublicException(){
        super();
    }

    public ProposalNotPublicException(String message) {
        super(message);
    }
}
