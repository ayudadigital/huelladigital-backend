package com.huellapositiva.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huellapositiva.application.dto.RegisterESALMemberRequestDto;
import com.huellapositiva.application.dto.JwtResponseDto;
import com.huellapositiva.application.exception.UserNotFoundException;
import com.huellapositiva.domain.model.entities.ContactPerson;
import com.huellapositiva.domain.model.valueobjects.Roles;
import com.huellapositiva.infrastructure.orm.entities.JpaContactPerson;
import com.huellapositiva.infrastructure.orm.repository.JpaContactPersonProfileRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaContactPersonRepository;
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
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.InputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.huellapositiva.util.TestData.*;
import static com.huellapositiva.util.TestUtils.loginAndGetJwtTokens;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(TestData.class)
class ESALContactPersonControllerShould {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private TestData testData;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JpaContactPersonRepository jpaContactPersonRepository;

    @Autowired
    private JpaContactPersonProfileRepository jpaContactPersonProfileRepository;

    @BeforeEach
    void beforeEach() {
        testData.resetData();
    }

    @Test
    void register_member_and_return_201_and_tokens() throws Exception {
        // GIVEN
        RegisterESALMemberRequestDto dto = new RegisterESALMemberRequestDto(VALID_NAME, VALID_SURNAME, VALID_PHONE, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        // WHEN
        MockHttpServletResponse response = mvc.perform(post("/api/v1/contactperson")
                .content(objectMapper.writeValueAsString(dto))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, matchesPattern("\\S+(/api/v1/contactperson/)" + UUID_REGEX)))
                .andReturn()
                .getResponse();

        // THEN
        String jsonResponse = response.getContentAsString();
        JwtResponseDto responseDto = objectMapper.readValue(jsonResponse, JwtResponseDto.class);
        Pair<String, List<String>> userDetails = jwtService.getUserDetails(responseDto.getAccessToken());
        assertThat(userDetails.getFirst()).isEqualTo(dto.getEmail());
        assertThat(userDetails.getSecond()).hasSize(1);
        assertThat(userDetails.getSecond().get(0)).isEqualTo(Roles.CONTACT_PERSON_NOT_CONFIRMED.toString());
        String location = response.getHeader(HttpHeaders.LOCATION);
        String id = location.substring(location.lastIndexOf('/') + 1);
        assertThat(jpaContactPersonRepository.findByUUID(id).get().getCredential().getEmail()).isEqualTo(DEFAULT_EMAIL);

        JpaContactPerson jpaContactPerson = jpaContactPersonRepository.findByEmail(DEFAULT_EMAIL)
                .orElseThrow(() -> new RuntimeException("The contact person not found with email: " + DEFAULT_EMAIL));
        assertThat(jpaContactPerson.getContactPersonProfile().getName()).isEqualTo(VALID_NAME);
        assertThat(jpaContactPerson.getContactPersonProfile().getSurname()).isEqualTo(VALID_SURNAME);
        assertThat(jpaContactPerson.getContactPersonProfile().getPhoneNumber()).isEqualTo(VALID_PHONE);
    }

