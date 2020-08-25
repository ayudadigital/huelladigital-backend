package com.huellapositiva.domain.model.valueobjects;

public class ExpressRegistrationVolunteer {

    private final EmailConfirmation confirmation;
    private final PasswordHash password;

    public ExpressRegistrationVolunteer(PasswordHash password, EmailConfirmation confirmation) {
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
