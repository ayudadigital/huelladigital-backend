package com.huellapositiva.domain.model.entities;

import com.huellapositiva.domain.model.valueobjects.EmailAddress;
import com.huellapositiva.domain.model.valueobjects.Id;
import com.huellapositiva.domain.model.valueobjects.PasswordHash;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContactPerson extends User {

    private ESAL joinedEsal;

    public ContactPerson(Id accountId, EmailAddress emailAddress, Id id) {
        super(accountId, emailAddress, id);
    }

    public ContactPerson(Id accountId, EmailAddress emailAddress, PasswordHash passwordHash, Id id) {
        super(accountId, emailAddress, passwordHash, id);
    }

    public boolean hasESAL() {
        return joinedEsal != null;
    }
}
