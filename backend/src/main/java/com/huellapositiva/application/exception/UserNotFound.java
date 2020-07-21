package com.huellapositiva.application.exception;

public class UserNotFound extends RuntimeException{
    public UserNotFound(){
        super();
    }

    public UserNotFound(String message){
        super(message);
    }
}
