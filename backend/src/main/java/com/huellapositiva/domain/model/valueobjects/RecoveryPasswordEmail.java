package com.huellapositiva.domain.model.valueobjects;

public class RecoveryPasswordEmail {

    private final EmailAddress email;
    private final String hash;

    public RecoveryPasswordEmail(EmailAddress email, String hash){
        this.email = email;
        this.hash = hash;
    }

    public static RecoveryPasswordEmail from(String email, String hash) {
        return new RecoveryPasswordEmail(
                EmailAddress.from(email), hash);
    }

    public String getEmail() {
        return email.toString();
    }

    public String getHash() {
        return hash;
    }

}
