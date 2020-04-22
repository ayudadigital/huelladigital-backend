package com.huellapositiva.domain.valueobjects;

import com.huellapositiva.domain.exception.PasswordNotAllowed;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class PlainPassword {
    private String value;

    private PlainPassword(String password) {
        this.value = password;
    }

    public static PlainPassword from(String password) {
        int minimumNumberOfCharactersAllowed = 6;
        if(password.length() < minimumNumberOfCharactersAllowed){
            throw new PasswordNotAllowed("Not allowed password less than six characters");
        }
        return new PlainPassword(password);
    }

    @Override
    public String toString() {
        return value;
    }
}

