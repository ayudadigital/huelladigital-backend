package com.huellapositiva.domain.actions;


import com.huellapositiva.application.dto.CredentialsESALMemberRequestDto;
import com.huellapositiva.domain.model.valueobjects.Id;
import com.huellapositiva.domain.service.EmailCommunicationService;
import com.huellapositiva.domain.service.ESALContactPersonService;
import com.huellapositiva.domain.model.valueobjects.EmailConfirmation;
import com.huellapositiva.domain.model.valueobjects.PlainPassword;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RegisterESALContactPersonAction {

    private final ESALContactPersonService esalContactPersonService;

    private final EmailCommunicationService emailCommunicationService;

    @Value("${huellapositiva.api.v1.confirmation-email}")
    private String emailConfirmationBaseUrl;

    /**
     * This method registers a new ContactPerson in the system and sends registration confirmation email.
     *
     * @param dto contains email and password of the ContactPerson
     * @return id of the ContactPerson created
     */
    public Id execute(CredentialsESALMemberRequestDto dto){
        EmailConfirmation emailConfirmation = EmailConfirmation.from(dto.getEmail(), emailConfirmationBaseUrl);
        Id id = esalContactPersonService.registerContactPerson(PlainPassword.from(dto.getPassword()), emailConfirmation);
        emailCommunicationService.sendRegistrationConfirmationEmail(emailConfirmation);
        return id;
    }
}
