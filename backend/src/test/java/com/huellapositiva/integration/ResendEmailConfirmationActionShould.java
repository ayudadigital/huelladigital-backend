package com.huellapositiva.integration;

import com.huellapositiva.application.exception.EmailConfirmationAlreadyConfirmedException;
import com.huellapositiva.domain.actions.EmailConfirmationAction;
import com.huellapositiva.domain.actions.ResendEmailConfirmationAction;
import com.huellapositiva.domain.service.EmailCommunicationService;
import com.huellapositiva.infrastructure.orm.entities.JpaCredential;
import com.huellapositiva.infrastructure.orm.entities.JpaEmailConfirmation;
import com.huellapositiva.infrastructure.orm.repository.JpaEmailConfirmationRepository;
import com.huellapositiva.util.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.Instant;
import java.util.UUID;

import static com.huellapositiva.util.TestData.DEFAULT_EMAIL;
import static com.huellapositiva.util.TestData.DEFAULT_PASSWORD;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestData.class)
class ResendEmailConfirmationActionShould {

    @Autowired
    private TestData testData;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private EmailConfirmationAction emailConfirmationAction;

    @Autowired
    private ResendEmailConfirmationAction resendEmailConfirmationAction;

    @Autowired
    private JpaEmailConfirmationRepository jpaEmailConfirmationRepository;

    @MockBean
    private EmailCommunicationService communicationService;

    @BeforeEach
    void beforeEach() {
        testData.resetData();
    }

    @Test
    void update_hash_and_update_timestamp_and_resend_email() {
        // GIVEN
        UUID initialHash = UUID.randomUUID();
        JpaCredential jpaCredential = testData.createCredential(DEFAULT_EMAIL, DEFAULT_PASSWORD, initialHash);
        Instant updateTimestamp = jpaCredential.getEmailConfirmation().getUpdatedOn().toInstant();

        // WHEN
        resendEmailConfirmationAction.execute(jpaCredential.getId());

        // THEN
        JpaEmailConfirmation newEmailConfirmation = jpaEmailConfirmationRepository.findByEmail(DEFAULT_EMAIL)
                .orElseThrow(() -> new UsernameNotFoundException("User with username: " + DEFAULT_EMAIL + " was not found."));
        String newHash = newEmailConfirmation.getHash();
        Instant lastUpdateTimestamp = newEmailConfirmation.getUpdatedOn().toInstant();
        assertAll(
                () -> assertThat(initialHash, is(not(newHash))),
                () -> assertThat(updateTimestamp, is(not(lastUpdateTimestamp)))
        );
        verify(communicationService).sendRegistrationConfirmationEmail(any());
    }

    @Test
    void verify_email_is_not_confirmed_yet() {
        // GIVEN
        UUID hash = UUID.randomUUID();
        JpaCredential jpaCredential = testData.createCredential(DEFAULT_EMAIL, DEFAULT_PASSWORD, hash);
        emailConfirmationAction.execute(hash);

        // WHEN + THEN
        String id = jpaCredential.getId();
        assertThrows(EmailConfirmationAlreadyConfirmedException.class,
                () -> resendEmailConfirmationAction.execute(id));
    }
}
