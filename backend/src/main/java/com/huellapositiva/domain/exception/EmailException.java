package com.huellapositiva.domain.exception;

public class EmailException extends RuntimeException {
    public EmailException() {
    }

    public EmailException(Exception ex) {
        super(ex);
    }
}
