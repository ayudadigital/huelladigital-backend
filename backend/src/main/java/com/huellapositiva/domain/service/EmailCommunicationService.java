package com.huellapositiva.domain.service;

import com.huellapositiva.domain.Email;
import com.huellapositiva.domain.exception.EmailException;
import com.huellapositiva.domain.valueobjects.EmailConfirmation;
import com.huellapositiva.domain.valueobjects.EmailTemplate;
import com.huellapositiva.infrastructure.EmailService;
import com.huellapositiva.infrastructure.TemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailCommunicationService {

    @Value("${huellapositiva.feature.email.from}")
    private String from;

    @Autowired
    private final EmailService emailService;

    @Autowired
    private final TemplateService templateService;

    public EmailCommunicationService(EmailService emailService, TemplateService templateService) {
        this.emailService = emailService;
        this.templateService = templateService;
    }

    public void sendRegistrationConfirmationEmail(EmailConfirmation emailConfirmation) {
        try {
            EmailTemplate emailTemplate = templateService.getEmailConfirmationTemplate(emailConfirmation);
            Email email = Email.createFrom(emailConfirmation, emailTemplate, from);
            emailService.sendEmail(email);
        } catch (Exception ex) {
            throw new EmailException(ex);
        }
    }
}
