package com.huellapositiva.domain.actions;

import com.huellapositiva.application.dto.RegisterVolunteerRequestDto;
import com.huellapositiva.domain.service.VolunteerService;
import com.huellapositiva.domain.valueobjects.EmailConfirmation;
import com.huellapositiva.domain.valueobjects.EmailTemplate;
import com.huellapositiva.domain.valueobjects.PlainPassword;
import com.huellapositiva.infrastructure.EmailService;
import com.huellapositiva.domain.Email;
import com.huellapositiva.infrastructure.TemplateService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RegisterVolunteerAction {

    private final VolunteerService volunteerService;

    private final EmailService emailService;

    private final TemplateService templateService;

    public void execute(RegisterVolunteerRequestDto dto) {
        EmailConfirmation emailConfirmation = EmailConfirmation.from(dto.getEmail());
        volunteerService.registerVolunteer(PlainPassword.from(dto.getPassword()), emailConfirmation);
        EmailTemplate emailTemplate = templateService.getEmailConfirmationTemplate();
        emailTemplate = emailTemplate.parse(emailConfirmation);
        Email email = Email.createFrom(emailConfirmation, emailTemplate);
        emailService.sendEmail(email);
    }
}