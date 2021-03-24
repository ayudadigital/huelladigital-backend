package com.huellapositiva.domain.exception;

public class RequirementAlreadyExistsException extends RuntimeException{
    public RequirementAlreadyExistsException(String message) {
        super(message);
    }
}
