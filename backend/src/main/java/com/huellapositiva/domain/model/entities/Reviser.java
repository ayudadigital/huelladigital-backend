package com.huellapositiva.domain.model.entities;

import com.huellapositiva.domain.model.valueobjects.EmailAddress;
import com.huellapositiva.domain.model.valueobjects.Id;

public class Reviser extends User {

    public Reviser(EmailAddress emailAddress, Id id) {
        super(emailAddress, id);
    }

    public Reviser(EmailAddress emailAddress, Id id, String name, String surname) {
        super(emailAddress, id, name, surname);
    }

    public static Reviser from(User user) {
        return new Reviser(user.getEmailAddress(), user.getId(), user.getName(), user.getSurname());
    }
}
