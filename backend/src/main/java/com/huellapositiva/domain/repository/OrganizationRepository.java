package com.huellapositiva.domain.repository;

import com.huellapositiva.domain.ExpressRegistrationOrganization;
import com.huellapositiva.infrastructure.orm.model.Credential;
import com.huellapositiva.infrastructure.orm.model.EmailConfirmation;
import com.huellapositiva.infrastructure.orm.model.Organization;
import com.huellapositiva.infrastructure.orm.model.Role;
import com.huellapositiva.infrastructure.orm.repository.JpaEmailConfirmationRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaOrganizationRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaRoleRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

import static com.huellapositiva.domain.Roles.ORGANIZATION_NOT_CONFIRMED;

@Component
@Transactional
@AllArgsConstructor
public class OrganizationRepository {

    @Autowired
    private final JpaOrganizationRepository jpaOrganizationRepository;

    @Autowired
    private final JpaEmailConfirmationRepository jpaEmailConfirmationRepository;

    @Autowired
    private final JpaRoleRepository jpaRoleRepository;

    public Integer save(ExpressRegistrationOrganization expressOrganization) {
        Role role = jpaRoleRepository.findByName(ORGANIZATION_NOT_CONFIRMED.toString())
                .orElseThrow(() -> new RuntimeException("Role ORGANIZATION_NOT_CONFIRMED not found."));
        EmailConfirmation emailConfirmation = EmailConfirmation.builder()
                .email(expressOrganization.getEmail())
                .hash(expressOrganization.getConfirmationToken())
                .build();
        emailConfirmation = jpaEmailConfirmationRepository.save(emailConfirmation);
        Credential credential = Credential.builder()
                .email(expressOrganization.getEmail())
                .hashedPassword(expressOrganization.getHashedPassword())
                .roles(Collections.singleton(role))
                .emailConfirmed(false)
                .emailConfirmation(emailConfirmation)
                .build();
        Organization organization = Organization.builder()
                .name(expressOrganization.getName())
                .credential(credential)
                .build();
        return jpaOrganizationRepository.save(organization).getId();
    }
}
