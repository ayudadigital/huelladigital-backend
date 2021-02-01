package com.huellapositiva.domain.actions;

import com.huellapositiva.application.exception.EmailConfirmationAlreadyConfirmedException;
import com.huellapositiva.application.exception.EmailConfirmationExpiredException;
import com.huellapositiva.application.exception.EmailConfirmationHashNotFoundException;
import com.huellapositiva.domain.exception.RoleNotFoundException;
import com.huellapositiva.infrastructure.orm.entities.JpaCredential;
import com.huellapositiva.infrastructure.orm.entities.JpaEmailConfirmation;
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

import static com.huellapositiva.domain.model.valueobjects.Roles.*;
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

    /**
     * This method confirms if the given hash is valid and in that case it gives the user the role of VOLUNTEER.
     * When finished all previous access tokens are revoked.
     *
     * @param hash this parameter is given in a request path variable. Its value is stored in DB
     * @throws EmailConfirmationHashNotFoundException hash not found in the DB
     * @throws EmailConfirmationAlreadyConfirmedException email already confirmed
     * @throws EmailConfirmationExpiredException hash has expired
     */
    public void execute(UUID hash) {
        JpaEmailConfirmation emailConfirmation = jpaEmailConfirmationRepository.findByHash(hash.toString())
                .orElseThrow(() -> new EmailConfirmationHashNotFoundException("Hash " + hash + " not found."));

        boolean isEmailConfirmed = emailConfirmation.getCredential().getEmailConfirmed();
        if (isEmailConfirmed) {
            throw new EmailConfirmationAlreadyConfirmedException("Email is already confirmed");
        }

        Instant expirationTimestamp = emailConfirmation.getUpdatedOn().toInstant().plusMillis(emailExpirationTime);
        if(expirationTimestamp.isBefore(now())) {
            throw new EmailConfirmationExpiredException("Hash " + hash + " has expired on " + expirationTimestamp.toString() + ".");
        }

        JpaCredential jpaCredential = emailConfirmation.getCredential();
        jpaCredential.setEmailConfirmed(true);
        boolean isVolunteer = emailConfirmation.getCredential().getRoles()
                .stream()
                .allMatch(role -> role.getName().equals(VOLUNTEER_NOT_CONFIRMED.toString()));
        String roleToBeSet = isVolunteer ? VOLUNTEER.toString() : CONTACT_PERSON.toString();
        Role newJpaRole = jpaRoleRepository.findByName(roleToBeSet)
                .orElseThrow(() -> new RoleNotFoundException("Role " + roleToBeSet + "not found."));
        Set<Role> newUserRoles = new HashSet<>();
        newUserRoles.add(newJpaRole);
        jpaCredential.setRoles(newUserRoles);
        credentialRepository.save(jpaCredential);
        jwtService.revokeAccessTokens(jpaCredential.getId());
    }
}
