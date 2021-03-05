package com.huellapositiva.domain.model.entities;

import com.huellapositiva.domain.model.valueobjects.EmailAddress;
import com.huellapositiva.domain.model.valueobjects.Id;
import com.huellapositiva.domain.model.valueobjects.PasswordHash;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.net.URL;

@Builder
@Getter
@AllArgsConstructor
@Setter
public class User {

    private final Id accountId;

    private final EmailAddress emailAddress;

    private PasswordHash passwordHash;

    private final Id id;

    private String name;

    private String surname;

    private URL photo;

    public User(Id accountId, EmailAddress emailAddress, Id id) {
        this.accountId = accountId;
        this.emailAddress = emailAddress;
        this.id = id;
    }

    public User(Id accountId, EmailAddress emailAddress, Id id, String name, String surname) {
        this.accountId = accountId;
        this.emailAddress = emailAddress;
        this.id = id;
        this.name = name;
        this.surname = surname;
    }

    public User(Id accountId, EmailAddress emailAddress, PasswordHash passwordHash, Id id) {
        this.accountId = accountId;
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
