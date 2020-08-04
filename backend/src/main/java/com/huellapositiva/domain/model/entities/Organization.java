package com.huellapositiva.domain.model.entities;

import com.huellapositiva.domain.exception.UserAlreadyHasOrganizationException;
import com.huellapositiva.domain.model.valueobjects.EmailAddress;
import lombok.Getter;

@Getter
public class Organization {

    private final String name;
    private ContactPerson contactPerson;

    public Organization(String name) {
        this.name = name;
    }

    /** The user is required to be already registered
     *
     * @param contactPerson
     */
    public void addUserAsMember(ContactPerson contactPerson) {
        if (contactPerson.hasOrganization()) {
            throw new UserAlreadyHasOrganizationException();
        }
        this.contactPerson = contactPerson;
    }

    public EmailAddress getEmail() {
        return this.contactPerson.getEmailAddress();
    }
}
