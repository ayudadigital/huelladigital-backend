package com.huellapositiva.domain.model.entities;

import com.huellapositiva.domain.model.valueobjects.EmailAddress;
import com.huellapositiva.domain.model.valueobjects.Id;
import lombok.Getter;

@Getter
public class User {

    private EmailAddress emailAddress;

    private Id id;

    public User(EmailAddress emailAddress, Id id) {
        this.emailAddress = emailAddress;
        this.id = id;
    }

    public User() {}

}
