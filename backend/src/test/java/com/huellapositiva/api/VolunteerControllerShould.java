package com.huellapositiva.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huellapositiva.application.dto.AuthenticationRequestDto;
import com.huellapositiva.application.dto.ESALRequestDto;
import com.huellapositiva.application.dto.JwtResponseDto;
import com.huellapositiva.application.dto.ProfileDto;
import com.huellapositiva.domain.model.valueobjects.Roles;
import com.huellapositiva.infrastructure.orm.entities.JpaVolunteer;
import com.huellapositiva.infrastructure.orm.repository.JpaProfileRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaVolunteerRepository;
import com.huellapositiva.infrastructure.security.JwtService;
import com.huellapositiva.util.ProfileDtoDataEntry;
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
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static com.huellapositiva.util.TestData.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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

    @Autowired
    private JpaProfileRepository jpaProfileRepository;

    @BeforeEach
    void beforeEach() {
        testData.resetData();
    }

    @Test
    void return_201_and_tokens_when_registering_volunteer() throws Exception {
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
    void return_400_registering_volunteer_without_password() throws Exception {
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
    void return_400_registering_volunteer_with_short_password() throws Exception {
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
    void return_400_registering_volunteer_without_email() throws Exception {
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
    void return_400_registering_volunteer_null() throws Exception {
        mvc.perform(post(SIGN_UP_URL)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @MethodSource("provideMalformedEmails")
    void return_400_when_registering_volunteer_with_malformed_email(String malformedEmail) throws Exception {
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
    void return_200_when_upload_curriculum_vitae_successfully() throws Exception {
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
    void return_200_when_upload_curriculum_vitae_successfully_with_profile_created() throws Exception {
        testData.createVolunteerWithProfile(DEFAULT_EMAIL, DEFAULT_PASSWORD);
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

    @Test
    void return_400_when_there_is_not_cv_uploaded() throws Exception {
        testData.createVolunteer(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        JwtResponseDto jwtResponseDto = TestUtils.loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);
        InputStream is = null;
        mvc.perform(multipart("/api/v1/volunteers/cv-upload")
                .file(new MockMultipartFile("cv", null, "application/pdf", is))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .contentType(MULTIPART_FORM_DATA)
                .with(csrf())
                .accept(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void return_200_when_get_profile_information_without_profile_information_stored() throws Exception {
        testData.createVolunteer(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        JwtResponseDto jwtResponseDto = TestUtils.loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);
        mvc.perform(get("/api/v1/volunteers/fetchProfileInformation")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .contentType(MULTIPART_FORM_DATA)
                .with(csrf())
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void return_200_when_get_profile_information_with_profile_information_stored() throws Exception {
        testData.createVolunteerWithProfile(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        JwtResponseDto jwtResponseDto = TestUtils.loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        mvc.perform(get("/api/v1/volunteers/fetchProfileInformation")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .with(csrf())
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @MethodSource("provideCorrectProfileInformationSameEmail")
    void return_204_when_updates_profile_information_successfully_without_email(ProfileDtoDataEntry profileDtoDataEntry) throws Exception {
        testData.createVolunteer(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        JwtResponseDto jwtResponseDto = TestUtils.loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        mvc.perform(multipart("/api/v1/volunteers/updateProfileInformation")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(profileDtoDataEntry))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isNoContent());

        JpaVolunteer jpaVolunteer = jpaVolunteerRepository.findByEmailWithCredentialAndLocation(DEFAULT_EMAIL);
        assertThat(jpaVolunteer.getProfile().getId()).isNotNull();
    }

    private static Stream<ProfileDtoDataEntry> provideCorrectProfileInformationSameEmail() {
        return Stream.of(
                ProfileDtoDataEntry.builder()
                        .name("nombre")
                        .surname("apellido")
                        .email(DEFAULT_EMAIL)
                        .phoneNumber("+4 123456789")
                        .birthDate("2000-12-10")
                        .province("Las Palmas")
                        .address("hola2")
                        .zipCode("35100")
                        .island("Fuerteventura")
                        .town("hola")
                        .twitter("https://www.twitter.com/home")
                        .linkedin("https://linkedin.com/in/home")
                        .additionalInformation("add")
                        .build(),
                ProfileDtoDataEntry.builder()
                        .name("nombre")
                        .surname("apellido")
                        .email(DEFAULT_EMAIL)
                        .phoneNumber("+344 123456789")
                        .birthDate("2000-12-10")
                        .zipCode("38000")
                        .island("Fuerteventura")
                        .twitter("https://twitter.com/home")
                        .instagram("https://www.instagram.com/home")
                        .additionalInformation("add")
                        .build()
        );
    }

    @ParameterizedTest
    @MethodSource("provideCorrectProfileInformationDifferentEmail")
    void return_204_when_updates_profile_information_successfully_with_email(ProfileDtoDataEntry profileDto) throws Exception {
        testData.createVolunteer(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        JwtResponseDto jwtResponseDto = TestUtils.loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        mvc.perform(multipart("/api/v1/volunteers/updateProfileInformation")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(profileDto))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isNoContent());

        JpaVolunteer jpaVolunteer = jpaVolunteerRepository.findByEmailWithCredentialAndLocation(DEFAULT_EMAIL_2);
        assertThat(jpaVolunteer.getProfile().getId()).isNotNull();
    }

    private static Stream<ProfileDtoDataEntry> provideCorrectProfileInformationDifferentEmail() {
        return Stream.of(
                ProfileDtoDataEntry.builder()
                        .name("nombre")
                        .surname("apellido")
                        .email(DEFAULT_EMAIL_2)
                        .phoneNumber("+34 123456789")
                        .birthDate("2000-12-10")
                        .zipCode("35000")
                        .island("Fuerteventura")
                        .build()
        );
    }

    @ParameterizedTest
    @MethodSource("provideIncorrectProfileInformation")
    void return_400_when_not_provided_correct_information_for_updating_profile(ProfileDtoDataEntry profileDto) throws Exception {
        testData.createVolunteer(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        JwtResponseDto jwtResponseDto = TestUtils.loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        mvc.perform(multipart("/api/v1/volunteers/updateProfileInformation")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(profileDto))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    private static Stream<ProfileDtoDataEntry> provideIncorrectProfileInformation() {
        return Stream.of(
                ProfileDtoDataEntry.builder()
                        .name("nombre")
                        .surname("apellido")
                        .email(DEFAULT_EMAIL)
                        .phoneNumber("+34 123456789")
                        .birthDate("2000-12-10")
                        .province("Las Palmas")
                        .address("hola2")
                        .island("Fuerteventura")
                        .town("hola3")
                        .twitter("twitter")
                        .instagram("instagram")
                        .linkedin("linkedin")
                        .additionalInformation("add")
                        .build(),
                ProfileDtoDataEntry.builder()
                        .name("nombre")
                        .surname("apellido")
                        .email(DEFAULT_EMAIL)
                        .phoneNumber("+34 123456789")
                        .birthDate("2000-12-10")
                        .zipCode("35100")
                        .twitter("twitter")
                        .instagram("instagram")
                        .linkedin("linkedin")
                        .additionalInformation("add")
                        .build(),
                ProfileDtoDataEntry.builder()
                        .name("nombre")
                        .surname("apellido")
                        .email(DEFAULT_EMAIL)
                        .birthDate("2000-12-10")
                        .province("hola1")
                        .address("hola2")
                        .zipCode("35100")
                        .island("Fuerteventura")
                        .town("hola3")
                        .twitter("twitter")
                        .instagram("instagram")
                        .linkedin("linkedin")
                        .additionalInformation("add")
                        .build(),
                ProfileDtoDataEntry.builder()
                        .name("nombre")
                        .surname("apellido")
                        .email(DEFAULT_EMAIL)
                        .phoneNumber("+34 123456789")
                        .birthDate("2000-12-10")
                        .build(),
                ProfileDtoDataEntry.builder()
                        .name("nombre")
                        .surname("apellido")
                        .phoneNumber("+34 123456789")
                        .birthDate("2000-12-10")
                        .zipCode("35100")
                        .island("Fuerteventura")
                        .build(),
                ProfileDtoDataEntry.builder()
                        .name("nombre")
                        .surname("apellido")
                        .email(DEFAULT_EMAIL)
                        .phoneNumber("+34 123456789")
                        .birthDate("2000-12-10")
                        .zipCode("35100")
                        .island("Islandia")
                        .build(),
                ProfileDtoDataEntry.builder()
                        .name("nombre")
                        .surname("apellido")
                        .email(DEFAULT_EMAIL_2)
                        .phoneNumber("+34 123456789")
                        .birthDate("2000-12-10")
                        .zipCode("3510055")
                        .island("Fuerteventura")
                        .build(),
                ProfileDtoDataEntry.builder()
                        .name("nombre")
                        .surname("apellido")
                        .email(DEFAULT_EMAIL_2)
                        .phoneNumber("+34 123456789")
                        .birthDate("2000-12-60")
                        .zipCode("35100")
                        .island("Fuerteventura")
                        .build(),
                ProfileDtoDataEntry.builder()
                        .name("nombre")
                        .surname("apellido")
                        .email(DEFAULT_EMAIL)
                        .phoneNumber("+34 123456789")
                        .birthDate("2000-12-10")
                        .province("Las Palmas")
                        .address("hola2")
                        .island("Fuerteventura")
                        .zipCode("38000")
                        .town("hola")
                        .twitter("https://instagram.com/joselito")
                        .instagram("instagram")
                        .linkedin("linkedin")
                        .additionalInformation("add")
                        .build(),
                ProfileDtoDataEntry.builder()
                        .name("nombre")
                        .surname("apellido")
                        .email(DEFAULT_EMAIL_2)
                        .phoneNumber("+34 123456789")
                        .birthDate("2000-12-10")
                        .zipCode("36100")
                        .island("Fuerteventura")
                        .build()
        );
    }

    @Test
    void return_409_when_provided_already_existing_email() throws Exception {
        testData.createVolunteer(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        testData.createVolunteer(DEFAULT_EMAIL_2,DEFAULT_PASSWORD);
        JwtResponseDto jwtResponseDto = TestUtils.loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        ProfileDtoDataEntry profileDto = ProfileDtoDataEntry.builder()
                .name("nombre")
                .surname("Farruquito")
                .email(DEFAULT_EMAIL_2)
                .phoneNumber("+34 123456789")
                .birthDate("2000-12-10")
                .province("Las Palmas")
                .address("hola2")
                .zipCode("12345")
                .island("Fuerteventura")
                .town("hola")
                .twitter("https://twitter.com/home")
                .instagram("https://instagram.com/home")
                .linkedin("https://linkedin.com/in/home")
                .additionalInformation("add")
                .build();

        mvc.perform(multipart("/api/v1/volunteers/updateProfileInformation")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(profileDto))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    void return_204_when_upload_photo_successfully() throws Exception {
        testData.createVolunteer(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        JwtResponseDto jwtResponseDto = TestUtils.loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);
        InputStream is = getClass().getClassLoader().getResourceAsStream("images/huellapositiva-logo.png");
        mvc.perform(multipart("/api/v1/volunteers/photo-upload")
                .file(new MockMultipartFile("photo", "photo-test.JPG", "image/jpeg", is))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .contentType(MULTIPART_FORM_DATA)
                .with(csrf())
                .accept(APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void return_204_when_upload_photo_successfully_with_profile() throws Exception {
        testData.createVolunteerWithProfile(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        JwtResponseDto jwtResponseDto = TestUtils.loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        InputStream is = getClass().getClassLoader().getResourceAsStream("images/huellapositiva-logo.png");
        mvc.perform(multipart("/api/v1/volunteers/photo-upload")
                .file(new MockMultipartFile("photo", "photo-test.JPG", "image/jpeg", is))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .contentType(MULTIPART_FORM_DATA)
                .with(csrf())
                .accept(APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void return_400_when_uploaded_file_is_not_JPG_JPEG_PNG_GIF() throws Exception {
        testData.createVolunteer(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        JwtResponseDto jwtResponseDto = TestUtils.loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);
        InputStream is = getClass().getClassLoader().getResourceAsStream("documents/pdf-test.pdf");
        mvc.perform(multipart("/api/v1/volunteers/photo-upload")
                .file(new MockMultipartFile("photo", "pdf-test.pdf", "application/pdf", is))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .contentType(MULTIPART_FORM_DATA)
                .with(csrf())
                .accept(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}






