package com.huellapositiva.domain.actions;

import com.huellapositiva.domain.Roles;
import com.huellapositiva.infrastructure.orm.model.Credential;
import com.huellapositiva.infrastructure.orm.model.EmailConfirmation;
import com.huellapositiva.application.exception.EmailConfirmationHashNotFound;
import com.huellapositiva.infrastructure.orm.model.Role;
import com.huellapositiva.infrastructure.orm.repository.JpaEmailConfirmationRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaCredentialRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaRoleRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@AllArgsConstructor
@Transactional
public class EmailConfirmationAction {

    private final JpaEmailConfirmationRepository jpaEmailConfirmationRepository;

    private final JpaCredentialRepository credentialRepository;

    private final JpaRoleRepository jpaRoleRepository;

    public void execute(UUID hash) {
        EmailConfirmation emailConfirmation = jpaEmailConfirmationRepository.findByHash(hash.toString())
                .orElseThrow(() -> new EmailConfirmationHashNotFound("Hash " + hash + " not found"));
        Credential credential = emailConfirmation.getCredential();
        credential.setEmailConfirmed(true);
        Optional<Role> newRole = jpaRoleRepository.findByName("VOLUNTEER");
        Set<Role> newUserRole = new HashSet<>();
        newUserRole.add(newRole.get());
        credential.setRoles(newUserRole);
        credentialRepository.save(credential);
    }
}
