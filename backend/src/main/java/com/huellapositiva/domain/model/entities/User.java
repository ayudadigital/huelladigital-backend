package com.huellapositiva.domain.model.entities;

import com.huellapositiva.domain.model.valueobjects.EmailAddress;
import com.huellapositiva.domain.model.valueobjects.Id;
import lombok.Getter;

@Getter
public class User {

    private final EmailAddress emailAddress;

    private final Id id;

    private String name;

    private String surname;

    public User(EmailAddress emailAddress, Id id) {
        this.emailAddress = emailAddress;
        this.id = id;
    }

    public User(EmailAddress emailAddress, Id id, String name, String surname) {
        this.emailAddress = emailAddress;
        this.id = id;
        this.name = name;
        this.surname = surname;
    }

    public String getFullName() {
        if (name == null) {
            return null;
        }
        if (surname == null) {
            return String.format("%s", name);
        }
        return String.format("%s %s", name, surname);
    }
}
