package com.huellapositiva.application.exception;

/**
 * This exception is thrown when a proposal is neither PUBLISHED nor FINISHED
 * @see com.huellapositiva.domain.model.valueobjects.ProposalStatus
 */
public class ProposalNotPublic extends RuntimeException {

    public ProposalNotPublic(){
        super();
    }

    public ProposalNotPublic(String message) {
        super(message);
    }
}
