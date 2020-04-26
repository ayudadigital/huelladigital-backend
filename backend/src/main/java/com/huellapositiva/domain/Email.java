package com.huellapositiva.domain;

import com.huellapositiva.domain.exception.EmailNotValidException;
import com.huellapositiva.domain.valueobjects.EmailConfirmation;
import com.huellapositiva.domain.valueobjects.EmailTemplate;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Email {
    private String subject;
    private String from;
    private String to;
    private String body;

    public static Email createFrom(EmailConfirmation emailConfirmation, EmailTemplate emailTemplate) {
        if (emailConfirmation.getEmailAddress().isEmpty()) {
            throw new EmailNotValidException("Error when build the email, the email address is empty");
        }

        return Email.builder()
                .from("noreply@huellapositiva.com")
                .to(emailConfirmation.getEmailAddress())
                .subject("Confirmación de la cuenta en huellapositiva")
                .body(emailTemplate.getParsedTemplate())
                .build();

    }

}
