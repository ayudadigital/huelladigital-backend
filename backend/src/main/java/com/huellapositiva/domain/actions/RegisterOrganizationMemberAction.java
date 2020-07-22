package com.huellapositiva.domain.actions;


import com.huellapositiva.application.dto.CredentialsOrganizationMemberRequestDto;
import com.huellapositiva.domain.service.EmailCommunicationService;
import com.huellapositiva.domain.service.OrganizationMemberService;
import com.huellapositiva.domain.valueobjects.EmailConfirmation;
import com.huellapositiva.domain.valueobjects.PlainPassword;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RegisterOrganizationMemberAction {

    private final OrganizationMemberService organizationMemberService;

    private final EmailCommunicationService emailCommunicationService;

    @Value("${huellapositiva.api.v1.confirmation-email}")
    private String emailConfirmationBaseUrl;

    public void execute(CredentialsOrganizationMemberRequestDto dto){
        EmailConfirmation emailConfirmation = EmailConfirmation.from(dto.getEmail(), emailConfirmationBaseUrl);
        organizationMemberService.registerMember(PlainPassword.from(dto.getPassword()), emailConfirmation);
        emailCommunicationService.sendRegistrationConfirmationEmail(emailConfirmation);
    }
}
