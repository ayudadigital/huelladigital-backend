package com.huellapositiva.api;

import com.huellapositiva.application.exception.UserNotFoundException;
import com.huellapositiva.domain.actions.FetchCredentialsAction;
import com.huellapositiva.infrastructure.orm.entities.JpaCredential;
import com.huellapositiva.infrastructure.orm.repository.JpaCredentialRepository;
import com.huellapositiva.util.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Calendar;
import java.util.UUID;

import static com.huellapositiva.util.TestData.DEFAULT_EMAIL;
import static com.huellapositiva.util.TestData.DEFAULT_PASSWORD;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(TestData.class)
class RecoveryPasswordApiControllerShould {
    private static final String baseUri = "/api/v1/restore-password";

    @Autowired
    private MockMvc mvc;

    @Autowired
    FetchCredentialsAction credentialsAction;

    @Autowired
    private JpaCredentialRepository jpaCredentialRepository;

    @Autowired
    private TestData testData;

    @BeforeEach
    void beforeEach() {
        testData.resetData();
    }

    @Test
    void return_204_when_sends_a_recovery_password_email() throws Exception {
        // GIVEN
        testData.createCredential(DEFAULT_EMAIL, DEFAULT_PASSWORD, UUID.randomUUID());

        // WHEN + THEN
        mvc.perform(get(baseUri + "/sendRecoveryPasswordEmail/" + DEFAULT_EMAIL)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void return_404_when_user_email_not_found() throws Exception {
        // WHEN + THEN
        mvc.perform(get(baseUri + "/sendRecoveryPasswordEmail/" + "emailNotFound@huellapositiva.com")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void return_204_when_changing_password_is_successful() throws Exception {
        testData.createCredential(DEFAULT_EMAIL, DEFAULT_PASSWORD, UUID.randomUUID());
        credentialsAction.executeGenerationRecoveryPasswordEmail(DEFAULT_EMAIL);
        JpaCredential jpaCredential = jpaCredentialRepository.findByEmail(DEFAULT_EMAIL).orElseThrow(UserNotFoundException::new);

        // WHEN + THEN
        mvc.perform(post(baseUri + "/changePassword")
                .with(csrf())
                .param("hash", jpaCredential.getHashRecoveryPassword())
                .param("newPassword", "NEWPASSWORD")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void return_423_when_trying_to_change_password_and_time_has_expired() throws Exception {
        testData.createCredential(DEFAULT_EMAIL, DEFAULT_PASSWORD, UUID.randomUUID());
        credentialsAction.executeGenerationRecoveryPasswordEmail(DEFAULT_EMAIL);
        JpaCredential jpaCredential = jpaCredentialRepository.findByEmail(DEFAULT_EMAIL).orElseThrow(UserNotFoundException::new);

        Calendar c = Calendar.getInstance();
        c.setTime(jpaCredential.getCreatedRecoveryHashOn());
        c.add(Calendar.HOUR, -2);  // number of days to add

        jpaCredential.setCreatedRecoveryHashOn(c.getTime());
        jpaCredentialRepository.save(jpaCredential);

        // WHEN + THEN
        mvc.perform(post(baseUri + "/changePassword")
                .with(csrf())
                .param("hash", jpaCredential.getHashRecoveryPassword())
                .param("newPassword", "NEWPASSWORD")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isLocked());
    }
}