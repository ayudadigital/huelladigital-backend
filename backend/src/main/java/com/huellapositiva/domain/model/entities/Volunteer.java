package com.huellapositiva.domain.model.entities;

import com.huellapositiva.domain.model.valueobjects.EmailAddress;
import com.huellapositiva.domain.model.valueobjects.Id;
import com.huellapositiva.domain.model.valueobjects.PasswordHash;
import lombok.Getter;
import lombok.Setter;

import java.net.URL;

@Getter
@Setter
public class Volunteer extends User {

    private URL curriculumVitae;

    public Volunteer(Id accountId, EmailAddress emailAddress, Id id) {
        super(accountId, emailAddress, id);
    }

    public Volunteer(Id accountId, EmailAddress emailAddress, PasswordHash passwordHash, Id id) {
        super(accountId, emailAddress, passwordHash,id);
    }
}
