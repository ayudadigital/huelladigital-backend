package com.huellapositiva.domain.service;

import com.huellapositiva.domain.Email;
import com.huellapositiva.domain.exception.EmailException;
import com.huellapositiva.domain.valueobjects.EmailConfirmation;
import com.huellapositiva.domain.valueobjects.EmailTemplate;
import com.huellapositiva.infrastructure.EmailService;
import com.huellapositiva.infrastructure.TemplateService;
import com.huellapositiva.infrastructure.orm.service.IssueService;
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

    @Autowired
    private IssueService issueService;

    public EmailCommunicationService(EmailService emailService, TemplateService templateService) {
        this.emailService = emailService;
        this.templateService = templateService;
    }

    public EmailCommunicationService(EmailService emailService, TemplateService templateService, IssueService issueService) {
        this.emailService = emailService;
        this.templateService = templateService;
        this.issueService = issueService;
    }

    public void sendRegistrationConfirmationEmail(EmailConfirmation emailConfirmation) {
        try {
            EmailTemplate emailTemplate = templateService.getEmailConfirmationTemplate(emailConfirmation);
            Email email = Email.createFrom(emailConfirmation, emailTemplate, from);
            emailService.sendEmail(email);
        } catch (Exception ex) {
            //issueService.registerVolunteerIssue(emailConfirmation.getEmailAddress(), ex);
            throw new EmailException(ex);
        }
    }
}
