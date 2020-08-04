package com.huellapositiva.domain.actions;


import com.huellapositiva.application.dto.CredentialsESALMemberRequestDto;
import com.huellapositiva.domain.service.EmailCommunicationService;
import com.huellapositiva.domain.service.ESALMemberService;
import com.huellapositiva.domain.model.valueobjects.EmailConfirmation;
import com.huellapositiva.domain.model.valueobjects.PlainPassword;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RegisterESALMemberAction {

    private final ESALMemberService ESALMemberService;

    private final EmailCommunicationService emailCommunicationService;

    @Value("${huellapositiva.api.v1.confirmation-email}")
    private String emailConfirmationBaseUrl;

    public Integer execute(CredentialsESALMemberRequestDto dto){
        EmailConfirmation emailConfirmation = EmailConfirmation.from(dto.getEmail(), emailConfirmationBaseUrl);
        Integer id = ESALMemberService.registerMember(PlainPassword.from(dto.getPassword()), emailConfirmation);
        emailCommunicationService.sendRegistrationConfirmationEmail(emailConfirmation);
        return id;
    }
}
