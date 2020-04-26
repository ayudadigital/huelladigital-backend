package com.huellapositiva.domain.actions;

import com.huellapositiva.application.dto.RegisterVolunteerRequestDto;
import com.huellapositiva.domain.service.VolunteerService;
import com.huellapositiva.domain.valueobjects.EmailConfirmation;
import com.huellapositiva.domain.valueobjects.EmailTemplate;
import com.huellapositiva.domain.valueobjects.PlainPassword;
import com.huellapositiva.infrastructure.EmailService;
import com.huellapositiva.domain.Email;
import com.huellapositiva.infrastructure.TemplateService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RegisterVolunteerAction {

    @Value("${huellapositiva.api.v1.confirmation-email}")
    private String emailConfirmationBaseUrl;

    private final VolunteerService volunteerService;

    private final EmailService emailService;

    private final TemplateService templateService;

    public RegisterVolunteerAction(VolunteerService volunteerService, EmailService emailService, TemplateService templateService) {
        this.volunteerService = volunteerService;
        this.emailService = emailService;
        this.templateService = templateService;
    }

    public void execute(RegisterVolunteerRequestDto dto) {
        EmailConfirmation emailConfirmation = EmailConfirmation.from(dto.getEmail(), emailConfirmationBaseUrl);
        volunteerService.registerVolunteer(PlainPassword.from(dto.getPassword()), emailConfirmation);
        EmailTemplate emailTemplate = templateService.getEmailConfirmationTemplate(emailConfirmation);
        Email email = Email.createFrom(emailConfirmation, emailTemplate);
        emailService.sendEmail(email);
    }
}