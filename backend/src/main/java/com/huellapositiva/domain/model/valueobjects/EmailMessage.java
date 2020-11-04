package com.huellapositiva.domain.model.valueobjects;

import com.huellapositiva.domain.exception.EmailNotValidException;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmailMessage {
    private final String subject;
    private final String from;
    private final String to;
    private final String body;

    public static EmailMessage createFrom(String from, String to, String subject, EmailTemplate emailTemplate) {
        validateEmail(to);
        return EmailMessage.builder()
                .from(from)
                .to(to)
                .subject(subject)
                .body(emailTemplate.getParsedTemplate())
                .build();
    }

    private static void validateEmail(String emailAddress) {
        if (emailAddress.isEmpty()) {
            throw new EmailNotValidException("Error when build the email, the email address is empty");
        }
    }
}
