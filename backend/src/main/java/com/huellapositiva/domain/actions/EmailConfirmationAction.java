package com.huellapositiva.domain.actions;

import com.huellapositiva.application.exception.EmailConfirmationAlreadyConfirmed;
import com.huellapositiva.application.exception.EmailConfirmationExpired;
import com.huellapositiva.application.exception.EmailConfirmationHashNotFound;
import com.huellapositiva.domain.exception.RoleNotFoundException;
import com.huellapositiva.infrastructure.orm.entities.JpaCredential;
import com.huellapositiva.infrastructure.orm.entities.EmailConfirmation;
import com.huellapositiva.infrastructure.orm.entities.Role;
import com.huellapositiva.infrastructure.orm.repository.JpaCredentialRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaEmailConfirmationRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaRoleRepository;
import com.huellapositiva.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.huellapositiva.domain.model.valueobjects.Roles.VOLUNTEER;
import static java.time.Instant.now;

@Service
@RequiredArgsConstructor
@Transactional
public class EmailConfirmationAction {

    private final JpaEmailConfirmationRepository jpaEmailConfirmationRepository;

    private final JpaCredentialRepository credentialRepository;

    private final JpaRoleRepository jpaRoleRepository;

    private final JwtService jwtService;

    @Value("${huellapositiva.email-confirmation.expiration-time}")
    private long emailExpirationTime;

    public void execute(UUID hash) {
        EmailConfirmation emailConfirmation = jpaEmailConfirmationRepository.findByHash(hash.toString())
                .orElseThrow(() -> new EmailConfirmationHashNotFound("Hash " + hash + " not found."));

        boolean isEmailConfirmed = emailConfirmation.getCredential().getEmailConfirmed();
        if (isEmailConfirmed) {
            throw new EmailConfirmationAlreadyConfirmed("Email is already confirmed");
        }

        Instant expirationTimestamp = emailConfirmation.getUpdatedOn().toInstant().plusMillis(emailExpirationTime);
        if(expirationTimestamp.isBefore(now())) {
           throw new EmailConfirmationExpired("Hash " + hash + " has expired on " + expirationTimestamp.toString() + ".");
        }

        JpaCredential jpaCredential = emailConfirmation.getCredential();
        jpaCredential.setEmailConfirmed(true);
        Role newRole = jpaRoleRepository.findByName(VOLUNTEER.toString())
                .orElseThrow(() -> new RoleNotFoundException("Role VOLUNTEER not found."));
        Set<Role> newUserRole = new HashSet<>();
        newUserRole.add(newRole);
        jpaCredential.setRoles(newUserRole);
        credentialRepository.save(jpaCredential);
        jwtService.revokeAccessTokens(jpaCredential.getEmail());
    }
}