    @ParameterizedTest
    @MethodSource("provideIncorrectProfileInformation")
    void return_400_when_register_contact_person_with_malformed_data_request(RegisterESALMemberRequestDto registerESALMemberRequestDto) throws Exception {
        // GIVEN
        RegisterESALMemberRequestDto dto = registerESALMemberRequestDto;
        // WHEN + THEN
        mvc.perform(post("/api/v1/contactperson")
                .content(objectMapper.writeValueAsString(dto))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    private static Stream<RegisterESALMemberRequestDto> provideIncorrectProfileInformation() {
        return Stream.of(
                RegisterESALMemberRequestDto.builder()
                        .name("ABCD2345")
                        .surname(VALID_SURNAME)
                        .phoneNumber(VALID_PHONE)
                        .email(DEFAULT_EMAIL)
                        .password(DEFAULT_PASSWORD)
                        .build(),
                RegisterESALMemberRequestDto.builder()
                        .name("")
                        .surname(VALID_SURNAME)
                        .phoneNumber(VALID_PHONE)
                        .email(DEFAULT_EMAIL)
                        .password(DEFAULT_PASSWORD)
                        .build(),
                RegisterESALMemberRequestDto.builder()
                        .name(VALID_NAME)
                        .surname("abcd1234")
                        .phoneNumber(VALID_PHONE)
                        .email(DEFAULT_EMAIL)
                        .password(DEFAULT_PASSWORD)
                        .build(),
                RegisterESALMemberRequestDto.builder()
                        .name(VALID_NAME)
                        .surname("")
                        .phoneNumber(VALID_PHONE)
                        .email(DEFAULT_EMAIL)
                        .password(DEFAULT_PASSWORD)
                        .build(),
                RegisterESALMemberRequestDto.builder()
                        .name(VALID_NAME)
                        .surname(VALID_SURNAME)
                        .phoneNumber("123987231589")
                        .email(DEFAULT_EMAIL)
                        .password(DEFAULT_PASSWORD)
                        .build(),
                RegisterESALMemberRequestDto.builder()
                        .name(VALID_NAME)
                        .surname(VALID_SURNAME)
                        .phoneNumber("")
                        .email(DEFAULT_EMAIL)
                        .password(DEFAULT_PASSWORD)
                        .build(),
                RegisterESALMemberRequestDto.builder()
                        .name(VALID_NAME)
                        .surname(VALID_SURNAME)
                        .phoneNumber(VALID_PHONE)
                        .email("foo@@@huellapositiva.com")
                        .password(DEFAULT_PASSWORD)
                        .build(),
                RegisterESALMemberRequestDto.builder()
                        .name(VALID_NAME)
                        .surname(VALID_SURNAME)
                        .phoneNumber(VALID_PHONE)
                        .email("   ")
                        .password(DEFAULT_PASSWORD)
                        .build(),
                RegisterESALMemberRequestDto.builder()
                        .name(VALID_NAME)
                        .surname(VALID_SURNAME)
                        .phoneNumber(VALID_PHONE)
                        .email(DEFAULT_EMAIL)
                        .password("asfas`´^][{}~~")
                        .build(),
                RegisterESALMemberRequestDto.builder()
                        .name(VALID_NAME)
                        .surname(VALID_SURNAME)
                        .phoneNumber(VALID_PHONE)
                        .email(DEFAULT_EMAIL)
                        .password("asfa")
                        .build(),
                RegisterESALMemberRequestDto.builder()
                        .name(VALID_NAME)
                        .surname(VALID_SURNAME)
                        .phoneNumber(VALID_PHONE)
                        .email(DEFAULT_EMAIL)
                        .password("           ")
                        .build()
        );
    }

    @Test
    void return_204_when_upload_photo_successfully() throws Exception {
        JpaContactPerson contactPerson = testData.createESALJpaContactPerson(VALID_NAME, VALID_SURNAME, VALID_PHONE, DEFAULT_ESAL_CONTACT_PERSON_EMAIL, DEFAULT_PASSWORD);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_ESAL_CONTACT_PERSON_EMAIL, DEFAULT_PASSWORD);
        InputStream is = getClass().getClassLoader().getResourceAsStream("images/huellapositiva-logo.png");
        mvc.perform(multipart("/api/v1/contactperson/profile/photo")
                .file(new MockMultipartFile("photo", "photo-test.PNG", "image/png", is))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .contentType(MULTIPART_FORM_DATA)
                .with(csrf())
                .accept(APPLICATION_JSON))
                .andExpect(status().isNoContent());

        contactPerson = jpaContactPersonRepository.findByAccountIdWithProfile(contactPerson.getCredential().getId()).orElseThrow(UserNotFoundException::new);
        assertThat(contactPerson.getContactPersonProfile().getPhotoUrl()).isNotNull();
    }

    @Test
    void return_204_when_upload_photo_successfully_with_profile() throws Exception {
        JpaContactPerson contactPerson = testData.createESALJpaContactPersonWithProfile(VALID_NAME, VALID_SURNAME, VALID_PHONE, DEFAULT_ESAL_CONTACT_PERSON_EMAIL, DEFAULT_PASSWORD);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_ESAL_CONTACT_PERSON_EMAIL, DEFAULT_PASSWORD);
        String originalPhotoUrl = contactPerson.getContactPersonProfile().getPhotoUrl();

        InputStream is = getClass().getClassLoader().getResourceAsStream("images/huellapositiva-logo.png");
        mvc.perform(multipart("/api/v1/contactperson/profile/photo")
                .file(new MockMultipartFile("photo", "photo-test.PNG", "image/png", is))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .contentType(MULTIPART_FORM_DATA)
                .with(csrf())
                .accept(APPLICATION_JSON))
                .andExpect(status().isNoContent());

        JpaContactPerson updatedContactPerson =
                jpaContactPersonRepository.findByAccountIdWithProfile(contactPerson.getCredential().getId()).orElseThrow(UserNotFoundException::new);
        String updatedPhotoUrl = updatedContactPerson.getContactPersonProfile().getPhotoUrl();

        assertThat(updatedPhotoUrl).isNotNull();
        assertThat(updatedPhotoUrl).isNotEqualTo(originalPhotoUrl);
    }

