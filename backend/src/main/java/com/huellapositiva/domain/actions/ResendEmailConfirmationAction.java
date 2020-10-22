package com.huellapositiva.domain.actions;

import com.huellapositiva.application.exception.EmailConfirmationAlreadyConfirmedException;
import com.huellapositiva.domain.service.EmailCommunicationService;
import com.huellapositiva.domain.model.valueobjects.EmailConfirmation;
import com.huellapositiva.domain.model.valueobjects.Token;
import com.huellapositiva.infrastructure.orm.entities.JpaCredential;
import com.huellapositiva.infrastructure.orm.repository.JpaCredentialRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaEmailConfirmationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ResendEmailConfirmationAction {

    private final JpaEmailConfirmationRepository jpaEmailConfirmationRepository;

    private final JpaCredentialRepository jpaCredentialRepository;

    private final EmailCommunicationService communicationService;

    @Value("${huellapositiva.api.v1.confirmation-email}")
    private String emailConfirmationBaseUrl;


    /**
     * This method fetches the user credentials from the DB and checks if that email is confirmed.
     * In case it is not confirmed, creates a new hash and resends the confirmation email.
     *
     * @throws UsernameNotFoundException
     * @throws EmailConfirmationAlreadyConfirmedException
     */
    public void execute() {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        JpaCredential jpaCredential = jpaCredentialRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User with username: " + email + " was not found."));
        boolean isEmailConfirmed = jpaCredential.getEmailConfirmed();
        if (isEmailConfirmed) {
            throw new EmailConfirmationAlreadyConfirmedException("Email is already confirmed");
        }
        jpaEmailConfirmationRepository.updateHashByEmail(email, Token.createToken().toString());
        EmailConfirmation emailConfirmationValueObject = EmailConfirmation.from(email, emailConfirmationBaseUrl);
        communicationService.sendRegistrationConfirmationEmail(emailConfirmationValueObject);
    }
}