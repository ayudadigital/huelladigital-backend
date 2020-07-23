package com.huellapositiva.domain.model.valueobjects;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString
public class EmailConfirmation {
    private final String emailConfirmationBaseUrl;
    private final EmailAddress emailAddress;
    private final Token token;

    private EmailConfirmation(EmailAddress emailAddress, Token token, String emailConfirmationBaseUrl) {
        this.emailAddress = emailAddress;
        this.token = token;
        this.emailConfirmationBaseUrl = emailConfirmationBaseUrl;
    }

    public static EmailConfirmation from(String email, String emailConfirmationBaseUrl) {
        return new EmailConfirmation(
                EmailAddress.from(email), Token.createToken(), emailConfirmationBaseUrl);
    }

    public String getEmailAddress(){
        return emailAddress.toString();
    }

    public String getToken() {
        return token.toString();
    }

    public String getUrl() {
        return emailConfirmationBaseUrl + getToken();
    }
}
