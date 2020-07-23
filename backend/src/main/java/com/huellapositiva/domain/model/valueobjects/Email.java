package com.huellapositiva.domain.model.valueobjects;

import com.huellapositiva.domain.exception.EmailNotValidException;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Email {
    private final String subject;
    private final String from;
    private final String to;
    private final String body;

    public static Email createFrom(EmailConfirmation emailConfirmation, EmailTemplate emailTemplate, String from) {
        if (emailConfirmation.getEmailAddress().isEmpty()) {
            throw new EmailNotValidException("Error when build the email, the email address is empty");
        }
        return Email.builder()
                .from(from)
                .to(emailConfirmation.getEmailAddress())
                .subject("Confirmación de la cuenta en huellapositiva")
                .body(emailTemplate.getParsedTemplate())
                .build();
    }
}
