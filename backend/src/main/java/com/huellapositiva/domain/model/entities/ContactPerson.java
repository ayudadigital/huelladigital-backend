package com.huellapositiva.domain.model.entities;

import com.huellapositiva.domain.model.valueobjects.EmailAddress;
import com.huellapositiva.domain.model.valueobjects.Id;
import lombok.Getter;

@Getter
public class ContactPerson extends User {

    private ESAL ESAL;

    public ContactPerson(EmailAddress emailAddress, Id id) {
        super(emailAddress, id);
    }

    public ContactPerson(EmailAddress emailAddress, Id id, ESAL ESAL) {
        super(emailAddress, id);
        this.ESAL = ESAL;
    }

    public boolean hasESAL(){
        return this.ESAL != null;
    }

}
