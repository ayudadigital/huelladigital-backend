package com.huellapositiva.domain.model.entities;

import com.huellapositiva.domain.model.valueobjects.EmailAddress;
import com.huellapositiva.domain.model.valueobjects.Id;
import lombok.Getter;

@Getter
public class ContactPerson extends User {

    private Organization organization;

    public ContactPerson(EmailAddress emailAddress, Id id) {
        super(emailAddress, id);
    }

    public ContactPerson(EmailAddress emailAddress, Id id, Organization organization) {
        super(emailAddress, id);
        this.organization = organization;
    }

    public boolean hasOrganization(){
        return this.organization != null;
    }

}
