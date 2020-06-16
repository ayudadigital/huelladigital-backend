package com.huellapositiva.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huellapositiva.application.dto.CredentialsVolunteerRequestDto;
import com.huellapositiva.application.dto.JwtResponseDto;
import com.huellapositiva.domain.Roles;
import com.huellapositiva.infrastructure.security.JwtService;
import com.huellapositiva.util.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.util.Pair;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.stream.Stream;

import static com.huellapositiva.util.TestData.DEFAULT_EMAIL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(TestData.class)
class VolunteerControllerShould {

    private static final String SIGN_UP_URL = "/api/v1/volunteers/register";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private TestData testData;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JwtService jwtService;

    @BeforeEach
    void beforeEach() {
        testData.resetData();
    }

    @Test
    void registering_volunteer_should_return_201_and_tokens() throws Exception {
        String regexToken = "^[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.?[A-Za-z0-9-_.+/=]*$";
        CredentialsVolunteerRequestDto dto = CredentialsVolunteerRequestDto.builder()
                .email(DEFAULT_EMAIL)
                .password("password")
                .build();

        String body = objectMapper.writeValueAsString(dto);
        String jsonResponse = mvc.perform(post(SIGN_UP_URL)
                .content(body)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse().getContentAsString();

        JwtResponseDto responseDto = objectMapper.readValue(jsonResponse, JwtResponseDto.class);
        Pair<String, List<String>> userDetails = jwtService.getUserDetails(responseDto.getAccessToken());
        assertThat(userDetails.getFirst()).isEqualTo(dto.getEmail());
        assertThat(userDetails.getSecond()).hasSize(1);
        assertThat(userDetails.getSecond().get(0)).isEqualTo(Roles.VOLUNTEER_NOT_CONFIRMED.toString());
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
}






