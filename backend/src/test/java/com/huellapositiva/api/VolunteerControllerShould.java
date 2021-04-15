package com.huellapositiva.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huellapositiva.application.dto.*;
import com.huellapositiva.domain.model.valueobjects.Roles;
import com.huellapositiva.infrastructure.orm.entities.JpaVolunteer;
import com.huellapositiva.infrastructure.orm.repository.JpaVolunteerRepository;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(TestData.class)
class VolunteerControllerShould {

    private static final String SIGN_UP_URL = "/api/v1/volunteers";

    @Autowired
    private ObjectMapper objectMapper;

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
                .password(DEFAULT_PASSWORD)
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
        List<String> roles = jwtService.getUserDetails(responseDto.getAccessToken()).getSecond();
        assertThat(roles).containsExactly(Roles.VOLUNTEER_NOT_CONFIRMED.toString());
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
                "username@yahoo..com"
        );
    }

    @Test
    void return_409_when_there_is_a_user_with_the_same_email_address() throws Exception {
        AuthenticationRequestDto dto = AuthenticationRequestDto.builder()
                .email(DEFAULT_EMAIL)
                .password("password")
                .build();

        String body = objectMapper.writeValueAsString(dto);
        mvc.perform(post(SIGN_UP_URL)
                .content(body)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON));

        mvc.perform(post(SIGN_UP_URL)
                .content(body)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    void return_200_when_upload_curriculum_vitae_successfully() throws Exception {
        testData.createVolunteer(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);
        InputStream is = getClass().getClassLoader().getResourceAsStream("documents/pdf-test.pdf");
        mvc.perform(multipart("/api/v1/volunteers/profile/cv")
                .file(new MockMultipartFile("cv", "pdf-test.pdf", "application/pdf", is))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .contentType(MULTIPART_FORM_DATA)
                .with(csrf())
                .accept(APPLICATION_JSON))
                .andExpect(status().isNoContent());

        JpaVolunteer jpaVolunteer = jpaVolunteerRepository.findByEmailWithCredentialLocationAndProfile(DEFAULT_EMAIL);
        assertThat(jpaVolunteer.getProfile().getCurriculumVitaeUrl()).isNotNull();
    }

    @Test
    void return_200_when_upload_curriculum_vitae_successfully_with_profile_created() throws Exception {
        testData.createVolunteerWithProfile(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        InputStream is = getClass().getClassLoader().getResourceAsStream("documents/pdf-test.pdf");
        mvc.perform(multipart("/api/v1/volunteers/profile/cv")
                .file(new MockMultipartFile("cv", "pdf-test.pdf", "application/pdf", is))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .contentType(MULTIPART_FORM_DATA)
                .with(csrf())
                .accept(APPLICATION_JSON))
                .andExpect(status().isNoContent());

        JpaVolunteer jpaVolunteer = jpaVolunteerRepository.findByEmailWithCredentialLocationAndProfile(DEFAULT_EMAIL);
        assertThat(jpaVolunteer.getProfile().getCurriculumVitaeUrl()).isNotNull();
    }

    @Test
    void return_400_when_uploaded_file_is_not_PDF() throws Exception {
        testData.createVolunteer(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);
        InputStream is = getClass().getClassLoader().getResourceAsStream("images/huellapositiva-logo.png");
        mvc.perform(multipart("/api/v1/volunteers/profile/cv")
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
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);
        InputStream is = getClass().getClassLoader().getResourceAsStream("documents/doc-test.docx");
        mvc.perform(multipart("/api/v1/volunteers/profile/cv")
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
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);
        mvc.perform(multipart("/api/v1/volunteers/profile/cv")
                .file(new MockMultipartFile("cv", "doc-test.pdf", "application/pdf", InputStream.nullInputStream()))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .contentType(MULTIPART_FORM_DATA)
                .with(csrf())
                .accept(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void return_200_when_get_profile_information_without_profile_information_stored() throws Exception {
        testData.createVolunteer(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);
        mvc.perform(get("/api/v1/volunteers/profile")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .contentType(MULTIPART_FORM_DATA)
                .with(csrf())
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void return_200_when_get_profile_information_with_profile_information_stored() throws Exception {
        JpaVolunteer jpaVolunteer = testData.createVolunteerWithProfile(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);
        MockHttpServletResponse response = mvc.perform(get("/api/v1/volunteers/profile")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .with(csrf())
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        GetProfileResponseDto getProfileResponseDto = objectMapper.readValue(response.getContentAsString(), GetProfileResponseDto.class);
        assertThat(getProfileResponseDto.getName()).isEqualTo(jpaVolunteer.getProfile().getName());
        assertThat(getProfileResponseDto.getSurname()).isEqualTo(jpaVolunteer.getProfile().getSurname());
        assertThat(getProfileResponseDto.getBirthDate()).isEqualTo(jpaVolunteer.getProfile().getBirthDate().toString());
        assertThat(getProfileResponseDto.getPhoneNumber()).isEqualTo(jpaVolunteer.getProfile().getPhoneNumber());
        assertThat(getProfileResponseDto.getProvince()).isEqualTo(jpaVolunteer.getLocation().getProvince());
        assertThat(getProfileResponseDto.getZipCode()).isEqualTo(jpaVolunteer.getLocation().getZipCode());
        assertThat(getProfileResponseDto.getIsland()).isEqualTo(jpaVolunteer.getLocation().getIsland());
        assertThat(getProfileResponseDto.getTown()).isEqualTo(jpaVolunteer.getLocation().getTown());
        assertThat(getProfileResponseDto.getAddress()).isEqualTo(jpaVolunteer.getLocation().getAddress());
        assertThat(getProfileResponseDto.getPhoto()).isEqualTo(jpaVolunteer.getProfile().getPhotoUrl());
        assertThat(getProfileResponseDto.getCurriculumVitae()).isEqualTo(jpaVolunteer.getProfile().getCurriculumVitaeUrl());
        assertThat(getProfileResponseDto.getTwitter()).isEqualTo(jpaVolunteer.getProfile().getTwitter());
        assertThat(getProfileResponseDto.getLinkedin()).isEqualTo(jpaVolunteer.getProfile().getLinkedin());
        assertThat(getProfileResponseDto.getInstagram()).isEqualTo(jpaVolunteer.getProfile().getInstagram());
        assertThat(getProfileResponseDto.getAdditionalInformation()).isEqualTo(jpaVolunteer.getProfile().getAdditionalInformation());
    }

    @ParameterizedTest
    @MethodSource("provideCorrectProfileInformationSameEmail")
    void return_204_when_updates_profile_information_successfully_without_email(UpdateVolunteerProfileRequestDto UpdateVolunteerProfileRequestDto) throws Exception {
        testData.createVolunteer(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        mvc.perform(post("/api/v1/volunteers/profile")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(UpdateVolunteerProfileRequestDto))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isNoContent());

        JpaVolunteer jpaVolunteer = jpaVolunteerRepository.findByEmailWithCredentialAndLocation(DEFAULT_EMAIL);
        assertThat(jpaVolunteer.getProfile().getId()).isNotNull();
        assertThat(jpaVolunteer.getProfile().getName()).isEqualTo(UpdateVolunteerProfileRequestDto.getName());
        assertThat(jpaVolunteer.getProfile().getSurname()).isEqualTo(UpdateVolunteerProfileRequestDto.getSurname());
        assertThat(jpaVolunteer.getProfile().getBirthDate()).isEqualTo(UpdateVolunteerProfileRequestDto.getBirthDate());
        assertThat(jpaVolunteer.getProfile().getPhoneNumber()).isEqualTo(UpdateVolunteerProfileRequestDto.getPhoneNumber());
        assertThat(jpaVolunteer.getLocation().getProvince()).isEqualTo(UpdateVolunteerProfileRequestDto.getProvince());
        assertThat(jpaVolunteer.getLocation().getZipCode()).isEqualTo(UpdateVolunteerProfileRequestDto.getZipCode());
        assertThat(jpaVolunteer.getLocation().getIsland()).isEqualTo(UpdateVolunteerProfileRequestDto.getIsland());
        assertThat(jpaVolunteer.getLocation().getTown()).isEqualTo(UpdateVolunteerProfileRequestDto.getTown());
        assertThat(jpaVolunteer.getLocation().getAddress()).isEqualTo(UpdateVolunteerProfileRequestDto.getAddress());
        assertThat(jpaVolunteer.getProfile().getTwitter()).isEqualTo(UpdateVolunteerProfileRequestDto.getTwitter());
        assertThat(jpaVolunteer.getProfile().getLinkedin()).isEqualTo(UpdateVolunteerProfileRequestDto.getLinkedin());
        assertThat(jpaVolunteer.getProfile().getInstagram()).isEqualTo(UpdateVolunteerProfileRequestDto.getInstagram());
        assertThat(jpaVolunteer.getProfile().getAdditionalInformation()).isEqualTo(UpdateVolunteerProfileRequestDto.getAdditionalInformation());
    }

    private static Stream<UpdateVolunteerProfileRequestDto> provideCorrectProfileInformationSameEmail() {
        return Stream.of(
                UpdateVolunteerProfileRequestDto.builder()
                        .name(VALID_NAME)
                        .surname(VALID_SURNAME)
                        .email(DEFAULT_EMAIL)
                        .phoneNumber("+4 123456789")
                        .birthDate(VALID_BIRTHDAY)
                        .province("Las Palmas")
                        .address("hola2")
                        .zipCode("35100")
                        .island(VALID_ISLAND)
                        .town("hola")
                        .twitter(VALID_TWITTER)
                        .linkedin(VALID_LINKEDIN)
                        .additionalInformation(VALID_ADDITIONAL_INFO)
                        .build(),
                UpdateVolunteerProfileRequestDto.builder()
                        .name(VALID_NAME)
                        .surname(VALID_SURNAME)
                        .email(DEFAULT_EMAIL)
                        .phoneNumber("+344 123456789")
                        .birthDate(VALID_BIRTHDAY)
                        .zipCode(VALID_ZIPCODE)
                        .island(VALID_ISLAND)
                        .twitter(VALID_TWITTER)
                        .instagram(VALID_INSTAGRAM)
                        .additionalInformation(VALID_ADDITIONAL_INFO)
                        .build()
        );
    }

    @Test
    void return_204_when_updates_profile_previously_created() throws Exception {
        testData.createVolunteerWithProfile(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);
        UpdateVolunteerProfileRequestDto updateProfileDto = UpdateVolunteerProfileRequestDto.builder()
                .name(VALID_NAME)
                .surname(VALID_SURNAME)
                .email(DEFAULT_EMAIL)
                .phoneNumber(VALID_PHONE)
                .birthDate(VALID_BIRTHDAY)
                .zipCode(VALID_ZIPCODE)
                .island(VALID_ISLAND)
                .build();

        mvc.perform(post("/api/v1/volunteers/profile")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(updateProfileDto))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isNoContent());

        JpaVolunteer jpaVolunteer = jpaVolunteerRepository.findByEmailWithCredentialAndLocation(DEFAULT_EMAIL);
        assertThat(jpaVolunteer.getProfile().getId()).isNotNull();
        assertThat(jpaVolunteer.getProfile().getName()).isEqualTo(updateProfileDto.getName());
        assertThat(jpaVolunteer.getProfile().getSurname()).isEqualTo(updateProfileDto.getSurname());
        assertThat(jpaVolunteer.getProfile().getBirthDate()).isEqualTo(updateProfileDto.getBirthDate());
        assertThat(jpaVolunteer.getProfile().getPhoneNumber()).isEqualTo(updateProfileDto.getPhoneNumber());
        assertThat(jpaVolunteer.getLocation().getProvince()).isEqualTo(updateProfileDto.getProvince());
        assertThat(jpaVolunteer.getLocation().getZipCode()).isEqualTo(updateProfileDto.getZipCode());
        assertThat(jpaVolunteer.getLocation().getIsland()).isEqualTo(updateProfileDto.getIsland());
        assertThat(jpaVolunteer.getLocation().getTown()).isEqualTo(updateProfileDto.getTown());
        assertThat(jpaVolunteer.getLocation().getAddress()).isEqualTo(updateProfileDto.getAddress());
        assertThat(jpaVolunteer.getProfile().getTwitter()).isEqualTo(updateProfileDto.getTwitter());
        assertThat(jpaVolunteer.getProfile().getLinkedin()).isEqualTo(updateProfileDto.getLinkedin());
        assertThat(jpaVolunteer.getProfile().getInstagram()).isEqualTo(updateProfileDto.getInstagram());
        assertThat(jpaVolunteer.getProfile().getAdditionalInformation()).isEqualTo(updateProfileDto.getAdditionalInformation());
    }

    @ParameterizedTest
    @MethodSource("provideIncorrectProfileInformation")
    void return_400_when_not_provided_correct_information_for_updating_profile(UpdateVolunteerProfileRequestDto profileDto) throws Exception {
        testData.createVolunteer(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        mvc.perform(post("/api/v1/volunteers/profile")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(profileDto))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    private static Stream<UpdateVolunteerProfileRequestDto> provideIncorrectProfileInformation() {
        return Stream.of(
                UpdateVolunteerProfileRequestDto.builder()
                        .name(VALID_NAME)
                        .name(VALID_SURNAME)
                        .email(DEFAULT_EMAIL)
                        .phoneNumber(VALID_PHONE)
                        .birthDate(VALID_BIRTHDAY)
                        .province("Las Palmas")
                        .address("hola2")
                        .island(VALID_ISLAND)
                        .town("hola3")
                        .twitter("twitter")
                        .instagram("instagram")
                        .linkedin("linkedin")
                        .additionalInformation("add")
                        .build(),
                UpdateVolunteerProfileRequestDto.builder()
                        .name(VALID_NAME)
                        .name(VALID_SURNAME)
                        .email(DEFAULT_EMAIL)
                        .phoneNumber(VALID_PHONE)
                        .birthDate(VALID_BIRTHDAY)
                        .zipCode("35100")
                        .twitter("twitter")
                        .instagram("instagram")
                        .linkedin("linkedin")
                        .additionalInformation("add")
                        .build(),
                UpdateVolunteerProfileRequestDto.builder()
                        .name(VALID_NAME)
                        .name(VALID_SURNAME)
                        .email(DEFAULT_EMAIL)
                        .birthDate(VALID_BIRTHDAY)
                        .province("hola1")
                        .address("hola2")
                        .zipCode("35100")
                        .island(VALID_ISLAND)
                        .town("hola3")
                        .twitter("twitter")
                        .instagram("instagram")
                        .linkedin("linkedin")
                        .additionalInformation("add")
                        .build(),
                UpdateVolunteerProfileRequestDto.builder()
                        .name(VALID_NAME)
                        .name(VALID_SURNAME)
                        .email(DEFAULT_EMAIL)
                        .phoneNumber(VALID_PHONE)
                        .birthDate(VALID_BIRTHDAY)
                        .build(),
                UpdateVolunteerProfileRequestDto.builder()
                        .name(VALID_NAME)
                        .name(VALID_SURNAME)
                        .phoneNumber(VALID_PHONE)
                        .birthDate(VALID_BIRTHDAY)
                        .zipCode("35100")
                        .island(VALID_ISLAND)
                        .build(),
                UpdateVolunteerProfileRequestDto.builder()
                        .name(VALID_NAME)
                        .name(VALID_SURNAME)
                        .email(DEFAULT_EMAIL)
                        .phoneNumber(VALID_PHONE)
                        .birthDate(VALID_BIRTHDAY)
                        .zipCode("35100")
                        .island("Islandia")
                        .build(),
                UpdateVolunteerProfileRequestDto.builder()
                        .name(VALID_NAME)
                        .name(VALID_SURNAME)
                        .email(DEFAULT_EMAIL_2)
                        .phoneNumber(VALID_PHONE)
                        .birthDate(VALID_BIRTHDAY)
                        .zipCode("3510055")
                        .island(VALID_ISLAND)
                        .build(),
                UpdateVolunteerProfileRequestDto.builder()
                        .name(VALID_NAME)
                        .name(VALID_SURNAME)
                        .email(DEFAULT_EMAIL)
                        .phoneNumber(VALID_PHONE)
                        .birthDate(VALID_BIRTHDAY)
                        .province("Las Palmas")
                        .address("hola2")
                        .island(VALID_ISLAND)
                        .zipCode("38000")
                        .town("hola")
                        .twitter("https://instagram.com/joselito")
                        .instagram("instagram")
                        .linkedin("linkedin")
                        .additionalInformation("add")
                        .build(),
                UpdateVolunteerProfileRequestDto.builder()
                        .name(VALID_NAME)
                        .name(VALID_SURNAME)
                        .email(DEFAULT_EMAIL_2)
                        .phoneNumber(VALID_PHONE)
                        .birthDate(VALID_BIRTHDAY)
                        .zipCode(VALID_ZIPCODE)
                        .island(VALID_ISLAND)
                        .build(),
                UpdateVolunteerProfileRequestDto.builder()
                        .name(VALID_NAME)
                        .name(VALID_SURNAME)
                        .email(DEFAULT_EMAIL_2)
                        .phoneNumber("+34 12345d789")
                        .birthDate(VALID_BIRTHDAY)
                        .zipCode(VALID_ZIPCODE)
                        .island(VALID_ISLAND)
                        .build(),
                UpdateVolunteerProfileRequestDto.builder()
                        .name(VALID_NAME)
                        .name(VALID_SURNAME)
                        .email(DEFAULT_EMAIL_2)
                        .phoneNumber("34 123456789")
                        .birthDate(VALID_BIRTHDAY)
                        .zipCode(VALID_ZIPCODE)
                        .island(VALID_ISLAND)
                        .build(),
                UpdateVolunteerProfileRequestDto.builder()
                        .name(VALID_NAME)
                        .name(VALID_SURNAME)
                        .email(DEFAULT_EMAIL_2)
                        .phoneNumber("34 12345789101112")
                        .birthDate(VALID_BIRTHDAY)
                        .zipCode(VALID_ZIPCODE)
                        .island(VALID_ISLAND)
                        .build(),
                UpdateVolunteerProfileRequestDto.builder()
                        .name(VALID_NAME)
                        .name(VALID_SURNAME)
                        .email(DEFAULT_EMAIL)
                        .phoneNumber("+344 123456789")
                        .birthDate(VALID_BIRTHDAY)
                        .zipCode("38000")
                        .island(VALID_ISLAND)
                        .twitter("https://twitter.com/home")
                        .instagram("https://www.instagram.com/home")
                        .additionalInformation("01234567890123456789012345678901234567890123456789012345678901234567890" +
                                "12345678901234567890123456789012345678901234567890123456789012345678901234567890123456" +
                                "78901234567890123456789012345678901234567890123456789012345678901234567890123456789012" +
                                "34567890123456789012345678901234567890123456789012345678901234567890123456789012345678" +
                                "90123456789012345678901234567890123456789012345678901234567890123456789012345678901234" +
                                "56789012345678901234567890123456789012345678901234567890123456789012345678901234567890" +
                                "12345678901234567890123456789012345678901234567890123456789012345678901234567890123456" +
                                "7890123456789")
                        .build(),
                UpdateVolunteerProfileRequestDto.builder()
                        .name(VALID_NAME)
                        .name(VALID_SURNAME)
                        .email(DEFAULT_EMAIL)
                        .phoneNumber("+344 123456789")
                        .birthDate(VALID_BIRTHDAY)
                        .zipCode("abcde")
                        .island(VALID_ISLAND)
                        .twitter("https://twitter.com/home")
                        .instagram("https://www.instagram.com/home")
                        .additionalInformation("0123456s")
                        .build(),
                UpdateVolunteerProfileRequestDto.builder()
                        .name(VALID_NAME)
                        .name(VALID_SURNAME)
                        .email(DEFAULT_EMAIL_2)
                        .phoneNumber("+34 123456e789")
                        .birthDate(VALID_BIRTHDAY)
                        .zipCode("35000")
                        .island(VALID_ISLAND)
                        .build()
        );
    }

    @Test
    void return_409_when_provided_new_email_already_bound_to_a_different_account() throws Exception {
        testData.createVolunteer(DEFAULT_ACCOUNT_ID, DEFAULT_EMAIL, DEFAULT_PASSWORD);
        testData.createVolunteer("22222222-2222-2222-2222-222222222222", DEFAULT_EMAIL_2, DEFAULT_PASSWORD);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        UpdateVolunteerProfileRequestDto profileDto = UpdateVolunteerProfileRequestDto.builder()
                .name(VALID_NAME)
                .surname(VALID_SURNAME)
                .email(DEFAULT_EMAIL_2)
                .phoneNumber(VALID_PHONE)
                .birthDate(VALID_BIRTHDAY)
                .province(VALID_PROVINCE)
                .address(VALID_ADDRESS)
                .zipCode(VALID_ZIPCODE)
                .island(VALID_ISLAND)
                .town(VALID_TOWN)
                .twitter(VALID_TWITTER)
                .instagram(VALID_INSTAGRAM)
                .linkedin(VALID_LINKEDIN)
                .additionalInformation(VALID_ADDITIONAL_INFO)
                .build();

        mvc.perform(post("/api/v1/volunteers/profile")
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
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);
        InputStream is = getClass().getClassLoader().getResourceAsStream("images/huellapositiva-logo.png");
        mvc.perform(multipart("/api/v1/volunteers/profile/photo")
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
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        InputStream is = getClass().getClassLoader().getResourceAsStream("images/huellapositiva-logo.png");
        mvc.perform(multipart("/api/v1/volunteers/profile/photo")
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
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        InputStream is = getClass().getClassLoader().getResourceAsStream("images/Sample-png-image-3mb.png");
        mvc.perform(multipart("/api/v1/volunteers/profile/photo")
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
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        InputStream is = getClass().getClassLoader().getResourceAsStream("images/oversized.png");
        mvc.perform(multipart("/api/v1/volunteers/profile/photo")
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
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);
        InputStream is = getClass().getClassLoader().getResourceAsStream("documents/pdf-test.pdf");
        mvc.perform(multipart("/api/v1/volunteers/profile/photo")
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
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);
        mvc.perform(multipart("/api/v1/volunteers/profile/photo")
                .file(new MockMultipartFile("photo", "photo-test.PNG", "image/png", InputStream.nullInputStream()))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .contentType(MULTIPART_FORM_DATA)
                .with(csrf())
                .accept(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void return_204_when_state_of_subscribed_field_changed_successfully() throws Exception {
        testData.createVolunteerWithProfile(DEFAULT_EMAIL, DEFAULT_PASSWORD);

        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);


        mvc.perform(post(SIGN_UP_URL + "/profile/newsletter")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(new UpdateNewsletterSubscriptionDto(Boolean.TRUE)))
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        JpaVolunteer volunteer = jpaVolunteerRepository.findByAccountIdWithCredentialAndLocationAndProfile(DEFAULT_ACCOUNT_ID).orElseThrow();
        assertThat(volunteer.getProfile().isNewsletter()).isTrue();

        mvc.perform(post(SIGN_UP_URL + "/profile/newsletter")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(new UpdateNewsletterSubscriptionDto(Boolean.FALSE)))
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        volunteer = jpaVolunteerRepository.findByAccountIdWithCredentialAndLocationAndProfile(DEFAULT_ACCOUNT_ID).orElseThrow();
        assertThat(volunteer.getProfile().isNewsletter()).isFalse();
    }
}
