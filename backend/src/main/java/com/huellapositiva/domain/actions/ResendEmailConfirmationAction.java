package com.huellapositiva.domain.actions;

import com.huellapositiva.application.exception.EmailConfirmationAlreadyConfirmedException;
import com.huellapositiva.domain.model.valueobjects.EmailConfirmation;
import com.huellapositiva.domain.model.valueobjects.Token;
import com.huellapositiva.domain.service.EmailCommunicationService;
import com.huellapositiva.infrastructure.orm.entities.JpaCredential;
import com.huellapositiva.infrastructure.orm.repository.JpaCredentialRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaEmailConfirmationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
     * @param accountId Account ID of the logged user
     */
    public void execute(String accountId) {
        JpaCredential jpaCredential = jpaCredentialRepository.findByAccountId(accountId)
                .orElseThrow(() -> new UsernameNotFoundException("User with account ID: " + accountId + " was not found."));

        if (Boolean.TRUE.equals(jpaCredential.getEmailConfirmed())) {
            throw new EmailConfirmationAlreadyConfirmedException("Email is already confirmed");
        }

        String email = jpaCredential.getEmail();
        jpaEmailConfirmationRepository.updateHashByEmail(email, Token.createToken().toString());
        EmailConfirmation emailConfirmationValueObject = EmailConfirmation.from(email, emailConfirmationBaseUrl);
        communicationService.sendRegistrationConfirmationEmail(emailConfirmationValueObject);
    }
}