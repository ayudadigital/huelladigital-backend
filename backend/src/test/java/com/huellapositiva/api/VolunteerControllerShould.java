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
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Stream;

import static com.huellapositiva.infrastructure.security.SecurityConstants.ACCESS_TOKEN_PREFIX;
import static com.huellapositiva.infrastructure.security.SecurityConstants.REFRESH_TOKEN_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(TestData.class)
class VolunteerControllerShould {

    private static final String registerUri = "/api/v1/volunteers/register";
    private static final String loginUri = "/api/v1/volunteers/login";

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
        mvc.perform(post(registerUri)
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
        mvc.perform(post(registerUri)
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
        mvc.perform(post(registerUri)
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
        mvc.perform(post(registerUri)
                .content(body)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registering_volunteer_null_should_return_400() throws Exception {
        mvc.perform(post(registerUri)
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
        mvc.perform(post(registerUri)
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
        mvc.perform(post(registerUri)
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
        mvc.perform(post(registerUri)
                .content(body)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        //THEN
        verify(issueService).registerVolunteerIssue(any(), any());
    }

    @Test
    void validate_user_correctly_and_send_tokens() throws Exception {
        //GIVEN
        Volunteer volunteer = testData.createVolunteer(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        CredentialsVolunteerRequestDto dto = new CredentialsVolunteerRequestDto(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        String body = objectMapper.writeValueAsString(dto);
        String regexToken = "^[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.?[A-Za-z0-9-_.+/=]*$";

        //WHEN
        MockHttpServletResponse response = mvc.perform(post(loginUri)
                .content(body)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        String accessToken = response.getHeader("Authorization");
        String refreshToken = response.getHeader("Refresh");

        //THEN
        assertThat(accessToken.replace(ACCESS_TOKEN_PREFIX, "")).matches(regexToken);
        assertThat(refreshToken.replace(REFRESH_TOKEN_PREFIX, "")).matches(regexToken);
    }

    @Test
    void invalid_user_should_return_401() throws Exception {
        //GIVEN
        Volunteer volunteer = testData.createVolunteer(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        CredentialsVolunteerRequestDto dto = new CredentialsVolunteerRequestDto(DEFAULT_EMAIL, "invalidPassword");
        String body = objectMapper.writeValueAsString(dto);

        //WHEN
        mvc.perform(post(loginUri)
                .content(body)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        //THEN

    }


    @Test
    void grant_access_when_token_contains_valid_role() throws Exception {
        //GIVEN
        Volunteer volunteer = testData.createVolunteer(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        CredentialsVolunteerRequestDto dto = new CredentialsVolunteerRequestDto(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        String body = objectMapper.writeValueAsString(dto);
        String authorization = mvc.perform(post(loginUri)
                .content(body)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getHeader("Authorization");

        //WHEN
        String response = mvc.perform(get("/api/v1/test")
                .header("Authorization", authorization)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        //THEN
        assertThat(response).isEqualTo("OK");

    }

    @Test
    void deny_access_when_token_contains_invalid_role() throws Exception {
        //GIVEN
        Volunteer volunteer = testData.createFakeRoleVolunteer(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        CredentialsVolunteerRequestDto dto = new CredentialsVolunteerRequestDto(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        String body = objectMapper.writeValueAsString(dto);
        String authorization = mvc.perform(post(loginUri)
                .content(body)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getHeader("Authorization");

        //WHEN + THEN
        mvc.perform(get("/api/v1/test")
                .header("Authorization", authorization)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void return_401_when_token_has_expired() throws Exception {
        //GIVEN
        Volunteer volunteer = testData.createVolunteer(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        CredentialsVolunteerRequestDto dto = new CredentialsVolunteerRequestDto(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        String body = objectMapper.writeValueAsString(dto);
        String authorization = mvc.perform(post(loginUri)
                .content(body)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getHeader("Authorization");
        Thread.sleep(6000);

        //WHEN + THEN
        mvc.perform(get("/api/v1/test")
                .header("Authorization", authorization)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

    }

    @Test
    void generate_new_access_token() throws Exception {
        //GIVEN
        Volunteer volunteer = testData.createVolunteer(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        CredentialsVolunteerRequestDto dto = new CredentialsVolunteerRequestDto(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        String body = objectMapper.writeValueAsString(dto);

        MockHttpServletResponse response = mvc.perform(post(loginUri)
                .content(body)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        String accessToken = response.getHeader("Authorization");
        String refreshToken = response.getHeader("Refresh");
        Thread.sleep(6000);

        //WHEN
        response = mvc.perform(get("/api/v1/test")
                .header("Authorization", accessToken)
                .header("Refresh", refreshToken)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();
        String newAccessToken = response.getHeader("Authorization");

        //THEN
        assertThat(newAccessToken)
                .isNotEqualTo(accessToken)
                .isNotNull();
    }
}






