package com.huellapositiva.integration;

import com.huellapositiva.application.dto.CredentialsOrganizationRequestDto;
import com.huellapositiva.domain.Roles;
import com.huellapositiva.domain.service.OrganizationService;
import com.huellapositiva.domain.valueobjects.EmailConfirmation;
import com.huellapositiva.domain.valueobjects.PlainPassword;
import com.huellapositiva.infrastructure.orm.model.Credential;
import com.huellapositiva.infrastructure.orm.model.Organization;
import com.huellapositiva.infrastructure.orm.repository.JpaOrganizationRepository;
import com.huellapositiva.util.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static com.huellapositiva.util.TestData.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestData.class)
public class OrganizationServiceShould {

    @Autowired
    private TestData testData;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private JpaOrganizationRepository organizationRepository;

    @BeforeEach
    void beforeEach() {
        testData.resetData();
    }

    @Test
    void persist_an_entity_in_db() {
        // GIVEN
        CredentialsOrganizationRequestDto dto = CredentialsOrganizationRequestDto.builder()
                .email(DEFAULT_EMAIL)
                .password(DEFAULT_PASSWORD)
                .name(DEFAULT_ORGANIZATION)
                .build();

        // WHEN
        Integer organizationId = organizationService.registerOrganization(PlainPassword.from(dto.getPassword()), EmailConfirmation.from(dto.getEmail(), ""), dto.getName());

        // THEN
        Optional<Organization> organizationOptional = organizationRepository.findByIdWithCredentialsAndRoles(organizationId);
        assertTrue(organizationOptional.isPresent());
        Organization organization = organizationOptional.get();
        Credential credential = organization.getCredential();
        assertThat(credential.getEmail(), is(DEFAULT_EMAIL));
        assertThat(passwordEncoder.matches(DEFAULT_PASSWORD, credential.getHashedPassword()), is(true));
        assertThat(credential.getRoles(), hasSize(1));
        assertThat(credential.getRoles().iterator().next().getName(), is(Roles.ORGANIZATION_NOT_CONFIRMED.toString()));
        assertThat(organization.getName(), is(DEFAULT_ORGANIZATION));
    }
}
