package com.huellapositiva.api;

import com.huellapositiva.application.dto.JwtResponseDto;
import com.huellapositiva.domain.actions.EmailConfirmationAction;
import com.huellapositiva.infrastructure.orm.entities.JpaCredential;
import com.huellapositiva.util.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static com.huellapositiva.domain.model.valueobjects.Roles.VOLUNTEER_NOT_CONFIRMED;
import static com.huellapositiva.util.TestData.DEFAULT_EMAIL;
import static com.huellapositiva.util.TestData.DEFAULT_PASSWORD;
import static com.huellapositiva.util.TestUtils.loginAndGetJwtTokens;
import static java.time.Instant.now;
import static org.awaitility.Awaitility.await;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(TestData.class)
class EmailAddressConfirmationControllerShould {

    private static final String baseUri = "/api/v1/email-confirmation";

    @Value("${huellapositiva.email-confirmation.expiration-time}")
    private long expirationTime;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private TestData testData;

    @Autowired
    private EmailConfirmationAction emailConfirmationAction;

    @BeforeEach
    void beforeEach() {
        testData.resetData();
    }

    @Test
    void confirm_email_should_return_204() throws Exception {
        UUID token = UUID.randomUUID();
        testData.createCredential("email@huellapositiva.com", token, "password", VOLUNTEER_NOT_CONFIRMED);

        mvc.perform(get(baseUri + '/' + token)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void confirm_email_with_non_existing_hash_should_return_404() throws Exception {
        mvc.perform(get(baseUri + '/' + "00000000-0000-0000-0000-000000000000")
                .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void resend_email_should_return_204() throws Exception {
        // GIVEN
        testData.createCredential(DEFAULT_EMAIL, UUID.randomUUID(), DEFAULT_PASSWORD, VOLUNTEER_NOT_CONFIRMED);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        // WHEN + THEN
        mvc.perform(post(baseUri + "/resend-email-confirmation")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .with(csrf())
                .contentType(APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void resend_email_confirmation_with_a_confirmed_email_should_return_409() throws Exception {
        // GIVEN
        UUID token = UUID.randomUUID();
        testData.createCredential(DEFAULT_EMAIL, token, DEFAULT_PASSWORD, VOLUNTEER_NOT_CONFIRMED);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);
        emailConfirmationAction.execute(token);

        // WHEN + THEN
        mvc.perform(post(baseUri + "/resend-email-confirmation")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .with(csrf())
                .contentType(APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    void confirmed_email_should_return_409() throws Exception {
        // GIVEN
        UUID token = UUID.randomUUID();
        testData.createCredential("email@huellapositiva.com", token, "password", VOLUNTEER_NOT_CONFIRMED);
        emailConfirmationAction.execute(token);

        // WHEN + THEN
        mvc.perform(get(baseUri + '/' + token)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    void expired_email_should_return_410() throws Exception {
        // GIVEN
        UUID token = UUID.randomUUID();
        JpaCredential jpaCredential = testData.createCredential("email@huellapositiva.com", token, "password", VOLUNTEER_NOT_CONFIRMED);
        Instant expirationTimestamp = jpaCredential.getEmailConfirmation().getUpdatedOn().toInstant().plusMillis(expirationTime);
        await().until(() -> expirationTimestamp.isBefore(now()));

        // WHEN + THEN
        mvc.perform(get(baseUri + '/' + token)
                .contentType(APPLICATION_JSON))
                .andExpect(status().isGone());
    }
}