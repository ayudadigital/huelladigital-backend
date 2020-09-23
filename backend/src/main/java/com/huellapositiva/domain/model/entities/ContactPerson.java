package com.huellapositiva.domain.model.entities;

import com.huellapositiva.domain.model.valueobjects.EmailAddress;
import com.huellapositiva.domain.model.valueobjects.Id;
import com.huellapositiva.domain.model.valueobjects.PasswordHash;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContactPerson extends User {

    private ESAL joinedEsal;

    public ContactPerson(EmailAddress emailAddress, Id id) {
        super(emailAddress, id);
    }

    public ContactPerson(EmailAddress emailAddress, Id id, String name, String surname) {
        super(emailAddress, id, name, surname);
    }

    public ContactPerson(EmailAddress emailAddress, PasswordHash passwordHash, Id id) {
        super(emailAddress, passwordHash,id);
    }
}
