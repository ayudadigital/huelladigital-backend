package com.huellapositiva.integration;

import com.huellapositiva.domain.actions.EmailConfirmationAction;
import com.huellapositiva.infrastructure.orm.repository.JpaEmailConfirmationRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaCredentialRepository;
import com.huellapositiva.infrastructure.orm.model.Credential;
import com.huellapositiva.infrastructure.orm.model.EmailConfirmation;
import com.huellapositiva.util.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(TestData.class)
class EmailAddressConfirmationActionShould {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private TestData testData;

    @Autowired
    private JpaEmailConfirmationRepository jpaEmailConfirmationRepository;

    @Autowired
    private JpaCredentialRepository credentialRepository;

    @BeforeEach
    void beforeEach() {
        testData.resetData();
    }

    @Test
    @Disabled
    void confirm_email() {
        // GIVEN
        String email = "foo@huellapositiva.com";
        UUID hash = UUID.randomUUID();
        Credential credential = testData.createCredential(email, hash);

        // WHEN
        EmailConfirmationAction action = new EmailConfirmationAction(jpaEmailConfirmationRepository, credentialRepository);
        action.execute(hash);

        // THEN
        Integer emailConfirmationId = credential.getEmailConfirmation().getId();
        EmailConfirmation emailConfirmation = jpaEmailConfirmationRepository.findById(emailConfirmationId).get();
        assertThat(emailConfirmation.getEmail(), is(email));
        assertThat(emailConfirmation.getHash(), is(hash.toString()));
        credential = emailConfirmation.getCredential();
        assertThat(credential.getEmailConfirmed(), is(true));
    }

}
