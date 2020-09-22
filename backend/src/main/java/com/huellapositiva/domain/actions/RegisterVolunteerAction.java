package com.huellapositiva.domain.actions;

import com.huellapositiva.application.dto.AuthenticationRequestDto;
import com.huellapositiva.domain.model.entities.Volunteer;
import com.huellapositiva.domain.service.EmailCommunicationService;
import com.huellapositiva.domain.service.VolunteerService;
import com.huellapositiva.domain.model.valueobjects.EmailConfirmation;
import com.huellapositiva.domain.model.valueobjects.PlainPassword;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RegisterVolunteerAction {

    private final VolunteerService volunteerService;

    private final EmailCommunicationService communicationService;

    @Value("${huellapositiva.api.v1.confirmation-email}")
    private String emailConfirmationBaseUrl;

    /**
     * This method registers a volunteer and sends a registration confirmation email
     *
     * @param dto info with the email and password of the volunteer
     * @return volunteer entity
     */
    public Volunteer execute(AuthenticationRequestDto dto) {
        EmailConfirmation emailConfirmation = EmailConfirmation.from(dto.getEmail(), emailConfirmationBaseUrl);
        Volunteer volunteer = volunteerService.registerVolunteer(PlainPassword.from(dto.getPassword()), emailConfirmation);
        communicationService.sendRegistrationConfirmationEmail(emailConfirmation);
        return volunteer;
    }
}
