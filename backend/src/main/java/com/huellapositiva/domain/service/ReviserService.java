package com.huellapositiva.domain.service;

import com.huellapositiva.domain.exception.RoleNotFoundException;
import com.huellapositiva.domain.model.valueobjects.Id;
import com.huellapositiva.domain.model.valueobjects.PasswordHash;
import com.huellapositiva.infrastructure.orm.entities.JpaCredential;
import com.huellapositiva.infrastructure.orm.entities.JpaEmailConfirmation;
import com.huellapositiva.infrastructure.orm.entities.JpaReviser;
import com.huellapositiva.infrastructure.orm.entities.Role;
import com.huellapositiva.infrastructure.orm.repository.JpaCredentialRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaEmailConfirmationRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaReviserRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.UUID;

import static com.huellapositiva.domain.model.valueobjects.Roles.REVISER;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviserService {

    @Autowired
    private JpaCredentialRepository jpaCredentialRepository;

    @Autowired
    private JpaRoleRepository jpaRoleRepository;

    @Autowired
    private JpaEmailConfirmationRepository jpaEmailConfirmationRepository;

    @Autowired
    private JpaReviserRepository jpaReviserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${huellapositiva.web-admin.password}")
    private String reviserPassword;

    @Value("${huellapositiva.web-admin.email}")
    private String reviserEmail;

    @Value("admin")
    private String reviserName;

    @Value("admin")
    private String reviserSurname;

    /**
     * This methods creates a default reviser based on the credentials taken from the application.properties
     */
    @Transactional
    public void createDefaultReviser() {
        if (jpaCredentialRepository.findByEmail(reviserEmail).isEmpty()) {
            Role role = jpaRoleRepository.findByName(REVISER.toString())
                    .orElseThrow(() -> new RoleNotFoundException("Role REVISER not found."));
            JpaEmailConfirmation emailConfirmation = JpaEmailConfirmation.builder()
                    .email(reviserEmail)
                    .hash(UUID.randomUUID().toString())
                    .build();
            emailConfirmation = jpaEmailConfirmationRepository.save(emailConfirmation);
            JpaCredential jpaCredential = JpaCredential.builder()
                    .id(Id.newId().getValue())
                    .email(reviserEmail)
                    .hashedPassword(new PasswordHash(passwordEncoder.encode(reviserPassword)).toString())
                    .roles(Collections.singleton(role))
                    .emailConfirmed(true)
                    .emailConfirmation(emailConfirmation)
                    .build();
            JpaReviser jpaReviser = JpaReviser.builder()
                    .id(Id.newId().getValue())
                    .credential(jpaCredential)
                    .name(reviserName)
                    .surname(reviserSurname)
                    .build();
            jpaReviserRepository.save(jpaReviser);
        }
    }
}
