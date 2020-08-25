package com.huellapositiva.domain.model.entities;

import com.huellapositiva.domain.model.valueobjects.EmailAddress;
import com.huellapositiva.domain.model.valueobjects.Id;
import lombok.Getter;

@Getter
public class Volunteer extends User {

    public Volunteer(EmailAddress emailAddress, Id id) {
        super(emailAddress, id);
    }
}
