package com.huellapositiva.domain.valueobjects;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class EmailConfirmation {
    private final EmailAddress emailAddress;
    private final Token token;

    private EmailConfirmation(EmailAddress emailAddress, Token token) {
        this.emailAddress = emailAddress;
        this.token = token;
    }

    public static EmailConfirmation from(String email) {
        return new EmailConfirmation(
                EmailAddress.from(email), Token.createToken());
    }

    public String getEmailAddress(){
        return emailAddress.toString();
    }

    public String getToken() {
        return token.toString();
    }
}
