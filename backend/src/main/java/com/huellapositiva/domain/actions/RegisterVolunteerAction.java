package com.huellapositiva.domain.actions;

import com.huellapositiva.application.dto.RegisterVolunteerRequestDto;
import com.huellapositiva.domain.service.VolunteerService;
import com.huellapositiva.domain.valueobjects.EmailConfirmation;
import com.huellapositiva.domain.valueobjects.EmailTemplate;
import com.huellapositiva.domain.valueobjects.PlainPassword;
import com.huellapositiva.infrastructure.EmailService;
import com.huellapositiva.domain.Email;
import com.huellapositiva.infrastructure.NoOpEmailService;
import com.huellapositiva.infrastructure.TemplateService;
import com.huellapositiva.infrastructure.orm.service.IssueService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RegisterVolunteerAction {

    @Value("${huellapositiva.api.v1.confirmation-email}")
    private String emailConfirmationBaseUrl;

    private final VolunteerService volunteerService;

    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }

    private IssueService issueService;

    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

    private EmailService emailService;

    private final TemplateService templateService;

    public RegisterVolunteerAction(VolunteerService volunteerService, EmailService emailService, TemplateService templateService, IssueService issueService) {
        this.volunteerService = volunteerService;
        this.emailService = emailService;
        this.templateService = templateService;
        this.issueService = issueService;
    }

    public void execute(RegisterVolunteerRequestDto dto) {
        EmailConfirmation emailConfirmation = EmailConfirmation.from(dto.getEmail(), emailConfirmationBaseUrl);
        volunteerService.registerVolunteer(PlainPassword.from(dto.getPassword()), emailConfirmation);
        try {
            EmailTemplate emailTemplate = templateService.getEmailConfirmationTemplate(emailConfirmation);
            Email email = Email.createFrom(emailConfirmation, emailTemplate);
            emailService.sendEmail(email);
        } catch (RuntimeException ex) {
            // Guarda el email en una base de datos.
            // Guarda la excepción entera con la traza.  --> ex.printStackTrace();
            issueService.registerFailSendEmailConfirmation(emailConfirmation);
            throw ex;
        }
    }
}