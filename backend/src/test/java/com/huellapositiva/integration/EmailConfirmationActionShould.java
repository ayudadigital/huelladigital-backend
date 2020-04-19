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
class EmailConfirmationActionShould {

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

    // Confirmar un email
    // El email ya está confirmado / ¿Deberíamos borrar el token si ya se confirmó?
    // El hash existe
    // El hash NO existe
    // Esta asociado a usuario
    // El hash existe pero no está asociado a usuario
    // El hash ya está consumido

    // Estadísticas de usuarios que no consiguieron validar su email


    @Test
    @Disabled
    void confirm_email() {
        // GIVEN
        String email = "foo@huellapositiva.com";
        UUID hash = UUID.randomUUID();
        testData.createCredential(email, hash);

        // WHEN
        EmailConfirmationAction action = new EmailConfirmationAction(jpaEmailConfirmationRepository, credentialRepository);
        action.execute(hash);

        // THEN
        EmailConfirmation emailConfirmation = jpaEmailConfirmationRepository.findById(1).get();
        assertThat(emailConfirmation.getEmail(), is(email));
        assertThat(emailConfirmation.getHash(), is(hash.toString()));
        Credential credential = emailConfirmation.getCredential();
        assertThat(credential.getEmailConfirmed(), is(true));
    }

}
