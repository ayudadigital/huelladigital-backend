package com.huellapositiva.domain.actions;

import com.huellapositiva.application.dto.RegisterVolunteerRequestDto;
import com.huellapositiva.domain.service.EmailCommunicationService;
import com.huellapositiva.domain.service.VolunteerService;
import com.huellapositiva.domain.valueobjects.EmailConfirmation;
import com.huellapositiva.domain.valueobjects.PlainPassword;
import com.huellapositiva.infrastructure.EmailService;
import com.huellapositiva.infrastructure.TemplateService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RegisterVolunteerAction {
    private final VolunteerService volunteerService;
    private EmailService emailService;
    private TemplateService templateService;
    private EmailCommunicationService communicationService;

    @Value("${huellapositiva.api.v1.confirmation-email}")
    private String emailConfirmationBaseUrl;

    public RegisterVolunteerAction(VolunteerService volunteerService, EmailService emailService, TemplateService templateService) {
        this.volunteerService = volunteerService;
        this.emailService = emailService;
        this.templateService = templateService;
    }

    public RegisterVolunteerAction(VolunteerService volunteerService, EmailCommunicationService emailCommunicationService) {
        this.volunteerService = volunteerService;
        this.communicationService = emailCommunicationService;
    }

    public void execute(RegisterVolunteerRequestDto dto) {
        EmailConfirmation emailConfirmation = EmailConfirmation.from(dto.getEmail(), emailConfirmationBaseUrl);
        volunteerService.registerVolunteer(PlainPassword.from(dto.getPassword()), emailConfirmation);
        new EmailCommunicationService(emailService, templateService)
                .sendRegistrationConfirmationEmail(emailConfirmation);
    }

}
