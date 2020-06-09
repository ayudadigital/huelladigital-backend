package com.huellapositiva.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huellapositiva.application.dto.CredentialsVolunteerRequestDto;
import com.huellapositiva.domain.actions.ResendEmailConfirmationAction;
import com.huellapositiva.domain.service.EmailCommunicationService;
import com.huellapositiva.infrastructure.orm.model.EmailConfirmation;
import com.huellapositiva.infrastructure.orm.repository.JpaEmailConfirmationRepository;
import com.huellapositiva.util.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
@Import(TestData.class)
public class ResendEmailConfirmationActionShould {
    @Autowired
    private TestData testData;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String DEFAULT_EMAIL = "foo@huellapositiva.com";
    private static final String DEFAULT_PASSWORD = "plain-password";
    private static final String loginUri = "/api/v1/volunteers/login";

    @Autowired
    private ResendEmailConfirmationAction resendEmailConfirmationAction;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JpaEmailConfirmationRepository jpaEmailConfirmationRepository;

    @MockBean
    private EmailCommunicationService communicationService;

    @BeforeEach
    void beforeEach() {
        testData.resetData();
    }

    @Test
    void update_hash_and_resend_email() throws Exception {
        //GIVEN
        testData.createVolunteer(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        CredentialsVolunteerRequestDto dto = new CredentialsVolunteerRequestDto(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        String body = objectMapper.writeValueAsString(dto);
        String tokenJWT = mvc.perform(post(loginUri)
                .content(body)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getHeader(HttpHeaders.AUTHORIZATION);

        EmailConfirmation emailConfirmation = jpaEmailConfirmationRepository.findByEmail(DEFAULT_EMAIL)
                .orElseThrow(() -> new UsernameNotFoundException("User with username: " + DEFAULT_EMAIL + " was not found."));
        String initialHash = emailConfirmation.getHash();

        //WHEN
        resendEmailConfirmationAction.execute(tokenJWT);

        //THEN
        EmailConfirmation newEmailConfirmation = jpaEmailConfirmationRepository.findByEmail(DEFAULT_EMAIL)
                .orElseThrow(() -> new UsernameNotFoundException("User with username: " + DEFAULT_EMAIL + " was not found."));
        String newHash = newEmailConfirmation.getHash();
        assertThat(initialHash, is(not(newHash)));

        verify(communicationService).sendRegistrationConfirmationEmail(any());

    }
}
