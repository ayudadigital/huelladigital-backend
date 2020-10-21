package com.huellapositiva.api;

import com.huellapositiva.application.dto.JwtResponseDto;
import com.huellapositiva.util.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static com.huellapositiva.util.TestData.DEFAULT_EMAIL;
import static com.huellapositiva.util.TestData.DEFAULT_PASSWORD;
import static com.huellapositiva.util.TestUtils.loginAndGetJwtTokens;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(TestData.class)
class RecoveryPasswordApiControllerShould {
    private static final String baseUri = "/api/v1/restore-password";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private TestData testData;

    @BeforeEach
    void beforeEach() {
        testData.resetData();
    }

    @Test
    void return_204_when_sends_a_recovery_password_email() throws Exception{
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
}