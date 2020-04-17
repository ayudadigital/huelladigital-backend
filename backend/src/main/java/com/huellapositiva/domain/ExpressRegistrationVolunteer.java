package com.huellapositiva.domain;

public class ExpressRegistrationVolunteer {
    private Email email;
    private Password password;

    public ExpressRegistrationVolunteer(Email email, Password password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return this.email.toString();
    }

    public String getHashedPassword() {
        return this.password.hash();
    }
}
