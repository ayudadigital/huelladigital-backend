package com.huellapositiva.domain.model.valueobjects;

public class EmailRecoveryPassword {

    private EmailAddress email;
    private String hash;

    public EmailRecoveryPassword (EmailAddress email, String hash){
        this.email = email;
        this.hash = hash;
    }

    public static EmailRecoveryPassword from(String email, String hash) {
        return new EmailRecoveryPassword(
                EmailAddress.from(email), hash);
    }

    public String getEmail() {
        return email.toString();
    }

    public String getHash() {
        return hash;
    }

}
