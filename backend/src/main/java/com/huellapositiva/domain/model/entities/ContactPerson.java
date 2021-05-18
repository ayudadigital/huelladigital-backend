package com.huellapositiva.domain.model.entities;

import com.huellapositiva.application.dto.RegisterContactPersonDto;
import com.huellapositiva.domain.model.valueobjects.EmailAddress;
import com.huellapositiva.domain.model.valueobjects.Id;
import com.huellapositiva.domain.model.valueobjects.PasswordHash;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContactPerson extends User {

    private ESAL joinedEsal;

    private String name;

    private String surname;

    private String phoneNumber;

    public ContactPerson(Id accountId, EmailAddress emailAddress, Id contactPersonId) {
        super(accountId, emailAddress, contactPersonId);
    }

    public ContactPerson(Id accountId, EmailAddress emailAddress, PasswordHash passwordHash, Id contactPersonId) {
        super(accountId, emailAddress, passwordHash, contactPersonId);
    }

    public ContactPerson(Id accountId, EmailAddress emailAddress, PasswordHash passwordHash, Id contactPersonId, RegisterContactPersonDto dto) {
        super(accountId, emailAddress, passwordHash, contactPersonId);
        this.name = dto.getName();
        this.surname = dto.getSurname();
        this.phoneNumber = dto.getPhoneNumber();
    }

    public boolean hasESAL() {
        return joinedEsal != null;
    }
}
