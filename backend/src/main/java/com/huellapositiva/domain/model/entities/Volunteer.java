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

    public Volunteer(EmailAddress emailAddress, Id id) {
        super(emailAddress, id);
    }

    public Volunteer(EmailAddress emailAddress, PasswordHash passwordHash, Id id) {
        super(emailAddress, passwordHash,id);
    }
}
