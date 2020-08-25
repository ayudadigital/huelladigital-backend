package com.huellapositiva.domain.model.valueobjects;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class EmailAddress {
    private final String email;

    private EmailAddress(String email) {
        this.email = email;
    }

    public static EmailAddress from(String email) {
        return new EmailAddress(email);
    }

    @Override
    public String toString() {
        return email;
    }
}


