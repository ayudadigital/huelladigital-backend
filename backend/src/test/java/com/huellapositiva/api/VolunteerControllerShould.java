package com.huellapositiva.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huellapositiva.application.dto.AuthenticationRequestDto;
import com.huellapositiva.application.dto.JwtResponseDto;
import com.huellapositiva.application.dto.ProfileDto;
import com.huellapositiva.domain.model.valueobjects.Roles;
import com.huellapositiva.infrastructure.orm.entities.JpaVolunteer;
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
import java.util.List;
import java.util.stream.Stream;

import static com.huellapositiva.domain.model.valueobjects.Roles.VOLUNTEER_NOT_CONFIRMED;
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

        JpaVolunteer jpaVolunteer = jpaVolunteerRepository.findByEmailWithCredentialLocationAndProfile(DEFAULT_EMAIL);
        assertThat(jpaVolunteer.getProfile().getCurriculumVitaeUrl()).isNotNull();
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

        JpaVolunteer jpaVolunteer = jpaVolunteerRepository.findByEmailWithCredentialLocationAndProfile(DEFAULT_EMAIL);
        assertThat(jpaVolunteer.getProfile().getCurriculumVitaeUrl()).isNotNull();
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
    void return_400_when_uploaded_file_PDF_or_WORD_is_too_big() throws Exception {
        testData.createVolunteer(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        JwtResponseDto jwtResponseDto = TestUtils.loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);
        InputStream is = getClass().getClassLoader().getResourceAsStream("images/doc-test.docx");
        mvc.perform(multipart("/api/v1/volunteers/cv-upload")
                .file(new MockMultipartFile("cv", "doc-test.docx", "application/msword", is))
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
        mvc.perform(multipart("/api/v1/volunteers/cv-upload")
                .file(new MockMultipartFile("cv", null, "application/pdf", InputStream.nullInputStream()))
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
        JpaVolunteer jpaVolunteer = testData.createVolunteerWithProfile(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        JwtResponseDto jwtResponseDto = TestUtils.loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);
        MockHttpServletResponse response = mvc.perform(get("/api/v1/volunteers/fetchProfileInformation")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .with(csrf())
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        ProfileDto profileDto = objectMapper.readValue(response.getContentAsString(), ProfileDto.class);
        assertThat(profileDto.getName()).isEqualTo(jpaVolunteer.getProfile().getName());
        assertThat(profileDto.getSurname()).isEqualTo(jpaVolunteer.getProfile().getSurname());
        assertThat(profileDto.getBirthDate()).isEqualTo(jpaVolunteer.getProfile().getBirthDate());
        assertThat(profileDto.getPhoneNumber()).isEqualTo(jpaVolunteer.getProfile().getPhoneNumber());
        assertThat(profileDto.getProvince()).isEqualTo(jpaVolunteer.getLocation().getProvince());
        assertThat(profileDto.getZipCode()).isEqualTo(jpaVolunteer.getLocation().getZipCode());
        assertThat(profileDto.getIsland()).isEqualTo(jpaVolunteer.getLocation().getIsland());
        assertThat(profileDto.getTown()).isEqualTo(jpaVolunteer.getLocation().getTown());
        assertThat(profileDto.getAddress()).isEqualTo(jpaVolunteer.getLocation().getAddress());
        assertThat(profileDto.getPhoto()).isEqualTo(jpaVolunteer.getProfile().getPhotoUrl());
        assertThat(profileDto.getCurriculumVitae()).isEqualTo(jpaVolunteer.getProfile().getCurriculumVitaeUrl());
        assertThat(profileDto.getTwitter()).isEqualTo(jpaVolunteer.getProfile().getTwitter());
        assertThat(profileDto.getLinkedin()).isEqualTo(jpaVolunteer.getProfile().getLinkedin());
        assertThat(profileDto.getInstagram()).isEqualTo(jpaVolunteer.getProfile().getInstagram());
        assertThat(profileDto.getAdditionalInformation()).isEqualTo(jpaVolunteer.getProfile().getAdditionalInformation());
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
        assertThat(jpaVolunteer.getProfile().getName()).isEqualTo(profileDtoDataEntry.getName());
        assertThat(jpaVolunteer.getProfile().getSurname()).isEqualTo(profileDtoDataEntry.getSurname());
        assertThat(jpaVolunteer.getProfile().getBirthDate()).isEqualTo(profileDtoDataEntry.getBirthDate());
        assertThat(jpaVolunteer.getProfile().getPhoneNumber()).isEqualTo(profileDtoDataEntry.getPhoneNumber());
        assertThat(jpaVolunteer.getLocation().getProvince()).isEqualTo(profileDtoDataEntry.getProvince());
        assertThat(jpaVolunteer.getLocation().getZipCode()).isEqualTo(profileDtoDataEntry.getZipCode());
        assertThat(jpaVolunteer.getLocation().getIsland()).isEqualTo(profileDtoDataEntry.getIsland());
        assertThat(jpaVolunteer.getLocation().getTown()).isEqualTo(profileDtoDataEntry.getTown());
        assertThat(jpaVolunteer.getLocation().getAddress()).isEqualTo(profileDtoDataEntry.getAddress());
        assertThat(jpaVolunteer.getProfile().getPhotoUrl()).isEqualTo(profileDtoDataEntry.getPhoto());
        assertThat(jpaVolunteer.getProfile().getCurriculumVitaeUrl()).isEqualTo(profileDtoDataEntry.getCurriculumVitae());
        assertThat(jpaVolunteer.getProfile().getTwitter()).isEqualTo(profileDtoDataEntry.getTwitter());
        assertThat(jpaVolunteer.getProfile().getLinkedin()).isEqualTo(profileDtoDataEntry.getLinkedin());
        assertThat(jpaVolunteer.getProfile().getInstagram()).isEqualTo(profileDtoDataEntry.getInstagram());
        assertThat(jpaVolunteer.getProfile().getAdditionalInformation()).isEqualTo(profileDtoDataEntry.getAdditionalInformation());
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

        TestUtils.loginAndGetJwtTokens(mvc, DEFAULT_EMAIL_2, DEFAULT_PASSWORD);
        JpaVolunteer jpaVolunteer = jpaVolunteerRepository.findByEmailWithCredentialAndLocation(DEFAULT_EMAIL_2);
        assertThat(jpaVolunteer.getProfile().getId()).isNotNull();
        assertThat(jpaVolunteer.getCredential().getRoles()).hasToString("[" + VOLUNTEER_NOT_CONFIRMED + "]");
        assertThat(jpaVolunteer.getProfile().getName()).isEqualTo(profileDto.getName());
        assertThat(jpaVolunteer.getProfile().getSurname()).isEqualTo(profileDto.getSurname());
        assertThat(jpaVolunteer.getProfile().getBirthDate()).isEqualTo(profileDto.getBirthDate());
        assertThat(jpaVolunteer.getProfile().getPhoneNumber()).isEqualTo(profileDto.getPhoneNumber());
        assertThat(jpaVolunteer.getLocation().getProvince()).isEqualTo(profileDto.getProvince());
        assertThat(jpaVolunteer.getLocation().getZipCode()).isEqualTo(profileDto.getZipCode());
        assertThat(jpaVolunteer.getLocation().getIsland()).isEqualTo(profileDto.getIsland());
        assertThat(jpaVolunteer.getLocation().getTown()).isEqualTo(profileDto.getTown());
        assertThat(jpaVolunteer.getLocation().getAddress()).isEqualTo(profileDto.getAddress());
        assertThat(jpaVolunteer.getProfile().getPhotoUrl()).isEqualTo(profileDto.getPhoto());
        assertThat(jpaVolunteer.getProfile().getCurriculumVitaeUrl()).isEqualTo(profileDto.getCurriculumVitae());
        assertThat(jpaVolunteer.getProfile().getTwitter()).isEqualTo(profileDto.getTwitter());
        assertThat(jpaVolunteer.getProfile().getLinkedin()).isEqualTo(profileDto.getLinkedin());
        assertThat(jpaVolunteer.getProfile().getInstagram()).isEqualTo(profileDto.getInstagram());
        assertThat(jpaVolunteer.getProfile().getAdditionalInformation()).isEqualTo(profileDto.getAdditionalInformation());
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
                .file(new MockMultipartFile("photo", "photo-test.PNG", "image/png", is))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .contentType(MULTIPART_FORM_DATA)
                .with(csrf())
                .accept(APPLICATION_JSON))
                .andExpect(status().isNoContent());

        JpaVolunteer jpaVolunteer = jpaVolunteerRepository.findByEmailWithCredentialLocationAndProfile(DEFAULT_EMAIL);
        assertThat(jpaVolunteer.getProfile().getPhotoUrl()).isNotNull();
    }

    @Test
    void return_204_when_upload_photo_successfully_with_profile() throws Exception {
        testData.createVolunteerWithProfile(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        JwtResponseDto jwtResponseDto = TestUtils.loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        InputStream is = getClass().getClassLoader().getResourceAsStream("images/huellapositiva-logo.png");
        mvc.perform(multipart("/api/v1/volunteers/photo-upload")
                .file(new MockMultipartFile("photo", "photo-test.PNG", "image/png", is))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .contentType(MULTIPART_FORM_DATA)
                .with(csrf())
                .accept(APPLICATION_JSON))
                .andExpect(status().isNoContent());

        JpaVolunteer jpaVolunteer = jpaVolunteerRepository.findByEmailWithCredentialLocationAndProfile(DEFAULT_EMAIL);
        assertThat(jpaVolunteer.getProfile().getPhotoUrl()).isNotNull();
    }

    @Test
    void return_400_when_the_photo_uploaded_is_too_big() throws Exception {
        testData.createVolunteerWithProfile(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        JwtResponseDto jwtResponseDto = TestUtils.loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        InputStream is = getClass().getClassLoader().getResourceAsStream("images/Sample-png-image-3mb.png");
        mvc.perform(multipart("/api/v1/volunteers/photo-upload")
                .file(new MockMultipartFile("photo", "Sample-png-image-3mb.png", "image/png", is))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .contentType(MULTIPART_FORM_DATA)
                .with(csrf())
                .accept(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void return_400_when_the_photo_uploaded_is_oversized() throws Exception {
        testData.createVolunteerWithProfile(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        JwtResponseDto jwtResponseDto = TestUtils.loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        InputStream is = getClass().getClassLoader().getResourceAsStream("images/oversized.png");
        mvc.perform(multipart("/api/v1/volunteers/photo-upload")
                .file(new MockMultipartFile("photo", "Sample-png-image-3mb.png", "image/png", is))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .contentType(MULTIPART_FORM_DATA)
                .with(csrf())
                .accept(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
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

    @Test
    void return_400_when_there_is_not_photo_uploaded() throws Exception {
        testData.createVolunteer(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        JwtResponseDto jwtResponseDto = TestUtils.loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);
        mvc.perform(multipart("/api/v1/volunteers/photo-upload")
                .file(new MockMultipartFile("photo", null, "image/png", InputStream.nullInputStream()))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .contentType(MULTIPART_FORM_DATA)
                .with(csrf())
                .accept(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}






