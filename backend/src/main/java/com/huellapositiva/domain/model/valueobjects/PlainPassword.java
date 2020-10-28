package com.huellapositiva.domain.model.valueobjects;

import com.huellapositiva.application.exception.PasswordNotAllowedException;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class PlainPassword {
    private final String value;

    private PlainPassword(String password) {
        this.value = password;
    }

    public static PlainPassword from(String password) {
       int minimumNumberOfCharactersAllowed = 6;
        if(password.length() < minimumNumberOfCharactersAllowed){
            throw new PasswordNotAllowedException("Not allowed password less than six characters");
        }
        return new PlainPassword(password);
    }

    @Override
    public String toString() {
        return value;
    }
}