    @Test
    void return_204_when_upload_photo_successfully_with_no_profile() throws Exception {
        JpaContactPerson contactPerson = testData.createESALJpaContactPerson(VALID_NAME, VALID_SURNAME, VALID_PHONE, DEFAULT_ESAL_CONTACT_PERSON_EMAIL, DEFAULT_PASSWORD);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_ESAL_CONTACT_PERSON_EMAIL, DEFAULT_PASSWORD);

        InputStream is = getClass().getClassLoader().getResourceAsStream("images/huellapositiva-logo.png");
        mvc.perform(multipart("/api/v1/contactperson/profile/photo")
                .file(new MockMultipartFile("photo", "photo-test.PNG", "image/png", is))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .contentType(MULTIPART_FORM_DATA)
                .with(csrf())
                .accept(APPLICATION_JSON))
                .andExpect(status().isNoContent());

        contactPerson = jpaContactPersonRepository.findByAccountIdWithProfile(contactPerson.getCredential().getId()).orElseThrow(UserNotFoundException::new);
        assertThat(contactPerson.getContactPersonProfile().getPhotoUrl()).isNotNull();
    }
    @Test
    void return_400_when_the_photo_uploaded_is_too_big() throws Exception {
        testData.createESALJpaContactPersonWithProfile(VALID_NAME, VALID_SURNAME, VALID_PHONE, DEFAULT_ESAL_CONTACT_PERSON_EMAIL, DEFAULT_PASSWORD);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_ESAL_CONTACT_PERSON_EMAIL, DEFAULT_PASSWORD);

        InputStream is = getClass().getClassLoader().getResourceAsStream("images/Sample-png-image-3mb.png");
        mvc.perform(multipart("/api/v1/contactperson/profile/photo")
                .file(new MockMultipartFile("photo", "Sample-png-image-3mb.png", "image/png", is))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .contentType(MULTIPART_FORM_DATA)
                .with(csrf())
                .accept(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void return_400_when_the_photo_uploaded_is_oversized() throws Exception {
        testData.createESALJpaContactPersonWithProfile(VALID_NAME, VALID_SURNAME, VALID_PHONE, DEFAULT_ESAL_CONTACT_PERSON_EMAIL, DEFAULT_PASSWORD);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_ESAL_CONTACT_PERSON_EMAIL, DEFAULT_PASSWORD);

        InputStream is = getClass().getClassLoader().getResourceAsStream("images/oversized.png");
        mvc.perform(multipart("/api/v1/contactperson/profile/photo")
                .file(new MockMultipartFile("photo", "Sample-png-image-3mb.png", "image/png", is))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .contentType(MULTIPART_FORM_DATA)
                .with(csrf())
                .accept(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void return_400_when_uploaded_file_is_not_JPG_JPEG_PNG_GIF() throws Exception {
        testData.createESALJpaContactPersonWithProfile(VALID_NAME, VALID_SURNAME, VALID_PHONE, DEFAULT_ESAL_CONTACT_PERSON_EMAIL, DEFAULT_PASSWORD);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_ESAL_CONTACT_PERSON_EMAIL, DEFAULT_PASSWORD);
        InputStream is = getClass().getClassLoader().getResourceAsStream("documents/pdf-test.pdf");
        mvc.perform(multipart("/api/v1/contactperson/profile/photo")
                .file(new MockMultipartFile("photo", "pdf-test.pdf", "application/pdf", is))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .contentType(MULTIPART_FORM_DATA)
                .with(csrf())
                .accept(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void return_400_when_there_is_not_photo_uploaded() throws Exception {
        testData.createESALJpaContactPersonWithProfile(VALID_NAME, VALID_SURNAME, VALID_PHONE, DEFAULT_ESAL_CONTACT_PERSON_EMAIL, DEFAULT_PASSWORD);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_ESAL_CONTACT_PERSON_EMAIL, DEFAULT_PASSWORD);
        mvc.perform(multipart("/api/v1/contactperson/profile/photo")
                .file(new MockMultipartFile("photo", "photo-test.PNG", "image/png", InputStream.nullInputStream()))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .contentType(MULTIPART_FORM_DATA)
                .with(csrf())
                .accept(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
