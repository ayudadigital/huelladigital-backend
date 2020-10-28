package com.huellapositiva.domain.model.entities;

import com.huellapositiva.domain.model.valueobjects.EmailAddress;
import com.huellapositiva.domain.model.valueobjects.Id;
import com.huellapositiva.domain.model.valueobjects.PasswordHash;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor

public class User {

    private final EmailAddress emailAddress;

    private PasswordHash passwordHash;

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

    public User(EmailAddress emailAddress, PasswordHash passwordHash, Id id) {
        this.emailAddress = emailAddress;
        this.passwordHash = passwordHash;
        this.id = id;
    }

    public String getFullName() {
        if (name == null) {
            return null;
        } else if (surname == null) {
            return name;
        }
        return String.format("%s %s", name, surname);
    }
}
