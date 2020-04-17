package com.huellapositiva.domain.valueobjects;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class Email {
    private String email;

    private Email(String email) {
        this.email = email;
    }

    public static Email from(String email) {
        // TODO: validate email
        return new Email(email);
    }

    @Override
    public String toString() {
        return email;
    }
}
