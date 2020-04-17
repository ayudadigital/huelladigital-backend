package com.huellapositiva.domain.actions;

import com.huellapositiva.infrastructure.orm.model.Credential;
import com.huellapositiva.infrastructure.orm.model.EmailConfirmation;
import com.huellapositiva.domain.exception.EmailConfirmationHashNotFound;
import com.huellapositiva.infrastructure.orm.repository.JpaEmailConfirmationRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaCredentialRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@AllArgsConstructor
@Transactional
public class EmailConfirmationAction {

    private final JpaEmailConfirmationRepository jpaEmailConfirmationRepository;

    private final JpaCredentialRepository credentialRepository;

    public void execute(UUID hash) {
        EmailConfirmation emailConfirmation = jpaEmailConfirmationRepository.findByHash(hash.toString())
                .orElseThrow(() -> new EmailConfirmationHashNotFound("Hash " + hash + " not found"));
        Credential credential = emailConfirmation.getCredential();
        credential.setEmailConfirmed(true);
        credentialRepository.save(credential);
    }
}
