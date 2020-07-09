package com.huellapositiva.domain;

import com.huellapositiva.domain.valueobjects.EmailConfirmation;
import com.huellapositiva.domain.valueobjects.PasswordHash;

public class ExpressRegistrationOrganization {

    private final EmailConfirmation confirmation;
    private final PasswordHash password;
    private final String name;

    public ExpressRegistrationOrganization(PasswordHash password, EmailConfirmation confirmation, String name) {
        this.confirmation = confirmation;
        this.password = password;
        this.name = name;
    }

    public String getEmail() {
        return this.confirmation.getEmailAddress();
    }

    public String getHashedPassword() {
        return this.password.toString();
    }

    public String getConfirmationToken() {
        return confirmation.getToken();
    }

    public String getName() {
        return this.name;
    }
}
