package com.huellapositiva.util;

import com.huellapositiva.domain.Roles;
import com.huellapositiva.infrastructure.orm.model.Credential;
import com.huellapositiva.infrastructure.orm.model.EmailConfirmation;
import com.huellapositiva.infrastructure.orm.model.Role;
import com.huellapositiva.infrastructure.orm.model.Volunteer;
import com.huellapositiva.infrastructure.orm.repository.*;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Date;
import java.util.UUID;

@AllArgsConstructor
@TestComponent
@Transactional
public class TestData {

    public static final String DEFAULT_FROM = "noreply@huellapositiva.com";

    public static final String DEFAULT_EMAIL = "foo@huellapositiva.com";

    public static final String DEFAULT_PASSWORD = "plainPassword";

    @Autowired
    private final JpaVolunteerRepository volunteerRepository;

    @Autowired
    private final JpaCredentialRepository jpaCredentialRepository;

    @Autowired
    private final JpaEmailConfirmationRepository jpaEmailConfirmationRepository;

    @Autowired
    private final JpaFailEmailConfirmationRepository failEmailConfirmationRepository;

    @Autowired
    private final JpaRoleRepository roleRepository;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    public void resetData() {
        volunteerRepository.deleteAll();
        jpaCredentialRepository.deleteAll();
        jpaCredentialRepository.deleteAll();
        jpaEmailConfirmationRepository.deleteAll();
        failEmailConfirmationRepository.deleteAll();
    }

    private EmailConfirmation createEmailConfirmation(UUID token) {
        EmailConfirmation emailConfirmation = EmailConfirmation.builder()
                .email(DEFAULT_EMAIL)
                .createdOn(new Date())
                .hash(token.toString())
                .build();
        return jpaEmailConfirmationRepository.save(emailConfirmation);
    }

    public Credential createCredential(String email, UUID token) {
        return createCredential(email, DEFAULT_PASSWORD, token);
    }

    public Credential createCredential(String email, Roles role) {
        return createCredential(email, UUID.randomUUID(), DEFAULT_PASSWORD, role);
    }

    public Credential createCredential(String email, String plainPassword, UUID token) {
        return createCredential(email, token, plainPassword, Roles.VOLUNTEER_NOT_CONFIRMED);
    }

    public Credential createCredential(String email, UUID token, String plainPassword, Roles userRole){
        EmailConfirmation emailConfirmation = createEmailConfirmation(token);
        Role role = roleRepository.findByName(userRole.toString()).orElse(null);
        Credential credential = Credential.builder()
                .email(email)
                .hashedPassword(passwordEncoder.encode(plainPassword))
                .emailConfirmed(false)
                .emailConfirmation(emailConfirmation)
                .roles(Collections.singleton(role))
                .build();

        return jpaCredentialRepository.save(credential);
    }

    public Volunteer createVolunteer(String email, String password) {
        return createVolunteer(email, password, Roles.VOLUNTEER);
    }

    public Volunteer createVolunteer(String email, String password, Roles role) {
        Credential credential = createCredential(email, UUID.randomUUID(), password, role);

        Volunteer volunteer = Volunteer.builder().credential(credential).build();

        return volunteerRepository.save(volunteer);
    }
}
