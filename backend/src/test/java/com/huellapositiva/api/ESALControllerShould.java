package com.huellapositiva.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huellapositiva.application.dto.*;
import com.huellapositiva.domain.model.entities.ESAL;
import com.huellapositiva.domain.model.valueobjects.Roles;
import com.huellapositiva.infrastructure.orm.entities.JpaContactPerson;
import com.huellapositiva.infrastructure.orm.entities.JpaESAL;
import com.huellapositiva.infrastructure.orm.repository.JpaESALRepository;
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
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import javax.persistence.EntityNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static com.huellapositiva.util.TestData.*;
import static com.huellapositiva.util.TestUtils.loginAndGetJwtTokens;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(TestData.class)
class ESALControllerShould {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private TestData testData;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JpaESALRepository jpaESALRepository;

    @BeforeEach
    void beforeEach() {
        testData.resetData();
    }

    private ESALRequestDto getESALRequestDto() {
        return ESALRequestDto.builder()
                .name("Huella Positiva")
                .website("http://website.com")
                .description("description")
                .dataProtectionPolicy(true)
                .entityType("FOUNDATION")
                .island("Gran Canaria")
                .zipCode("36000")
                .privacyPolicy(true)
                .registeredEntity(true)
                .build();
    }

    private UpdateESALDto getUpdateESALDto(){
        return UpdateESALDto.builder()
                .name(DEFAULT_ESAL)
                .website("http://website.es")
                .description("another description")
                .entityType(DEFAULT_ENTITY_TYPE)
                .island(VALID_ISLAND)
                .zipCode(VALID_ZIPCODE)
                .province(VALID_PROVINCE)
                .town(VALID_TOWN)
                .address(VALID_ADDRESS)
                .build();
    }

    @Test
    void create_an_ESAL_and_update_member_joined_ESAL() throws Exception {
        testData.createESALJpaContactPerson(VALID_NAME, VALID_SURNAME, VALID_PHONE, DEFAULT_EMAIL, DEFAULT_PASSWORD);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        ESALRequestDto esalRequestDto = getESALRequestDto();
        mvc.perform(post("/api/v1/esal")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(esalRequestDto))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk());

