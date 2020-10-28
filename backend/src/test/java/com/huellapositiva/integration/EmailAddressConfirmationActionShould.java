package com.huellapositiva.integration;

import com.huellapositiva.domain.actions.EmailConfirmationAction;
import com.huellapositiva.infrastructure.orm.entities.JpaCredential;
import com.huellapositiva.infrastructure.orm.entities.Role;
import com.huellapositiva.infrastructure.orm.repository.JpaCredentialRepository;
import com.huellapositiva.infrastructure.security.JwtService;
import com.huellapositiva.util.TestData;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static com.huellapositiva.domain.model.valueobjects.Roles.*;
import static com.huellapositiva.util.TestData.DEFAULT_EMAIL;
import static com.huellapositiva.util.TestData.DEFAULT_PASSWORD;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"aws.paramstore.enabled=false"})
@Import(TestData.class)
class EmailAddressConfirmationActionShould {

    @Autowired
    private TestData testData;

    @MockBean
    private JwtService jwtService;

    @Autowired
    private JpaCredentialRepository credentialRepository;

    @Autowired
    private EmailConfirmationAction action;

    @BeforeEach
    void beforeEach() {
        testData.resetData();
    }

    @Test
    void confirm_email_as_volunteer() {
        // GIVEN
        UUID hash = UUID.randomUUID();
        JpaCredential jpaCredential = testData.createCredential(DEFAULT_EMAIL, hash, DEFAULT_PASSWORD, VOLUNTEER_NOT_CONFIRMED);

        // WHEN
        action.execute(hash);

        // THEN
        Optional<JpaCredential> credentialOptional = credentialRepository.findById(jpaCredential.getId());
        assertTrue(credentialOptional.isPresent());
        jpaCredential = credentialOptional.get();
        assertThat(jpaCredential.getEmailConfirmed(), is(true));
        String username = jpaCredential.getEmail();
        verify(jwtService, times(1)).revokeAccessTokens(username);
        Set<Role> roles = jpaCredential.getRoles();
        Assertions.assertThat(roles).hasSize(1);
        Assertions.assertThat(roles.iterator().next().getName()).isEqualTo(VOLUNTEER.toString());
    }


    @Test
    void confirm_email_as_contact_person() {
        // GIVEN
        UUID hash = UUID.randomUUID();
        JpaCredential jpaCredential = testData.createCredential(DEFAULT_EMAIL, hash, DEFAULT_PASSWORD, CONTACT_PERSON_NOT_CONFIRMED);

        // WHEN
        action.execute(hash);

        // THEN
        Optional<JpaCredential> credentialOptional = credentialRepository.findById(jpaCredential.getId());
        assertTrue(credentialOptional.isPresent());
        jpaCredential = credentialOptional.get();
        assertThat(jpaCredential.getEmailConfirmed(), is(true));
        String username = jpaCredential.getEmail();
        verify(jwtService, times(1)).revokeAccessTokens(username);
        Set<Role> jpaRoles = jpaCredential.getRoles();
        Assertions.assertThat(jpaRoles).hasSize(1);
        Assertions.assertThat(jpaRoles.iterator().next().getName()).isEqualTo(CONTACT_PERSON.toString());
    }
}