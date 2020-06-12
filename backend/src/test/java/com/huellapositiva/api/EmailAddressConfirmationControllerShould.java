package com.huellapositiva.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huellapositiva.domain.Roles;
import com.huellapositiva.util.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static com.huellapositiva.util.TestUtils.withMockUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(TestData.class)
class EmailAddressConfirmationControllerShould {

    private static final String baseUri = "/api/v1/email-confirmation";

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String DEFAULT_EMAIL = "foo@huellapositiva.com";
    private static final String DEFAULT_PASSWORD = "plain-password";
    private static final String loginUri = "/api/v1/volunteers/login";

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
        testData.createCredential("email@huellapositiva.com", token);

        mvc.perform(get(baseUri + '/' + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void confirm_email_with_non_existing_hash_should_return_404() throws Exception {
        mvc.perform(get(baseUri + '/' + "00000000-0000-0000-0000-000000000000")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void resend_email_should_return_204() throws Exception {
        //GIVEN
        testData.createCredential(DEFAULT_EMAIL, UUID.randomUUID());

        // WHEN + THEN
        mvc.perform(post(baseUri + "/resend-email-confirmation")
                .with(user(withMockUser(DEFAULT_EMAIL, Roles.VOLUNTEER)))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}


