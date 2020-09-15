package com.huellapositiva.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huellapositiva.application.dto.AuthenticationRequestDto;
import com.huellapositiva.application.dto.JwtResponseDto;
import com.huellapositiva.domain.model.valueobjects.Roles;
import com.huellapositiva.infrastructure.orm.repository.JpaVolunteerRepository;
import com.huellapositiva.infrastructure.security.JwtService;
import com.huellapositiva.util.TestData;
import com.huellapositiva.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Stream;

import static com.huellapositiva.util.TestData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"aws.paramstore.enabled=false"})
@AutoConfigureMockMvc
@Import(TestData.class)
class VolunteerControllerShould {

    private static final String SIGN_UP_URL = "/api/v1/volunteers";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private TestData testData;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JpaVolunteerRepository jpaVolunteerRepository;

    @BeforeEach
    void beforeEach() {
        testData.resetData();
    }

    @Test
    void registering_volunteer_should_return_201_and_tokens() throws Exception {
        // GIVEN
        AuthenticationRequestDto dto = AuthenticationRequestDto.builder()
                .email(DEFAULT_EMAIL)
                .password("password")
                .build();

        // THEN
        String body = objectMapper.writeValueAsString(dto);
        MockHttpServletResponse response = mvc.perform(post(SIGN_UP_URL)
                .content(body)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, matchesPattern("\\S+(/api/v1/volunteers/)" + UUID_REGEX)))
                .andReturn()
                .getResponse();

        // WHEN
        String jsonResponse = response.getContentAsString();
        JwtResponseDto responseDto = objectMapper.readValue(jsonResponse, JwtResponseDto.class);
        Pair<String, List<String>> userDetails = jwtService.getUserDetails(responseDto.getAccessToken());
        assertThat(userDetails.getFirst()).isEqualTo(dto.getEmail());
        assertThat(userDetails.getSecond()).hasSize(1);
        assertThat(userDetails.getSecond().get(0)).isEqualTo(Roles.VOLUNTEER_NOT_CONFIRMED.toString());
        String location = response.getHeader(HttpHeaders.LOCATION);
        String id = location.substring(location.lastIndexOf('/') + 1);
        assertThat(jpaVolunteerRepository.findByIdWithCredentialsAndRoles(id).get().getCredential().getEmail()).isEqualTo(DEFAULT_EMAIL);
    }

    @Test
    void registering_volunteer_without_password_should_return_400() throws Exception {
        AuthenticationRequestDto dto = AuthenticationRequestDto.builder()
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
        AuthenticationRequestDto dto = AuthenticationRequestDto.builder()
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
        AuthenticationRequestDto dto = AuthenticationRequestDto.builder()
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
        AuthenticationRequestDto dto = AuthenticationRequestDto.builder()
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
    void upload_curriculum_vitae_successfully_and_return_200() throws Exception {
        testData.createVolunteer(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        JwtResponseDto jwtResponseDto = TestUtils.loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);
        InputStream is = getClass().getClassLoader().getResourceAsStream("documents/pdf-test.pdf");
        mvc.perform(multipart("/api/v1/volunteers/cv-upload")
                .file(new MockMultipartFile("cv", "pdf-test.pdf", "application/pdf", is))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .contentType(MULTIPART_FORM_DATA)
                .with(csrf())
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void return_400_when_uploaded_file_is_not_PDF() throws Exception {
        testData.createVolunteer(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        JwtResponseDto jwtResponseDto = TestUtils.loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);
        InputStream is = getClass().getClassLoader().getResourceAsStream("images/huellapositiva-logo.png");
        mvc.perform(multipart("/api/v1/volunteers/cv-upload")
                .file(new MockMultipartFile("cv", "huellapositiva-logo.png", "application/pdf", is))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .contentType(MULTIPART_FORM_DATA)
                .with(csrf())
                .accept(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}






