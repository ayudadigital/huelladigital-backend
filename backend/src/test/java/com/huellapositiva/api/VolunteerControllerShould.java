package com.huellapositiva.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huellapositiva.application.dto.CredentialsVolunteerRequestDto;
import com.huellapositiva.application.dto.JwtResponseDto;
import com.huellapositiva.application.exception.PasswordNotAllowed;
import com.huellapositiva.domain.Roles;
import com.huellapositiva.domain.actions.RegisterVolunteerAction;
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
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.doThrow;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(TestData.class)
class VolunteerControllerShould {

    private static final String SIGN_UP_URL = "/api/v1/volunteers/register";
    private static final String loginUri = "/api/v1/volunteers/login";
    private static final String testJwtUri = "/api/v1/test-jwt-authorization";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String DEFAULT_EMAIL = "foo@huellapositiva.com";
    private static final String DEFAULT_PASSWORD = "plain-password";

    @Autowired
    private TestData testData;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private RegisterVolunteerAction registerVolunteerAction;

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
        mvc.perform(post(SIGN_UP_URL)
                .content(body)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    void registering_volunteer_without_password_should_return_400() throws Exception {
        CredentialsVolunteerRequestDto dto = CredentialsVolunteerRequestDto.builder()
                .email(DEFAULT_EMAIL)
                .build();

        String body = objectMapper.writeValueAsString(dto);
        mvc.perform(post(SIGN_UP_URL)
                .content(body)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
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
        mvc.perform(post(SIGN_UP_URL)
                .content(body)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registering_volunteer_without_email_should_return_400() throws Exception {
        CredentialsVolunteerRequestDto dto = CredentialsVolunteerRequestDto.builder()
                .password("password")
                .build();

        String body = objectMapper.writeValueAsString(dto);
        mvc.perform(post(SIGN_UP_URL)
                .content(body)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registering_volunteer_null_should_return_400() throws Exception {
        mvc.perform(post(SIGN_UP_URL)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @MethodSource("provideMalformedEmails")
    void registering_volunteer_with_malformed_email_should_return_400(String malformedEmail) throws Exception {
        CredentialsVolunteerRequestDto dto = CredentialsVolunteerRequestDto.builder()
                .email(malformedEmail)
                .password("password")
                .build();

        String body = objectMapper.writeValueAsString(dto);
        mvc.perform(post(SIGN_UP_URL)
                .content(body)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
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
    void validate_user_correctly_and_send_tokens() throws Exception {
        //GIVEN
        testData.createVolunteer(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        CredentialsVolunteerRequestDto dto = new CredentialsVolunteerRequestDto(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        String regexToken = "^[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.?[A-Za-z0-9-_.+/=]*$";

        //WHEN
        JwtResponseDto responseDto = login(dto);

        //THEN
        assertThat(responseDto.getAccessToken()).matches(regexToken);
        assertThat(responseDto.getRefreshToken()).matches(regexToken);
    }

    @Test
    void invalid_user_should_return_401() throws Exception {
        //GIVEN
        testData.createVolunteer(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        CredentialsVolunteerRequestDto dto = new CredentialsVolunteerRequestDto(DEFAULT_EMAIL, "invalidPassword");
        String body = objectMapper.writeValueAsString(dto);

        //WHEN + THEN
        mvc.perform(post(loginUri)
                .content(body)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }


    @Test
    void grant_access_when_token_contains_valid_role() throws Exception {
        //GIVEN
        testData.createVolunteer(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        CredentialsVolunteerRequestDto dto = new CredentialsVolunteerRequestDto(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        JwtResponseDto response = login(dto);
        String accessToken = response.getAccessToken();

        //WHEN
        mvc.perform(get(testJwtUri)
                .header(AUTHORIZATION, "Bearer " + accessToken)
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void deny_access_when_token_contains_invalid_role() throws Exception {
        //GIVEN
        testData.createVolunteer(DEFAULT_EMAIL, DEFAULT_PASSWORD, Roles.ADMIN);
        CredentialsVolunteerRequestDto dto = new CredentialsVolunteerRequestDto(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        JwtResponseDto response = login(dto);
        String accessToken = response.getAccessToken();

        //WHEN + THEN
        mvc.perform(get(testJwtUri)
                .header(AUTHORIZATION, "Bearer " + accessToken)
                .accept(APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void deny_access_when_do_not_provide_any_authorization() throws Exception {
        //WHEN + THEN
        mvc.perform(get(testJwtUri)
                .accept(APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void return_401_when_token_has_expired() throws Exception {
        //GIVEN
        testData.createVolunteer(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        CredentialsVolunteerRequestDto dto = new CredentialsVolunteerRequestDto(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        JwtResponseDto response = login(dto);
        String accessToken = response.getAccessToken();
        Thread.sleep(2000);

        //WHEN + THEN
        mvc.perform(get(testJwtUri)
                .header(AUTHORIZATION, "Bearer " + accessToken)
                .accept(APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void generate_new_access_token() throws Exception {
        //GIVEN
        testData.createVolunteer(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        CredentialsVolunteerRequestDto dto = new CredentialsVolunteerRequestDto(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        JwtResponseDto response = login(dto);
        String accessToken = response.getAccessToken();
        String refreshToken = response.getRefreshToken();
        Thread.sleep(1100);

        //WHEN
        String jsonResponse = mvc.perform(post("/api/v1/refresh")
                .contentType(APPLICATION_JSON)
                .content(refreshToken)
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        response = objectMapper.readValue(jsonResponse, JwtResponseDto.class);
        String newAccessToken = response.getAccessToken();
        String newRefreshToken = response.getRefreshToken();

        //THEN
        assertAll(
                () -> assertThat(newAccessToken).isNotEqualTo(accessToken),
                () -> assertThat(newRefreshToken).isNotEqualTo(refreshToken)
        );
    }

    @Test
    void fail_to_generate_new_access_token_if_refresh_token_is_malformed() throws Exception {
        //WHEN
        mvc.perform(post("/api/v1/refresh")
                .contentType(APPLICATION_JSON)
                .content("malformed JWT string")
                .accept(APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    private JwtResponseDto login(CredentialsVolunteerRequestDto loginDto) throws Exception {
        String jsonBody = objectMapper.writeValueAsString(loginDto);
        String jsonResponse = mvc.perform(post(loginUri)
                .content(jsonBody)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse().getContentAsString();
        return objectMapper.readValue(jsonResponse, JwtResponseDto.class);
    }
}






