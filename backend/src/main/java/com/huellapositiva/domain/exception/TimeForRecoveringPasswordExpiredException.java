package com.huellapositiva.domain.exception;

public class TimeForRecoveringPasswordExpiredException extends RuntimeException {
    public TimeForRecoveringPasswordExpiredException(String message) {
        super(message);
    }
}
