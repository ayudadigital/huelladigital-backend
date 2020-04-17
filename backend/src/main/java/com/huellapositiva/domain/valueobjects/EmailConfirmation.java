package com.huellapositiva.domain.valueobjects;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class EmailConfirmation {
    private final Email email;
    private final Token token;

    private EmailConfirmation(Email email, Token token) {
        this.email = email;
        this.token = token;
    }

    public static EmailConfirmation from(String email) {
        return new EmailConfirmation(
                Email.from(email), Token.createToken());
    }

    public String getEmail(){
        return email.toString();
    }

    public String getToken() {
        return token.toString();
    }
}
