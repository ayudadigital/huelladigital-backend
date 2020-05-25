package com.huellapositiva.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huellapositiva.application.dto.CredentialsVolunteerRequestDto;
import com.huellapositiva.application.exception.PasswordNotAllowed;
import com.huellapositiva.domain.actions.RegisterVolunteerAction;
import com.huellapositiva.domain.exception.EmailException;
import com.huellapositiva.infrastructure.orm.model.Volunteer;
import com.huellapositiva.infrastructure.orm.repository.JpaCredentialRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaFailEmailConfirmationRepository;
import com.huellapositiva.infrastructure.orm.service.IssueService;
import com.huellapositiva.util.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(TestData.class)
class VolunteerControllerShould {

    private static final String baseUri = "/api/v1/volunteers";

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String DEFAULT_EMAIL = "foo@huellapositiva.com";
    private static final String DEFAULT_PASSWORD = "plain-password";

    @Autowired
    JpaFailEmailConfirmationRepository failEmailConfirmationRepository;


    @Autowired
    private TestData testData;

    @Autowired
    private MockMvc mvc;

    @MockBean
    IssueService issueService;

    @MockBean
    private RegisterVolunteerAction registerVolunteerAction;

    @Autowired
    private JpaCredentialRepository credentialRepository;

    @BeforeEach
    void beforeEach() {
        testData.resetData();
    }

    @Test
    void registering_volunteer_should_return_201() throws Exception {
        CredentialsVolunteerRequestDto dto = CredentialsVolunteerRequestDto.builder()
                .email(DEFAULT_EMAIL)
                .password("password")
                .build();

        String body = objectMapper.writeValueAsString(dto);
        mvc.perform(post(baseUri)
                .content(body)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    void registering_volunteer_without_password_should_return_400() throws Exception {
        CredentialsVolunteerRequestDto dto = CredentialsVolunteerRequestDto.builder()
                .email(DEFAULT_EMAIL)
                .build();

        String body = objectMapper.writeValueAsString(dto);
        mvc.perform(post(baseUri)
                .content(body)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registering_volunteer_with_short_password_should_return_400() throws Exception {
        String shortPassword = "12345";
        CredentialsVolunteerRequestDto dto = CredentialsVolunteerRequestDto.builder()
                .email(DEFAULT_EMAIL)
                .password(shortPassword)
                .build();

        doThrow(new PasswordNotAllowed("to short")).when(registerVolunteerAction).execute(dto);
        String body = objectMapper.writeValueAsString(dto);
        mvc.perform(post(baseUri)
                .content(body)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registering_volunteer_without_email_should_return_400() throws Exception {
        CredentialsVolunteerRequestDto dto = CredentialsVolunteerRequestDto.builder()
                .password("password")
                .build();

        String body = objectMapper.writeValueAsString(dto);
        mvc.perform(post(baseUri)
                .content(body)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registering_volunteer_null_should_return_400() throws Exception {
        mvc.perform(post(baseUri)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registering_volunteer_fail_sending_email_confirmation_should_return_500() throws Exception {
        CredentialsVolunteerRequestDto dto = CredentialsVolunteerRequestDto.builder()
                .email(DEFAULT_EMAIL)
                .password("1234567")
                .build();
        doThrow(new EmailException()).when(registerVolunteerAction).execute(dto);


        String body = objectMapper.writeValueAsString(dto);
        mvc.perform(post(baseUri)
                .content(body)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }


    @ParameterizedTest
    @MethodSource("provideMalformedEmails")
    void registering_volunteer_with_malformed_email_should_return_400(String malformedEmail) throws Exception {
        CredentialsVolunteerRequestDto dto = CredentialsVolunteerRequestDto.builder()
                .email(malformedEmail)
                .password("password")
                .build();

        String body = objectMapper.writeValueAsString(dto);
        mvc.perform(post(baseUri)
                .content(body)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    private static Stream<String> provideMalformedEmails() {
        return Stream.of(
                ".username@yahoo.com",
                "username@yahoo.com.",
                "username@yahoo..com",
                "username@yahoo.c",
                "username@yahoo.corporate"
        );
    }

    @Test
    void fail_on_registering_a_volunteer_should_save_a_email_and_stacktrace() throws Exception {
        //GIVEN
        CredentialsVolunteerRequestDto dto = new CredentialsVolunteerRequestDto(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        doThrow(new EmailException()).when(registerVolunteerAction).execute(dto);

        //WHEN
        String body = objectMapper.writeValueAsString(dto);
        mvc.perform(post(baseUri)
                .content(body)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        //THEN
        verify(issueService).registerVolunteerIssue(any(), any());
    }

    @Test
    void validate_user_correctly_and_send_token() throws Exception {

        //GIVEN
        Volunteer volunteer = testData.createVolunteer(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        CredentialsVolunteerRequestDto dto = new CredentialsVolunteerRequestDto(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        String body = objectMapper.writeValueAsString(dto);

        //WHEN
        String jsonResponse = mvc.perform(post(baseUri + "/login")
                .content(body)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        //THEN
        System.out.println("Printline: " + jsonResponse);
        assertThat(jsonResponse).isNotEmpty();
    }
}






