package com.huellapositiva.domain.actions;

import com.huellapositiva.application.dto.CredentialsVolunteerRequestDto;
import com.huellapositiva.application.exception.UserAlreadyExists;
import com.huellapositiva.domain.service.EmailCommunicationService;
import com.huellapositiva.domain.service.VolunteerService;
import com.huellapositiva.domain.valueobjects.EmailConfirmation;
import com.huellapositiva.domain.valueobjects.PlainPassword;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RegisterVolunteerAction {

    private final VolunteerService volunteerService;

    private final EmailCommunicationService communicationService;

    @Value("${huellapositiva.api.v1.confirmation-email}")
    private String emailConfirmationBaseUrl;

    public void execute(CredentialsVolunteerRequestDto dto) {
        EmailConfirmation emailConfirmation = EmailConfirmation.from(dto.getEmail(), emailConfirmationBaseUrl);
        try {
            volunteerService.registerVolunteer(PlainPassword.from(dto.getPassword()), emailConfirmation);
        } catch (DataIntegrityViolationException ex) {
            throw new UserAlreadyExists();
        }
        communicationService.sendRegistrationConfirmationEmail(emailConfirmation);
    }
}
