package com.huellapositiva.domain;

public class ExpressRegistrationVolunteer {
    private Email email;
    private PasswordHash password;

    public ExpressRegistrationVolunteer(Email email, PasswordHash password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return this.email.toString();
    }

    public String getHashedPassword() {
        return this.password.toString();
    }
}
