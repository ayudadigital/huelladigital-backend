package com.huellapositiva.domain.model.entities;

import com.huellapositiva.domain.model.valueobjects.EmailAddress;
import com.huellapositiva.domain.model.valueobjects.Id;
import com.huellapositiva.domain.model.valueobjects.PasswordHash;
import com.huellapositiva.domain.model.valueobjects.PhoneNumber;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContactPerson extends User {

    private ESAL joinedEsal;

    private String name;

    private String surname;

    private PhoneNumber phoneNumber;

    public ContactPerson(Id accountId, EmailAddress emailAddress, Id contactPersonId) {
        super(accountId, emailAddress, contactPersonId);
    }

    public ContactPerson(Id accountId, EmailAddress emailAddress, PasswordHash passwordHash, Id contactPersonId) {
        super(accountId, emailAddress, passwordHash, contactPersonId);
    }

    public ContactPerson(Id accountId, EmailAddress emailAddress, PasswordHash passwordHash, Id contactPersonId, String name, String surname, PhoneNumber phoneNumber) {
        super(accountId, emailAddress, passwordHash, contactPersonId);
        this.name = name;
        this.surname = surname;
        this.phoneNumber = phoneNumber;
    }

    public boolean hasESAL() {
        return joinedEsal != null;
    }
}
