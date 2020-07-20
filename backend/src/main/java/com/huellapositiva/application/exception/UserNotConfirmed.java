package com.huellapositiva.application.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.PRECONDITION_FAILED)
public class UserNotConfirmed extends RuntimeException{
    public UserNotConfirmed(){
        super();
    }
}
