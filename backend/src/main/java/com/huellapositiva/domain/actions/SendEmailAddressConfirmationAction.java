package com.huellapositiva.domain.actions;


import com.huellapositiva.application.dto.EmailAddressConfirmationRequestDto;
import com.huellapositiva.application.exception.EmailAlreadyValidatedException;
import com.huellapositiva.application.exception.UserNotFoundException;
import com.huellapositiva.domain.Email;
import com.huellapositiva.domain.exception.EmailException;
import com.huellapositiva.domain.valueobjects.EmailConfirmation;
import com.huellapositiva.domain.valueobjects.EmailTemplate;
import com.huellapositiva.infrastructure.EmailService;
import com.huellapositiva.infrastructure.TemplateService;
import com.huellapositiva.infrastructure.orm.model.Credential;
import com.huellapositiva.infrastructure.orm.repository.JpaCredentialRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SendEmailAddressConfirmationAction {
    private final EmailService emailService;

    private final TemplateService templateService;

    private final JpaCredentialRepository jpaCredentialRepository;

    @Value("${huellapositiva.api.v1.confirmation-email}")
    private String emailConfirmationBaseUrl;

    @Value("${huellapositiva.feature.email.from}")
    private String from;

    public SendEmailAddressConfirmationAction(EmailService emailService, TemplateService templateService, JpaCredentialRepository jpaCredentialRepository) {
        this.emailService = emailService;
        this.templateService = templateService;
        this.jpaCredentialRepository = jpaCredentialRepository;
    }

    public void execute(EmailAddressConfirmationRequestDto dto) {
        Credential credential = jpaCredentialRepository.findByEmail(dto.getEmailAddress())
                .orElseThrow(() -> new UserNotFoundException("Email address not found"));

        if (Boolean.TRUE.equals(credential.getEmailConfirmed())) {
            throw new EmailAlreadyValidatedException("Email already validated");
        }

        EmailConfirmation emailConfirmation = EmailConfirmation.from(dto.getEmailAddress(), emailConfirmationBaseUrl);
        try {
            EmailTemplate emailTemplate = templateService.getEmailConfirmationTemplate(emailConfirmation);
            Email email = Email.createFrom(emailConfirmation, emailTemplate, from);
            emailService.sendEmail(email);
        } catch (Exception ex) {
            throw new EmailException(ex);
        }
    }
}
