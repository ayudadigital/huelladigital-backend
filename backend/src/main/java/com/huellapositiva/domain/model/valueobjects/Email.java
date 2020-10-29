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
        validateEmail(emailConfirmation.getEmailAddress());
        return Email.builder()
                .from(from)
                .to(emailConfirmation.getEmailAddress())
                .subject("Confirmación de la cuenta en huellapositiva")
                .body(emailTemplate.getParsedTemplate())
                .build();
    }

    public static Email createFrom(ProposalRevisionRequestEmail proposalRevisionRequestEmail, EmailTemplate emailTemplate, String from) {
        validateEmail(proposalRevisionRequestEmail.getEmailAddress());
        return Email.builder()
                .from(from)
                .to(proposalRevisionRequestEmail.getEmailAddress())
                .subject("Revisión de nuevas convocatorias requerida")
                .body(emailTemplate.getParsedTemplate())
                .build();
    }

    public static Email createFrom(ProposalRevisionEmail proposalRevisionEmail, EmailTemplate emailTemplate, String from) {
        validateEmail(proposalRevisionEmail.getEmailAddress());
        return Email.builder()
                .from(from)
                .to(proposalRevisionEmail.getEmailAddress())
                .subject("Revisión de tu convocatoria.")
                .body(emailTemplate.getParsedTemplate())
                .build();
    }
    public static Email createFrom(EmailRecoveryPassword emailRecoveryPassword, EmailTemplate emailTemplate, String from) {
        validateEmail(emailRecoveryPassword.getEmail());
        return Email.builder()
                .from(from)
                .to(emailRecoveryPassword.getEmail())
                .subject("Cambio de tu contraseña")
                .body(emailTemplate.getParsedTemplate())
                .build();
    }

    public static Email createFrom(EmailAddress emailAddress, EmailTemplate emailTemplate, String from) {
        validateEmail(emailAddress.toString());
        return Email.builder()
                .from(from)
                .to(emailAddress.toString())
                .subject("Confirmacion de cambio de contraseña")
                .body(emailTemplate.getParsedTemplate())
                .build();
    }

    private static void validateEmail(String emailAddress) {
        if (emailAddress.isEmpty()) {
            throw new EmailNotValidException("Error when build the email, the email address is empty");
        }
    }
}