        JpaESAL jpaESAL = jpaESALRepository.findByName(esalRequestDto.getName())
                .orElseThrow(EntityNotFoundException::new);
        assertThat(jpaESAL.getWebsite()).isEqualTo(esalRequestDto.getWebsite());
        assertThat(jpaESAL.getDescription()).isEqualTo(esalRequestDto.getDescription());
        assertThat(jpaESAL.isRegisteredEntity()).isEqualTo(esalRequestDto.isRegisteredEntity());
        assertThat(jpaESAL.getEntityType()).isEqualTo(esalRequestDto.getEntityType());
        assertThat(jpaESAL.getLocation().getIsland()).isEqualTo(esalRequestDto.getIsland());
        assertThat(jpaESAL.getLocation().getZipCode()).isEqualTo(esalRequestDto.getZipCode());
        assertThat(jpaESAL.isDataProtectionPolicy()).isEqualTo(esalRequestDto.isDataProtectionPolicy());
        assertThat(jpaESAL.isPrivacyPolicy()).isEqualTo(esalRequestDto.isPrivacyPolicy());
    }

    @Test
    void create_an_ESAL_as_a_not_confirmed_contact_person() throws Exception {
        testData.createESALJpaContactPerson(VALID_NAME, VALID_SURNAME, VALID_PHONE, DEFAULT_EMAIL, DEFAULT_PASSWORD, Roles.CONTACT_PERSON_NOT_CONFIRMED);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        ESALRequestDto esalRequestDto = getESALRequestDto();
        mvc.perform(post("/api/v1/esal")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(esalRequestDto))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk());
        assertThat(jpaESALRepository.findByName(esalRequestDto.getName())).isPresent();
    }

    @Test
    void return_200_when_get_esal_information_successfully() throws Exception {
        JpaContactPerson contactPerson = testData.createESALJpaContactPerson(VALID_NAME, VALID_SURNAME, VALID_PHONE, DEFAULT_EMAIL, DEFAULT_PASSWORD);
        String esalId = testData.createAndLinkESAL(contactPerson, testData.buildJpaESAL(DEFAULT_ESAL));
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        MockHttpServletResponse response = mvc.perform(get("/api/v1/esal/"+esalId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .with(csrf())
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        GetESALResponseDto esalResponseDto = objectMapper.readValue(response.getContentAsString(), GetESALResponseDto.class);
        JpaESAL jpaESAL = jpaESALRepository.findByNaturalId(esalId).orElseThrow(EntityNotFoundException::new);
        assertThat(jpaESAL.getName()).isEqualTo(esalResponseDto.getName());
        assertThat(jpaESAL.getDescription()).isEqualTo(esalResponseDto.getDescription());
        assertThat(jpaESAL.getWebsite()).isEqualTo(esalResponseDto.getWebsite());
        assertThat(jpaESAL.isRegisteredEntity()).isEqualTo(esalResponseDto.isRegisteredEntity());
        assertThat(jpaESAL.getEntityType()).isEqualTo(esalResponseDto.getEntityType());
        assertThat(jpaESAL.getLocation().getIsland()).isEqualTo(esalResponseDto.getIsland());
        assertThat(jpaESAL.getLocation().getZipCode()).isEqualTo(esalResponseDto.getZipCode());
        assertThat(jpaESAL.getLocation().getProvince()).isEqualTo(esalResponseDto.getProvince());
        assertThat(jpaESAL.getLocation().getTown()).isEqualTo(esalResponseDto.getTown());
        assertThat(jpaESAL.getLocation().getAddress()).isEqualTo(esalResponseDto.getAddress());
    }

    @Test
    void return_403_when_getting_esal_information_the_contact_person_does_not_own() throws Exception {
        ESAL esal = testData.createESAL(DEFAULT_ESAL);
        testData.createESALJpaContactPerson(VALID_NAME, VALID_SURNAME, VALID_PHONE, DEFAULT_EMAIL, DEFAULT_PASSWORD);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        mvc.perform(get("/api/v1/esal/"+esal.getId().getValue())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .with(csrf())
                .accept(APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void return_200_when_updating_an_esal_successfully() throws Exception {
        JpaContactPerson contactPerson = testData.createESALJpaContactPerson(VALID_NAME, VALID_SURNAME, VALID_PHONE, DEFAULT_EMAIL, DEFAULT_PASSWORD);
        testData.createAndLinkESAL(contactPerson, testData.buildJpaESAL(DEFAULT_ESAL));
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        UpdateESALDto updateESALDto = getUpdateESALDto();
        mvc.perform(put("/api/v1/esal/update")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(updateESALDto))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk());

        JpaESAL jpaESAL = jpaESALRepository.findByName(updateESALDto.getName()).orElseThrow(EntityNotFoundException::new);
        assertThat(jpaESAL.getName()).isEqualTo(updateESALDto.getName());
        assertThat(jpaESAL.getDescription()).isEqualTo(updateESALDto.getDescription());
        assertThat(jpaESAL.getWebsite()).isEqualTo(updateESALDto.getWebsite());
        assertThat(jpaESAL.isRegisteredEntity()).isEqualTo(updateESALDto.isRegisteredEntity());
        assertThat(jpaESAL.getEntityType()).isEqualTo(updateESALDto.getEntityType());
        assertThat(jpaESAL.getLocation().getIsland()).isEqualTo(updateESALDto.getIsland());
        assertThat(jpaESAL.getLocation().getZipCode()).isEqualTo(updateESALDto.getZipCode());
        assertThat(jpaESAL.getLocation().getProvince()).isEqualTo(updateESALDto.getProvince());
        assertThat(jpaESAL.getLocation().getTown()).isEqualTo(updateESALDto.getTown());
        assertThat(jpaESAL.getLocation().getAddress()).isEqualTo(updateESALDto.getAddress());
    }

    @ParameterizedTest
    @MethodSource("provideIncorrectEsalInformation")
    void return_400_when_provided_incorrect_information_for_updating_esal(UpdateESALDto dto) throws Exception {
        JpaContactPerson contactPerson = testData.createESALJpaContactPerson(VALID_NAME, VALID_SURNAME, VALID_PHONE, DEFAULT_EMAIL, DEFAULT_PASSWORD);
        testData.createAndLinkESAL(contactPerson, testData.buildJpaESAL(DEFAULT_ESAL));
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        mvc.perform(put("/api/v1/esal/update")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(dto))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    private static Stream<UpdateESALDto> provideIncorrectEsalInformation() {
        return Stream.of(
                UpdateESALDto.builder()
                        .name("")
                        .website("http://website.es")
                        .description("another description")
                        .entityType(DEFAULT_ENTITY_TYPE)
                        .island(VALID_ISLAND)
                        .zipCode(VALID_ZIPCODE)
                        .province(VALID_PROVINCE)
                        .town(VALID_TOWN)
                        .address(VALID_ADDRESS)
                        .build(),
                UpdateESALDto.builder()
                        .name(DEFAULT_ESAL)
                        .website("")
                        .description("another description")
                        .entityType(DEFAULT_ENTITY_TYPE)
                        .island(VALID_ISLAND)
                        .zipCode(VALID_ZIPCODE)
                        .province(VALID_PROVINCE)
                        .town(VALID_TOWN)
                        .address(VALID_ADDRESS)
                        .build(),
                UpdateESALDto.builder()
                        .name(DEFAULT_ESAL)
                        .website("http://website.es")
                        .description("another description")
                        .entityType("FUNDACION")
                        .island("")
                        .zipCode(VALID_ZIPCODE)
                        .province(VALID_PROVINCE)
                        .town(VALID_TOWN)
                        .address(VALID_ADDRESS)
                        .build(),
                UpdateESALDto.builder()
                        .name(DEFAULT_ESAL)
                        .website("http://website.es")
                        .description("another description")
                        .entityType(DEFAULT_ENTITY_TYPE)
                        .island(VALID_ISLAND)
                        .zipCode("3555555")
                        .province(VALID_PROVINCE)
                        .town(VALID_TOWN)
                        .address(VALID_ADDRESS)
                        .build()

        );
    }

    @Test
    void return_409_when_updating_esal_name_to_an_already_existing_esal() throws Exception {
        JpaContactPerson contactPerson = testData.createESALJpaContactPerson(VALID_NAME, VALID_SURNAME, VALID_PHONE, DEFAULT_EMAIL, DEFAULT_PASSWORD);
        testData.createAndLinkESAL(contactPerson, testData.buildJpaESAL(DEFAULT_ESAL));
        testData.createESAL("Huella Positiva");
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        UpdateESALDto updateESALDto = UpdateESALDto.builder()
                .name("Huella Positiva")
                .website("http://website.es")
                .description("another description")
                .entityType(DEFAULT_ENTITY_TYPE)
                .island(VALID_ISLAND)
                .zipCode(VALID_ZIPCODE)
                .province(VALID_PROVINCE)
                .town(VALID_TOWN)
                .address(VALID_ADDRESS)
                .build();

        mvc.perform(put("/api/v1/esal/update")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(updateESALDto))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    void return_204_when_upload_logo_successfully() throws Exception {
        JpaContactPerson contactPerson = testData.createESALJpaContactPerson(VALID_NAME, VALID_SURNAME, VALID_PHONE, DEFAULT_EMAIL, DEFAULT_PASSWORD);
        testData.createAndLinkESAL(contactPerson, testData.buildJpaESAL(DEFAULT_ESAL));
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        InputStream is = getClass().getClassLoader().getResourceAsStream("images/huellapositiva-logo.png");
        mvc.perform(multipart("/api/v1/esal/logo")
                .file(new MockMultipartFile("logo", "huellapositiva-logo.png", "image/png", is))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .contentType(MULTIPART_FORM_DATA)
                .with(csrf())
                .accept(APPLICATION_JSON))
                .andExpect(status().isNoContent());

        JpaESAL esal = jpaESALRepository.findByName(DEFAULT_ESAL).orElseThrow(EntityNotFoundException::new);
        assertThat(esal.getLogoUrl()).isNotNull();
    }

    @ParameterizedTest
    @MethodSource("provideInvalidImages")
    void return_400_when_the_photo_uploaded_is_too_big(List<String> logoURI) throws Exception {
        JpaContactPerson contactPerson = testData.createESALJpaContactPerson(VALID_NAME, VALID_SURNAME, VALID_PHONE, DEFAULT_EMAIL, DEFAULT_PASSWORD);
        testData.createAndLinkESAL(contactPerson, testData.buildJpaESAL(DEFAULT_ESAL));
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        MockMultipartFile file = new MockMultipartFile("logo", logoURI.get(0),
                logoURI.get(1), getClass().getClassLoader().getResourceAsStream(logoURI.get(0)));
        mvc.perform(multipart("/api/v1/esal/logo")
                .file(file)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .contentType(MULTIPART_FORM_DATA)
                .with(csrf())
                .accept(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    private static Stream<List<String>> provideInvalidImages(){
        return Stream.of(
                List.of("images/Sample-png-image-3mb.png","image/png"),
                List.of("images/oversized.png","image/png"),
                List.of("documents/pdf-test.pdf","application/pdf")
        );
    }

    @Test
    void return_400_when_there_is_not_photo_uploaded() throws Exception {
        JpaContactPerson contactPerson = testData.createESALJpaContactPerson(VALID_NAME, VALID_SURNAME, VALID_PHONE, DEFAULT_EMAIL, DEFAULT_PASSWORD);
        testData.createAndLinkESAL(contactPerson, testData.buildJpaESAL(DEFAULT_ESAL));
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        mvc.perform(multipart("/api/v1/esal/logo")
                .file(new MockMultipartFile("logo", "photo-test.PNG", "image/png", InputStream.nullInputStream()))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .contentType(MULTIPART_FORM_DATA)
                .with(csrf())
                .accept(APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_an_ESAL_as_reviser() throws Exception {
        testData.createCredential(DEFAULT_EMAIL, UUID.randomUUID(), DEFAULT_PASSWORD, Roles.REVISER);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        ESALRequestDto esalRequestDto = getESALRequestDto();
        mvc.perform(post("/api/v1/esal/reviser")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(esalRequestDto))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk());

        JpaESAL jpaESAL = jpaESALRepository.findByName(esalRequestDto.getName())
                .orElseThrow(EntityNotFoundException::new);
        assertThat(jpaESAL.getWebsite()).isEqualTo(esalRequestDto.getWebsite());
        assertThat(jpaESAL.getDescription()).isEqualTo(esalRequestDto.getDescription());
        assertThat(jpaESAL.isRegisteredEntity()).isEqualTo(esalRequestDto.isRegisteredEntity());
        assertThat(jpaESAL.getEntityType()).isEqualTo(esalRequestDto.getEntityType());
        assertThat(jpaESAL.getLocation().getIsland()).isEqualTo(esalRequestDto.getIsland());
        assertThat(jpaESAL.getLocation().getZipCode()).isEqualTo(esalRequestDto.getZipCode());
        assertThat(jpaESAL.isDataProtectionPolicy()).isEqualTo(esalRequestDto.isDataProtectionPolicy());
        assertThat(jpaESAL.isPrivacyPolicy()).isEqualTo(esalRequestDto.isPrivacyPolicy());
    }

    @Test
    void allow_members_to_delete_their_ESAL() throws Exception {
        JpaContactPerson contactPerson = testData.createESALJpaContactPerson(VALID_NAME, VALID_SURNAME, VALID_PHONE, DEFAULT_EMAIL, DEFAULT_PASSWORD);
        String esalId = testData.createAndLinkESAL(contactPerson, testData.buildJpaESAL(DEFAULT_ESAL));
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        mvc.perform(delete("/api/v1/esal/" + esalId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isOk());

        assertThat(jpaESALRepository.findByNaturalId(esalId)).isEmpty();
    }

    @Test
    void not_allow_to_create_an_ESAL_when_member_already_has_one() throws Exception {
        JpaContactPerson member = testData.createESALJpaContactPerson(VALID_NAME, VALID_SURNAME, VALID_PHONE, DEFAULT_EMAIL, DEFAULT_PASSWORD);
        testData.createAndLinkESAL(member, testData.buildJpaESAL("Huella Negativa"));
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        mvc.perform(post("/api/v1/esal")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(getESALRequestDto()))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isPreconditionFailed());
    }

    @Test
    void return_409_when_ESAL_is_already_taken() throws Exception {
        testData.createESALJpaContactPerson(VALID_NAME, VALID_SURNAME, VALID_PHONE, DEFAULT_EMAIL, DEFAULT_PASSWORD);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);
        testData.createJpaESAL(testData.buildJpaESAL("Huella Positiva"));

        mvc.perform(post("/api/v1/esal")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(getESALRequestDto()))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    void return_403_when_a_user_attempts_to_delete_an_ESAL_that_does_not_belong_to() throws Exception {
        JpaContactPerson contactPerson = testData.createESALJpaContactPerson(VALID_NAME, VALID_SURNAME, VALID_PHONE, DEFAULT_EMAIL, DEFAULT_PASSWORD);
        testData.createAndLinkESAL(contactPerson, testData.buildJpaESAL("Huella Positiva"));
        String secondESALId = testData.createJpaESAL(testData.buildJpaESAL("Huella Negativa")).getId();
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        mvc.perform(delete("/api/v1/esal/" + secondESALId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
