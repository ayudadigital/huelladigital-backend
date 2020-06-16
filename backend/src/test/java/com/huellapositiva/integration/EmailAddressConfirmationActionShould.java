package com.huellapositiva.integration;

import com.huellapositiva.domain.actions.EmailConfirmationAction;
import com.huellapositiva.infrastructure.orm.model.Credential;
import com.huellapositiva.infrastructure.orm.model.Role;
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

import static com.huellapositiva.domain.Roles.VOLUNTEER;
import static com.huellapositiva.domain.Roles.VOLUNTEER_NOT_CONFIRMED;
import static com.huellapositiva.util.TestData.DEFAULT_EMAIL;
import static com.huellapositiva.util.TestData.DEFAULT_PASSWORD;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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
    void confirm_email() {
        // GIVEN
        UUID hash = UUID.randomUUID();
        Credential credential = testData.createCredential(DEFAULT_EMAIL, hash, DEFAULT_PASSWORD, VOLUNTEER_NOT_CONFIRMED);

        // WHEN
        action.execute(hash);

        // THEN
        Optional<Credential> credentialOptional = credentialRepository.findById(credential.getId());
        assertTrue(credentialOptional.isPresent());
        credential = credentialOptional.get();
        assertThat(credential.getEmailConfirmed(), is(true));
        String username = credential.getEmail();
        verify(jwtService, times(1)).revokeAccessTokens(username);
        Set<Role> roles = credential.getRoles();
        Assertions.assertThat(roles).hasSize(1);
        Assertions.assertThat(roles.iterator().next().getName()).isEqualTo(VOLUNTEER.toString());
    }
}
