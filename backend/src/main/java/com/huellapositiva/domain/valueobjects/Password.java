package com.huellapositiva.domain.valueobjects;

import com.huellapositiva.domain.exception.PasswordNotAllowed;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class Password {
    private String password;

    private Password(String password) {
        this.password = password;
    }

    public static Password from(String password) {
        int minimumNumberOfCharactersAllowed = 6;
        if(password.length() < minimumNumberOfCharactersAllowed){
            throw new PasswordNotAllowed("Not allowed password less than six characters");
        }
        return new Password(password);
    }

    @Override
    public String toString() {
        return password;
    }
}

