package com.huellapositiva.domain.service;

import com.huellapositiva.domain.exception.RoleNotFoundException;
import com.huellapositiva.domain.model.valueobjects.PasswordHash;
import com.huellapositiva.infrastructure.orm.entities.Credential;
import com.huellapositiva.infrastructure.orm.entities.EmailConfirmation;
import com.huellapositiva.infrastructure.orm.entities.Role;
import com.huellapositiva.infrastructure.orm.repository.JpaCredentialRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaEmailConfirmationRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;

import static com.huellapositiva.domain.model.valueobjects.Roles.ADMIN;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    @Autowired
    private JpaCredentialRepository jpaCredentialRepository;

    @Autowired
    private JpaRoleRepository jpaRoleRepository;

    @Autowired
    private JpaEmailConfirmationRepository jpaEmailConfirmationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${huellapositiva.web-admin.password}")
    private String adminPassword;

    @Value("${huellapositiva.web-admin.email}")
    private String adminEmail;

    public void createDefaultAdmin() {
        if (jpaCredentialRepository.findByEmail(adminEmail).isEmpty()) {
            Role role = jpaRoleRepository.findByName(ADMIN.toString())
                    .orElseThrow(() -> new RoleNotFoundException("Role ADMIN not found."));
            EmailConfirmation emailConfirmation = EmailConfirmation.builder()
                    .email(adminEmail)
                    .hash(UUID.randomUUID().toString())
                    .build();
            emailConfirmation = jpaEmailConfirmationRepository.save(emailConfirmation);
            Credential credential = Credential.builder()
                    .email(adminEmail)
                    .hashedPassword(new PasswordHash(passwordEncoder.encode(adminPassword)).toString())
                    .roles(Collections.singleton(role))
                    .emailConfirmed(true)
                    .emailConfirmation(emailConfirmation)
                    .build();
            jpaCredentialRepository.save(credential);
        }
    }
}
