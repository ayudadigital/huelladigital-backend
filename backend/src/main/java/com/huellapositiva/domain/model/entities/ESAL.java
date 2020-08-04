package com.huellapositiva.domain.model.entities;

import com.huellapositiva.domain.exception.UserAlreadyHasESALException;
import com.huellapositiva.domain.model.valueobjects.EmailAddress;
import lombok.Getter;

@Getter
public class ESAL {

    private final String name;
    private ContactPerson contactPerson;

    public ESAL(String name) {
        this.name = name;
    }

    /** The user is required to be registered and not to be part of an organization
     *
     * @param contactPerson
     */
    public void addContactPerson(ContactPerson contactPerson) {
        if (contactPerson.hasESAL()) {
            throw new UserAlreadyHasESALException();
        }
        this.contactPerson = contactPerson;
    }

    public EmailAddress getEmail() {
        return this.contactPerson.getEmailAddress();
    }
}
