package com.huellapositiva.domain.actions;

import com.huellapositiva.application.exception.EmailConfirmationAlreadyConfirmed;
import com.huellapositiva.domain.service.EmailCommunicationService;
import com.huellapositiva.domain.valueobjects.EmailConfirmation;
import com.huellapositiva.domain.valueobjects.Token;
import com.huellapositiva.infrastructure.orm.model.Credential;
import com.huellapositiva.infrastructure.orm.repository.JpaCredentialRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaEmailConfirmationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ResendEmailConfirmationAction {

    private final JpaEmailConfirmationRepository jpaEmailConfirmationRepository;

    private final JpaCredentialRepository jpaCredentialRepository;

    private final EmailCommunicationService communicationService;

    @Value("${huellapositiva.api.v1.confirmation-email}")
    private String emailConfirmationBaseUrl;

    public ResendEmailConfirmationAction(JpaEmailConfirmationRepository jpaEmailConfirmationRepository, JpaCredentialRepository jpaCredentialRepository, EmailCommunicationService communicationService) {
        this.jpaEmailConfirmationRepository = jpaEmailConfirmationRepository;
        this.jpaCredentialRepository = jpaCredentialRepository;
        this.communicationService = communicationService;
    }

    public void execute() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = userDetails.getUsername();
        Credential credential = jpaCredentialRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User with username: " + email + " was not found."));

        boolean isEmailConfirmed = credential.getEmailConfirmed();
        if (isEmailConfirmed) {
            throw new EmailConfirmationAlreadyConfirmed("Email is already confirmed");
        }

        Integer updateOperation = jpaEmailConfirmationRepository.updateHashByEmail(email, Token.createToken().toString());
        if (updateOperation != 1) {

            throw new RuntimeException("No modifying anything hash or you have modified several hashes");
        }
        EmailConfirmation emailConfirmationValueObject = EmailConfirmation.from(email, emailConfirmationBaseUrl);
        communicationService.sendRegistrationConfirmationEmail(emailConfirmationValueObject);
    }
}