package com.huellapositiva.api;

import com.huellapositiva.util.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static com.huellapositiva.domain.Roles.VOLUNTEER_NOT_CONFIRMED;
import static com.huellapositiva.util.TestData.DEFAULT_EMAIL;
import static com.huellapositiva.util.TestData.DEFAULT_PASSWORD;
import static com.huellapositiva.util.TestUtils.withMockUser;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(TestData.class)
class EmailAddressConfirmationControllerShould {

    private static final String baseUri = "/api/v1/email-confirmation";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private TestData testData;

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

        // WHEN + THEN
        mvc.perform(post(baseUri + "/resend-email-confirmation")
                .with(user(withMockUser(DEFAULT_EMAIL, VOLUNTEER_NOT_CONFIRMED)))
                .with(csrf())
                .contentType(APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void expired_email_should_return_410() {
        // GIVEN
        UUID token = UUID.randomUUID();
        testData.createCredential("email@huellapositiva.com", token, "password", VOLUNTEER_NOT_CONFIRMED);

        // WHEN + THEN
        await().atMost(1, SECONDS).untilAsserted(() ->
                mvc.perform(get(baseUri + '/' + token)
                        .contentType(APPLICATION_JSON))
                        .andExpect(status().isGone())
        );
    }
}


