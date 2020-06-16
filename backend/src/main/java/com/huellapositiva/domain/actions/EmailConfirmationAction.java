package com.huellapositiva.domain.actions;

import com.huellapositiva.application.exception.EmailConfirmationHashNotFound;
import com.huellapositiva.domain.exception.RoleNotFoundException;
import com.huellapositiva.infrastructure.orm.model.Credential;
import com.huellapositiva.infrastructure.orm.model.EmailConfirmation;
import com.huellapositiva.infrastructure.orm.model.Role;
import com.huellapositiva.infrastructure.orm.repository.JpaCredentialRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaEmailConfirmationRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaRoleRepository;
import com.huellapositiva.infrastructure.security.JwtService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.huellapositiva.domain.Roles.VOLUNTEER;

@Service
@AllArgsConstructor
@Transactional
public class EmailConfirmationAction {

    private final JpaEmailConfirmationRepository jpaEmailConfirmationRepository;

    private final JpaCredentialRepository credentialRepository;

    private final JpaRoleRepository jpaRoleRepository;

    private final JwtService jwtService;

    public void execute(UUID hash) {
        EmailConfirmation emailConfirmation = jpaEmailConfirmationRepository.findByHash(hash.toString())
                .orElseThrow(() -> new EmailConfirmationHashNotFound("Hash " + hash + " not found"));
        Credential credential = emailConfirmation.getCredential();
        credential.setEmailConfirmed(true);
        Role newRole = jpaRoleRepository.findByName(VOLUNTEER.toString())
                .orElseThrow(() -> new RoleNotFoundException("Role VOLUNTEER not found."));
        Set<Role> newUserRole = new HashSet<>();
        newUserRole.add(newRole);
        credential.setRoles(newUserRole);
        credentialRepository.save(credential);
        jwtService.revokeAccessTokens(credential.getEmail());
    }
}
