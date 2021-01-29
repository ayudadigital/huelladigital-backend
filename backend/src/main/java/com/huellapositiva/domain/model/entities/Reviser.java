package com.huellapositiva.domain.model.entities;

import com.huellapositiva.domain.model.valueobjects.EmailAddress;
import com.huellapositiva.domain.model.valueobjects.Id;

public class Reviser extends User {

    public Reviser(Id accountId, EmailAddress emailAddress, Id id, String name, String surname) {
        super(accountId, emailAddress, id, name, surname);
    }

    public static Reviser from(User user) {
        return new Reviser(user.getAccountId(), user.getEmailAddress(), user.getId(), user.getName(), user.getSurname());
    }
}
