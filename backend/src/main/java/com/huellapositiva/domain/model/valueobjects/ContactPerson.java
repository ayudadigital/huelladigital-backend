package com.huellapositiva.domain.model.valueobjects;

import com.huellapositiva.domain.model.entities.ESAL;
import com.huellapositiva.domain.model.entities.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContactPerson extends User {


    private ESAL joinedEsal;

    public ContactPerson(EmailAddress emailAddress, Id id) {
        super(emailAddress, id);
    }

}
