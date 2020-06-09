package com.huellapositiva.domain.actions;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.huellapositiva.domain.service.EmailCommunicationService;
import com.huellapositiva.domain.valueobjects.EmailConfirmation;
import com.huellapositiva.domain.valueobjects.Token;
import com.huellapositiva.infrastructure.orm.model.Credential;
import com.huellapositiva.infrastructure.orm.repository.JpaCredentialRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaEmailConfirmationRepository;
import com.huellapositiva.infrastructure.security.JwtProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import static com.huellapositiva.infrastructure.security.SecurityConstants.ACCESS_TOKEN_PREFIX;

@Service
public class ResendEmailConfirmationAction {

    private final JpaEmailConfirmationRepository jpaEmailConfirmationRepository;

    private final JpaCredentialRepository jpaCredentialRepository;

    private final EmailCommunicationService communicationService;

    private JwtProperties jwtProperties;

    private Token token;

    @Value("${huellapositiva.api.v1.confirmation-email}")
    private String emailConfirmationBaseUrl;

    public ResendEmailConfirmationAction(JpaEmailConfirmationRepository jpaEmailConfirmationRepository, JpaCredentialRepository jpaCredentialRepository, EmailCommunicationService communicationService, JwtProperties jwtProperties) {
        this.jpaEmailConfirmationRepository = jpaEmailConfirmationRepository;
        this.jpaCredentialRepository = jpaCredentialRepository;
        this.communicationService = communicationService;
        this.jwtProperties = jwtProperties;
    }

    public void execute(String token) {
        String email = JWT.require(Algorithm.HMAC512(jwtProperties.getAccessToken().getSecret().getBytes()))
                .build()
                .verify(token.replace(ACCESS_TOKEN_PREFIX, ""))
                .getSubject();

        Credential credential = jpaCredentialRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User with username: " + email + " was not found."));

        if (!credential.getEmailConfirmed()) {
            Integer updateOperation = jpaEmailConfirmationRepository.updateHashByEmail(email, Token.createToken().toString());
            if (updateOperation != 1) {
                throw new RuntimeException("No modifying anything hash or you have modified several hashes");
            }

            EmailConfirmation emailConfirmationValueObject = EmailConfirmation.from(email, emailConfirmationBaseUrl);
            communicationService.sendRegistrationConfirmationEmail(emailConfirmationValueObject);
        }
    }
}
