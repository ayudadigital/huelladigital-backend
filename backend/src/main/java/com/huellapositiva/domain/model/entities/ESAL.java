package com.huellapositiva.domain.model.entities;

import com.huellapositiva.domain.model.valueobjects.EmailAddress;
import com.huellapositiva.domain.model.valueobjects.Id;
import lombok.Getter;

import java.util.Objects;

@Getter
public class ESAL {

    private final String name;

    private final Id id;

    private final EmailAddress contactPersonEmail;

    public ESAL(String name, Id id, EmailAddress contactPersonEmail) {
        this.name = name;
        this.id = id;
        this.contactPersonEmail = contactPersonEmail;
    }

    public EmailAddress getEmail() {
        return this.contactPersonEmail;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ESAL)) return false;
        ESAL esal = (ESAL) o;
        return id.equals(esal.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
