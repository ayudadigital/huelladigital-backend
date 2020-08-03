package com.huellapositiva.domain;

import com.huellapositiva.domain.valueobjects.EmailConfirmation;
import com.huellapositiva.domain.valueobjects.PasswordHash;

public class ExpressRegistrationOrganizationMember {

    private final EmailConfirmation confirmation;
    private final PasswordHash password;

    public ExpressRegistrationOrganizationMember(PasswordHash password, EmailConfirmation confirmation) {
        this.confirmation = confirmation;
        this.password = password;
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
}
