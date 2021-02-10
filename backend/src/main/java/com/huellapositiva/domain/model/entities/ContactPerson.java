package com.huellapositiva.domain.model.entities;

import com.huellapositiva.application.dto.RegisterESALMemberRequestDto;
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

    public ContactPerson(Id accountId, EmailAddress emailAddress, Id id) {
        super(accountId, emailAddress, id);
    }

    public ContactPerson(Id accountId, EmailAddress emailAddress, PasswordHash passwordHash, Id id) {
        super(accountId, emailAddress, passwordHash, id);
    }

    public ContactPerson(Id accountId, EmailAddress emailAddress, PasswordHash passwordHash, Id id, RegisterESALMemberRequestDto dto) {
        super(accountId, emailAddress, passwordHash, id);
        this.name = dto.getName();
        this.surname = dto.getSurname();
        this.phoneNumber = dto.getPhoneNumber();
    }

    public boolean hasESAL() {
        return joinedEsal != null;
    }
}
