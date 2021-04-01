package com.huellapositiva.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huellapositiva.application.dto.RegisterContactPersonDto;
import com.huellapositiva.application.dto.JwtResponseDto;
import com.huellapositiva.application.dto.UpdateContactPersonProfileRequestDto;
import com.huellapositiva.application.exception.UserNotFoundException;
import com.huellapositiva.domain.model.valueobjects.Roles;
import com.huellapositiva.infrastructure.orm.entities.JpaContactPerson;
import com.huellapositiva.infrastructure.orm.entities.JpaContactPersonProfile;
import com.huellapositiva.infrastructure.orm.entities.Role;
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
        RegisterContactPersonDto dto = new RegisterContactPersonDto(VALID_NAME, VALID_SURNAME, VALID_PHONE, DEFAULT_EMAIL, DEFAULT_PASSWORD);

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
    @MethodSource("provideIncorrectRegisterInformation")
    void return_400_when_register_contact_person_with_malformed_data_request(RegisterContactPersonDto registerContactPersonDto) throws Exception {
        // GIVEN
        RegisterContactPersonDto dto = registerContactPersonDto;
        // WHEN + THEN
        mvc.perform(post("/api/v1/contactperson")
                .content(objectMapper.writeValueAsString(dto))
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    private static Stream<RegisterContactPersonDto> provideIncorrectRegisterInformation() {
        return Stream.of(
                RegisterContactPersonDto.builder()
                        .name("ABCD2345")
                        .surname(VALID_SURNAME)
                        .phoneNumber(VALID_PHONE)
                        .email(DEFAULT_EMAIL)
                        .password(DEFAULT_PASSWORD)
                        .build(),
                RegisterContactPersonDto.builder()
                        .name("")
                        .surname(VALID_SURNAME)
                        .phoneNumber(VALID_PHONE)
                        .email(DEFAULT_EMAIL)
                        .password(DEFAULT_PASSWORD)
                        .build(),
                RegisterContactPersonDto.builder()
                        .name(VALID_NAME)
                        .surname("abcd1234")
                        .phoneNumber(VALID_PHONE)
                        .email(DEFAULT_EMAIL)
                        .password(DEFAULT_PASSWORD)
                        .build(),
                RegisterContactPersonDto.builder()
                        .name(VALID_NAME)
                        .surname("")
                        .phoneNumber(VALID_PHONE)
                        .email(DEFAULT_EMAIL)
                        .password(DEFAULT_PASSWORD)
                        .build(),
                RegisterContactPersonDto.builder()
                        .name(VALID_NAME)
                        .surname(VALID_SURNAME)
                        .phoneNumber("123987231589")
                        .email(DEFAULT_EMAIL)
                        .password(DEFAULT_PASSWORD)
                        .build(),
                RegisterContactPersonDto.builder()
                        .name(VALID_NAME)
                        .surname(VALID_SURNAME)
                        .phoneNumber("")
                        .email(DEFAULT_EMAIL)
                        .password(DEFAULT_PASSWORD)
                        .build(),
                RegisterContactPersonDto.builder()
                        .name(VALID_NAME)
                        .surname(VALID_SURNAME)
                        .phoneNumber(VALID_PHONE)
                        .email("foo@@@huellapositiva.com")
                        .password(DEFAULT_PASSWORD)
                        .build(),
                RegisterContactPersonDto.builder()
                        .name(VALID_NAME)
                        .surname(VALID_SURNAME)
                        .phoneNumber(VALID_PHONE)
                        .email("   ")
                        .password(DEFAULT_PASSWORD)
                        .build(),
                RegisterContactPersonDto.builder()
                        .name(VALID_NAME)
                        .surname(VALID_SURNAME)
                        .phoneNumber(VALID_PHONE)
                        .email(DEFAULT_EMAIL)
                        .password("asfas`´^][{}~~")
                        .build(),
                RegisterContactPersonDto.builder()
                        .name(VALID_NAME)
                        .surname(VALID_SURNAME)
                        .phoneNumber(VALID_PHONE)
                        .email(DEFAULT_EMAIL)
                        .password("asfa")
                        .build(),
                RegisterContactPersonDto.builder()
                        .name(VALID_NAME)
                        .surname(VALID_SURNAME)
                        .phoneNumber(VALID_PHONE)
                        .email(DEFAULT_EMAIL)
                        .password("           ")
                        .build()
        );
    }

    private static Stream<UpdateContactPersonProfileRequestDto> provideCorrectProfileInformationSameEmail() {
        return Stream.of(
                UpdateContactPersonProfileRequestDto.builder()
                        .name(VALID_NAME)
                        .surname(VALID_SURNAME)
                        .email(DEFAULT_ESAL_CONTACT_PERSON_EMAIL)
                        .phoneNumber("+4 123456789")
                        .build(),
                UpdateContactPersonProfileRequestDto.builder()
                        .name(VALID_NAME)
                        .surname(VALID_SURNAME)
                        .email(DEFAULT_ESAL_CONTACT_PERSON_EMAIL)
                        .phoneNumber("+344 123456789")
                        .build()
        );
    }

    @ParameterizedTest
    @MethodSource("provideCorrectProfileInformationSameEmail")
    void return_204_when_updates_profile_information_successfully_without_email(UpdateContactPersonProfileRequestDto updateContactPersonProfileRequestDto) throws Exception {
        testData.createESALJpaContactPerson(VALID_NAME, VALID_SURNAME, VALID_PHONE, DEFAULT_ESAL_CONTACT_PERSON_EMAIL, DEFAULT_PASSWORD);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_ESAL_CONTACT_PERSON_EMAIL, DEFAULT_PASSWORD);

        mvc.perform(post("/api/v1/contactperson/profile")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(updateContactPersonProfileRequestDto))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isNoContent());

        JpaContactPerson jpaContactPerson = jpaContactPersonRepository.findByEmail(DEFAULT_ESAL_CONTACT_PERSON_EMAIL)
                .orElseThrow(() -> new UserNotFoundException("Contact person not found. Email: " + DEFAULT_ESAL_CONTACT_PERSON_EMAIL));
        Role role = jpaContactPerson.getCredential().getRoles().stream().findFirst().get();
        assertThat(role.getName()).isEqualTo(Roles.CONTACT_PERSON.name());
        JpaContactPersonProfile profile = jpaContactPerson.getContactPersonProfile();
        assertThat(profile.getId()).isNotNull();
        assertThat(profile.getName()).isEqualTo(updateContactPersonProfileRequestDto.getName());
        assertThat(profile.getSurname()).isEqualTo(updateContactPersonProfileRequestDto.getSurname());
        assertThat(profile.getPhoneNumber()).isEqualTo(updateContactPersonProfileRequestDto.getPhoneNumber());
    }

    private static Stream<UpdateContactPersonProfileRequestDto> provideCorrectProfileInformationDifferentEmail() {
        return Stream.of(
                UpdateContactPersonProfileRequestDto.builder()
                        .name(VALID_NAME)
                        .surname(VALID_SURNAME)
                        .email(DEFAULT_EMAIL_2)
                        .phoneNumber("+4 123456789")
                        .build(),
                UpdateContactPersonProfileRequestDto.builder()
                        .name(VALID_NAME)
                        .surname(VALID_SURNAME)
                        .email(DEFAULT_EMAIL_2)
                        .phoneNumber("+344 123456789")
                        .build()
        );
    }

    @ParameterizedTest
    @MethodSource("provideCorrectProfileInformationDifferentEmail")
    void return_204_when_updates_profile_information_successfully_with_email(UpdateContactPersonProfileRequestDto updateContactPersonProfileRequestDto) throws Exception {
        testData.createESALJpaContactPerson(VALID_NAME, VALID_SURNAME, VALID_PHONE, DEFAULT_ESAL_CONTACT_PERSON_EMAIL, DEFAULT_PASSWORD);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_ESAL_CONTACT_PERSON_EMAIL, DEFAULT_PASSWORD);

        mvc.perform(post("/api/v1/contactperson/profile")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(updateContactPersonProfileRequestDto))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isNoContent());

        JpaContactPerson jpaContactPerson = jpaContactPersonRepository.findByEmail(DEFAULT_EMAIL_2)
                .orElseThrow(() -> new UserNotFoundException("Contact person not found. Email: " + DEFAULT_EMAIL_2));
        Role role = jpaContactPerson.getCredential().getRoles().stream().findFirst().get();
        assertThat(role.getName()).isEqualTo(Roles.CONTACT_PERSON_NOT_CONFIRMED.name());
        JpaContactPersonProfile profile = jpaContactPerson.getContactPersonProfile();
        assertThat(profile.getId()).isNotNull();
        assertThat(profile.getName()).isEqualTo(updateContactPersonProfileRequestDto.getName());
        assertThat(profile.getSurname()).isEqualTo(updateContactPersonProfileRequestDto.getSurname());
        assertThat(profile.getPhoneNumber()).isEqualTo(updateContactPersonProfileRequestDto.getPhoneNumber());
    }

    @ParameterizedTest
    @MethodSource("provideIncorrectUpdateInformation")
    void return_400_when_not_provided_correct_information_for_updating_profile(UpdateContactPersonProfileRequestDto profileDto) throws Exception {
        testData.createESALJpaContactPerson(VALID_NAME, VALID_SURNAME, VALID_PHONE, DEFAULT_ESAL_CONTACT_PERSON_EMAIL, DEFAULT_PASSWORD);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_ESAL_CONTACT_PERSON_EMAIL, DEFAULT_PASSWORD);

        mvc.perform(post("/api/v1/contactperson/profile")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(profileDto))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    private static Stream<UpdateContactPersonProfileRequestDto> provideIncorrectUpdateInformation() {
        return Stream.of(
                UpdateContactPersonProfileRequestDto.builder()
                        .name("abcd1234")
                        .surname(VALID_SURNAME)
                        .email(DEFAULT_EMAIL)
                        .phoneNumber(VALID_PHONE)
                        .build(),
                UpdateContactPersonProfileRequestDto.builder()
                        .name("")
                        .surname(VALID_SURNAME)
                        .email(DEFAULT_EMAIL)
                        .phoneNumber(VALID_PHONE)
                        .build(),
                UpdateContactPersonProfileRequestDto.builder()
                        .name(VALID_NAME)
                        .surname("abcd1234")
                        .email(DEFAULT_EMAIL)
                        .phoneNumber(VALID_PHONE)
                        .build(),
                UpdateContactPersonProfileRequestDto.builder()
                        .name(VALID_NAME)
                        .surname("")
                        .email(DEFAULT_EMAIL)
                        .phoneNumber(VALID_PHONE)
                        .build(),
                UpdateContactPersonProfileRequestDto.builder()
                        .name(VALID_NAME)
                        .surname(VALID_SURNAME)
                        .email("   ")
                        .phoneNumber(VALID_PHONE)
                        .build(),
                UpdateContactPersonProfileRequestDto.builder()
                        .name(VALID_NAME)
                        .surname(VALID_SURNAME)
                        .email("foo@@@huellapositiva.com")
                        .phoneNumber(VALID_PHONE)
                        .build(),
                UpdateContactPersonProfileRequestDto.builder()
                        .name(VALID_NAME)
                        .surname(VALID_SURNAME)
                        .email(DEFAULT_EMAIL)
                        .phoneNumber("+34 12345d789")
                        .build(),
                UpdateContactPersonProfileRequestDto.builder()
                        .name(VALID_NAME)
                        .surname(VALID_SURNAME)
                        .email(DEFAULT_EMAIL)
                        .phoneNumber("34 123456789")
                        .build(),
                UpdateContactPersonProfileRequestDto.builder()
                        .name(VALID_NAME)
                        .surname(VALID_SURNAME)
                        .email(DEFAULT_EMAIL)
                        .phoneNumber("34 12345789101112")
                        .build(),
                UpdateContactPersonProfileRequestDto.builder()
                        .name(VALID_NAME)
                        .surname(VALID_SURNAME)
                        .email(DEFAULT_EMAIL)
                        .phoneNumber("+3444 123456789")
                        .build()
        );
    }

    @Test
    void return_409_when_provided_new_email_already_bound_to_a_different_account() throws Exception {
        testData.createESALJpaContactPerson(VALID_NAME, VALID_SURNAME, VALID_PHONE, DEFAULT_ESAL_CONTACT_PERSON_EMAIL, DEFAULT_PASSWORD);
        testData.createESALJpaContactPerson(VALID_NAME, VALID_SURNAME, VALID_PHONE, DEFAULT_EMAIL_2, DEFAULT_PASSWORD);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_ESAL_CONTACT_PERSON_EMAIL, DEFAULT_PASSWORD);

        UpdateContactPersonProfileRequestDto profileDto = UpdateContactPersonProfileRequestDto.builder()
                .name(VALID_NAME)
                .surname(VALID_SURNAME)
                .email(DEFAULT_EMAIL_2)
                .phoneNumber(VALID_PHONE)
                .build();

        mvc.perform(post("/api/v1/contactperson/profile")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(profileDto))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isConflict());
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

        assertThat(updatedPhotoUrl).isNotNull().isNotEqualTo(originalPhotoUrl);
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
