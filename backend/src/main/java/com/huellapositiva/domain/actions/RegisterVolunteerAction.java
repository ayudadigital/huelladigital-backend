package com.huellapositiva.domain.actions;

import com.huellapositiva.application.dto.CredentialsVolunteerRequestDto;
import com.huellapositiva.application.dto.JwtResponseDto;
import com.huellapositiva.domain.service.EmailCommunicationService;
import com.huellapositiva.domain.service.VolunteerService;
import com.huellapositiva.domain.valueobjects.EmailConfirmation;
import com.huellapositiva.domain.valueobjects.PlainPassword;
import com.huellapositiva.infrastructure.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RegisterVolunteerAction {

    private final VolunteerService volunteerService;

    private final EmailCommunicationService communicationService;

    private final JwtService jwtService;
    @Value("${huellapositiva.api.v1.confirmation-email}")
    private String emailConfirmationBaseUrl;

    public RegisterVolunteerAction(VolunteerService volunteerService, EmailCommunicationService communicationService, JwtService jwtService) {
        this.volunteerService = volunteerService;
        this.communicationService = communicationService;
        this.jwtService = jwtService;
    }

    public void execute(CredentialsVolunteerRequestDto dto) {
        EmailConfirmation emailConfirmation = EmailConfirmation.from(dto.getEmail(), emailConfirmationBaseUrl);
        volunteerService.registerVolunteer(PlainPassword.from(dto.getPassword()), emailConfirmation);
        communicationService.sendRegistrationConfirmationEmail(emailConfirmation);
    }

    public JwtResponseDto authenticate(CredentialsVolunteerRequestDto dto) {
        return jwtService.create(dto.getEmail(), volunteerService.getVolunteerRoles(dto.getEmail()));
    }

}
