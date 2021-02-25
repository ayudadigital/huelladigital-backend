package com.huellapositiva.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huellapositiva.application.dto.*;
import com.huellapositiva.application.exception.UserNotFoundException;
import com.huellapositiva.domain.model.entities.Proposal;
import com.huellapositiva.domain.model.valueobjects.ProposalCategory;
import com.huellapositiva.domain.model.valueobjects.ProposalDate;
import com.huellapositiva.domain.model.valueobjects.Roles;
import com.huellapositiva.domain.repository.ProposalRepository;
import com.huellapositiva.infrastructure.orm.entities.*;
import com.huellapositiva.infrastructure.orm.repository.JpaProposalRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaVolunteerRepository;
import com.huellapositiva.infrastructure.orm.repository.JpaVolunteersProposalsRepository;
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

import javax.persistence.EntityNotFoundException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

import static com.huellapositiva.domain.model.valueobjects.ProposalDate.createClosingProposalDate;
import static com.huellapositiva.domain.model.valueobjects.ProposalStatus.*;
import static com.huellapositiva.util.TestData.*;
import static com.huellapositiva.util.TestUtils.loginAndGetJwtTokens;
import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.DAYS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(TestData.class)
class ProposalControllerShould {

    private static final String REGISTER_PROPOSAL_URI = "/api/v1/proposals";

    private static final String FETCH_PROPOSAL_URI = "/api/v1/proposals/";

    @Autowired
    private TestData testData;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JpaProposalRepository jpaProposalRepository;

    @Autowired
    private JpaVolunteerRepository jpaVolunteerRepository;

    @Autowired
    private JpaVolunteersProposalsRepository jpaVolunteersProposalsRepository;

    @Autowired
    private ProposalRepository proposalRepository;

    @BeforeEach
    void beforeEach() {
        testData.resetData();
    }

    @Test
    void persist_a_proposal() throws Exception {
        // GIVEN
        JpaContactPerson contactPerson = testData.createESALJpaContactPerson(VALID_NAME, VALID_SURNAME, VALID_PHONE, DEFAULT_EMAIL, DEFAULT_PASSWORD);
        testData.createAndLinkESAL(contactPerson, JpaESAL.builder().id(UUID.randomUUID().toString()).name("Huella Positiva").build());
        ProposalRequestDto proposalDto = testData.buildProposalDto();
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        // WHEN
        MockHttpServletResponse response = mvc.perform(multipart(REGISTER_PROPOSAL_URI)
                .file(new MockMultipartFile("file", null, "text/plain", "test data".getBytes()))
                .file(new MockMultipartFile("dto", "dto", "application/json", objectMapper.writeValueAsString(proposalDto).getBytes()))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .with(csrf())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, matchesPattern("\\S+(/api/v1/proposals/)" + UUID_REGEX)))
                .andReturn().getResponse();

        // THEN
        String location = response.getHeader(HttpHeaders.LOCATION);
        String id = location.substring(location.lastIndexOf('/') + 1);
        assertThat(jpaProposalRepository.findByNaturalId(id).get().getTitle()).isEqualTo("Recogida de ropita");
    }

    @Test
    void return_400_when_date_is_invalid_when_creating_a_proposal() throws Exception {
        // GIVEN
        JpaContactPerson contactPerson = testData.createESALJpaContactPerson(VALID_NAME, VALID_SURNAME, VALID_PHONE, DEFAULT_EMAIL, DEFAULT_PASSWORD);
        testData.createAndLinkESAL(contactPerson, JpaESAL.builder().id(UUID.randomUUID().toString()).name("Huella Positiva").build());
        String invalidStartingDate = "20-01-2021";
        ProposalRequestDto proposalDto = ProposalRequestDto.builder()
                .title("Recogida de ropita")
                .province("Santa Cruz de Tenerife")
                .town("Santa Cruz de Tenerife")
                .address("Avenida Weyler 4")
                .island("Tenerife")
                .zipCode("12345")
                .startingProposalDate("21-01-2021")
                .closingProposalDate("24-01-2021")
                .requiredDays("Weekends")
                .minimumAge(18)
                .maximumAge(26)
                .description("Recogida de ropa en la laguna")
                .durationInDays("1 semana")
                .startingVolunteeringDate(invalidStartingDate)
                .category(ProposalCategory.ON_SITE.toString())
                .skills(new String[][]{{"Habilidad", "Descripción"}, {"Negociación", "Saber regatear"}})
                .requirements(new String[]{"Forma física para cargar con la ropa", "Disponibilidad horaria", "Carnet de conducir"})
                .extraInfo("Es recomendable tener ganas de recoger ropa")
                .instructions("Se seleccionarán a los primeros 100 voluntarios")
                .build();
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        // WHEN + THEN
        mvc.perform(multipart(REGISTER_PROPOSAL_URI)
                .file(new MockMultipartFile("file", "fileName", "text/plain", "test data".getBytes()))
                .file(new MockMultipartFile("dto", "dto", "application/json", objectMapper.writeValueAsString(proposalDto).getBytes()))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .with(csrf())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void return_400_when_age_is_out_of_range() throws Exception {
        // GIVEN
        JpaContactPerson contactPerson = testData.createESALJpaContactPerson(VALID_NAME, VALID_SURNAME, VALID_PHONE, DEFAULT_EMAIL, DEFAULT_PASSWORD);
        testData.createAndLinkESAL(contactPerson, JpaESAL.builder().id(UUID.randomUUID().toString()).name("Huella Positiva").build());
        int invalidMinimumAge = 17;
        ProposalRequestDto proposalDto = ProposalRequestDto.builder()
                .title("Recogida de ropita")
                .province("Santa Cruz de Tenerife")
                .town("Santa Cruz de Tenerife")
                .address("Avenida Weyler 4")
                .island("Tenerife")
                .zipCode("12345")
                .startingProposalDate("20-01-2021")
                .closingProposalDate("24-01-2021")
                .requiredDays("Weekends")
                .minimumAge(invalidMinimumAge)
                .maximumAge(26)
                .description("Recogida de ropa en la laguna")
                .durationInDays("1 semana")
                .startingVolunteeringDate("25-01-2021")
                .category(ProposalCategory.ON_SITE.toString())
                .skills(new String[][]{{"Habilidad", "Descripción"}, {"Negociación", "Saber regatear"}})
                .requirements(new String[]{"Forma física para cargar con la ropa", "Disponibilidad horaria", "Carnet de conducir"})
                .extraInfo("Es recomendable tener ganas de recoger ropa")
                .instructions("Se seleccionarán a los primeros 100 voluntarios")
                .build();
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        // WHEN + THEN
        mvc.perform(multipart(REGISTER_PROPOSAL_URI)
                .file(new MockMultipartFile("file", "fileName", "text/plain", "test data".getBytes()))
                .file(new MockMultipartFile("dto", "dto", "application/json", objectMapper.writeValueAsString(proposalDto).getBytes()))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .with(csrf())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void return_400_when_minimum_age_is_greater_than_maximum_age() throws Exception {
        // GIVEN
        JpaContactPerson contactPerson = testData.createESALJpaContactPerson(VALID_NAME, VALID_SURNAME, VALID_PHONE, DEFAULT_EMAIL, DEFAULT_PASSWORD);
        testData.createAndLinkESAL(contactPerson, JpaESAL.builder().id(UUID.randomUUID().toString()).name("Huella Positiva").build());
        int invalidMinimumAge = 30;
        ProposalRequestDto proposalDto = ProposalRequestDto.builder()
                .title("Recogida de ropita")
                .province("Santa Cruz de Tenerife")
                .town("Santa Cruz de Tenerife")
                .address("Avenida Weyler 4")
                .island("Tenerife")
                .zipCode("12345")
                .startingProposalDate("20-01-2021")
                .closingProposalDate("24-01-2021")
                .requiredDays("Weekends")
                .minimumAge(invalidMinimumAge)
                .maximumAge(26)
                .description("Recogida de ropa en la laguna")
                .durationInDays("1 semana")
                .startingVolunteeringDate("25-01-2021")
                .category(ProposalCategory.ON_SITE.toString())
                .skills(new String[][]{{"Habilidad", "Descripción"}, {"Negociación", "Saber regatear"}})
                .requirements(new String[]{"Forma física para cargar con la ropa", "Disponibilidad horaria", "Carnet de conducir"})
                .extraInfo("Es recomendable tener ganas de recoger ropa")
                .instructions("Se seleccionarán a los primeros 100 voluntarios")
                .build();
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        // WHEN + THEN
        mvc.perform(multipart(REGISTER_PROPOSAL_URI)
                .file(new MockMultipartFile("file", "fileName", "text/plain", "test data".getBytes()))
                .file(new MockMultipartFile("dto", "dto", "application/json", objectMapper.writeValueAsString(proposalDto).getBytes()))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .with(csrf())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void fetch_and_return_proposal() throws Exception {
        // GIVEN
        JpaProposal proposal = testData.registerESALAndProposal(PUBLISHED);

        // WHEN
        MockHttpServletResponse fetchResponse = mvc.perform(get(FETCH_PROPOSAL_URI + proposal.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        // THEN
        ProposalResponseDto proposalResponseDto = objectMapper.readValue(fetchResponse.getContentAsString(), ProposalResponseDto.class);
        assertAll(
                () -> assertThat(proposalResponseDto.getTitle()).isEqualTo("Recogida de ropita"),
                () -> assertThat(proposalResponseDto.getSkills()).isNotEmpty(),
                () -> assertThat(proposalResponseDto.getRequirements()).isNotEmpty(),
                () -> assertThatCode(() -> createClosingProposalDate(proposalResponseDto.getClosingProposalDate())).doesNotThrowAnyException()
        );
    }

    @Test
    void return_404_when_fetching_a_non_existent_proposal() throws Exception {
        // GIVEN
        int id = 999;

        // WHEN + THEN
        mvc.perform(get(FETCH_PROPOSAL_URI + id)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void return_404_when_fetching_a_not_published_or_not_finished_proposal() throws Exception {
        // GIVEN
        JpaProposal proposal = testData.registerESALAndProposal(REVIEW_PENDING);

        // WHEN + THEN
        mvc.perform(get(FETCH_PROPOSAL_URI + proposal.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void fetch_a_finished_proposal_but_do_not_list_it() throws Exception {
        // GIVEN
        JpaProposal jpaProposal = testData.registerESALAndProposal(FINISHED);

        // WHEN
        String singleFetchResponse = mvc.perform(get(FETCH_PROPOSAL_URI + jpaProposal.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse()
                .getContentAsString();
        ProposalResponseDto singleProposal = objectMapper.readValue(singleFetchResponse, ProposalResponseDto.class);

        String paginatedFetchResponse = mvc.perform(get(FETCH_PROPOSAL_URI + "/" + 0 + "/" + 1)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse()
                .getContentAsString();
        ListedProposalsDto listedProposals = objectMapper.readValue(paginatedFetchResponse, ListedProposalsDto.class);

        // THEN
        assertAll(
                () -> assertThat(singleProposal.getId()).isEqualTo(jpaProposal.getId()),
                () -> assertThat(listedProposals.getProposals()).isEmpty()
        );
    }

    @Test
    void allow_a_volunteer_to_join() throws Exception {
        // GIVEN
        testData.createVolunteer(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        String proposalId = testData.registerESALandPublishedProposalObject();
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        // WHEN
        mvc.perform(post("/api/v1/proposals/" + proposalId + "/join")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // THEN
        JpaProposal proposal = jpaProposalRepository.findByIdWithOrganizationAndInscribedVolunteers(proposalId).get();
        assertThat(proposal.getInscribedVolunteers()).isNotEmpty();
        String volunteerId = proposal.getInscribedVolunteers().iterator().next().getId();
        assertThat(jpaVolunteerRepository.findByIdWithCredentialsAndRoles(volunteerId).get().getCredential().getEmail()).isEqualTo(DEFAULT_EMAIL);
    }

    @Test
    void return_404_when_joining_a_not_published_proposal() throws Exception {
        // GIVEN
        testData.createVolunteer(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        String proposalId = testData.registerESALAndProposal(REVIEW_PENDING).getId();
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        // WHEN
        mvc.perform(post("/api/v1/proposals/" + proposalId + "/join")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void return_404_when_joining_a_non_existent_proposal() throws Exception {
        // GIVEN
        int id = 999;
        testData.createVolunteer(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        // WHEN + THEN
        mvc.perform(post(FETCH_PROPOSAL_URI + id + "/join")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void return_410_when_joining_a_non_existent_proposal() throws Exception {
        // GIVEN
        testData.createVolunteer(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        JpaProposal proposal = testData.registerESALAndProposal(ENROLLMENT_CLOSED);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        // WHEN
        mvc.perform(post("/api/v1/proposals/" + proposal.getId() + "/join")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isGone());
    }

    @Test
    void create_proposal_as_reviser() throws Exception {
        testData.createCredential(DEFAULT_EMAIL, UUID.randomUUID(), DEFAULT_PASSWORD, Roles.REVISER);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        JpaContactPerson contactPerson = testData.createESALJpaContactPerson(VALID_NAME, VALID_SURNAME, VALID_PHONE, DEFAULT_ESAL_CONTACT_PERSON_EMAIL, DEFAULT_PASSWORD);
        testData.createAndLinkESAL(contactPerson, JpaESAL.builder().id(UUID.randomUUID().toString()).name("Huella Positiva").build());
        ProposalRequestDto proposalDto = testData.buildProposalDto();
        proposalDto.setEsalName("Huella Positiva");

        mvc.perform(multipart("/api/v1/proposals/reviser")
                .file(new MockMultipartFile("file", "fileName", "text/plain", "test data".getBytes()))
                .file(new MockMultipartFile("dto", "dto", "application/json", objectMapper.writeValueAsString(proposalDto).getBytes()))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .with(csrf())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(header().string(HttpHeaders.LOCATION, matchesPattern("\\S+(/api/v1/proposals/)" + UUID_REGEX)))
                .andExpect(status().isCreated());

        assertThat(jpaProposalRepository.findAll()).isNotEmpty();
    }

    @Test
    void return_400_when_multipart_file_is_missing() throws Exception {
        testData.createCredential(DEFAULT_EMAIL, UUID.randomUUID(), DEFAULT_PASSWORD, Roles.REVISER);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        JpaContactPerson contactPerson = testData.createESALJpaContactPerson(VALID_NAME, VALID_SURNAME, VALID_PHONE, DEFAULT_ESAL_CONTACT_PERSON_EMAIL, DEFAULT_PASSWORD);
        testData.createAndLinkESAL(contactPerson, JpaESAL.builder().id(UUID.randomUUID().toString()).name("Huella Positiva").build());
        ProposalRequestDto proposalDto = testData.buildProposalDto();
        proposalDto.setEsalName("Huella Positiva");

        mvc.perform(multipart("/api/v1/proposals/reviser")
                .file(new MockMultipartFile("dto", "dto", "application/json", objectMapper.writeValueAsString(proposalDto).getBytes()))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .with(csrf())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void return_400_when_closing_date_is_more_than_six_months_from_now() throws Exception {
        // GIVEN
        JpaContactPerson contactPerson = testData.createESALJpaContactPerson(VALID_NAME, VALID_SURNAME, VALID_PHONE, DEFAULT_EMAIL, DEFAULT_PASSWORD);
        testData.createAndLinkESAL(contactPerson, JpaESAL.builder().id(UUID.randomUUID().toString()).name("Huella Positiva").build());
        ProposalRequestDto proposalDto = ProposalRequestDto.builder()
                .title("Recogida de ropita")
                .province("Santa Cruz de Tenerife")
                .town("Santa Cruz de Tenerife")
                .address("Avenida Weyler 4")
                .zipCode("12345")
                .island("Tenerife")
                .startingProposalDate("21-08-2030")
                .closingProposalDate("24-08-2030")
                .requiredDays("Weekends")
                .minimumAge(18)
                .maximumAge(26)
                .description("Recogida de ropa en la laguna")
                .durationInDays("1 semana")
                .startingVolunteeringDate("20-08-2030")
                .category(ProposalCategory.ON_SITE.toString())
                .skills(new String[][]{{"Habilidad", "Descripción"}, {"Negociación", "Saber regatear"}})
                .requirements(new String[]{"Forma física para cargar con la ropa", "Disponibilidad horaria", "Carnet de conducir"})
                .extraInfo("Es recomendable tener ganas de recoger ropa")
                .instructions("Se seleccionarán a los primeros 100 voluntarios")
                .build();
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        // WHEN + THEN
        mvc.perform(multipart(REGISTER_PROPOSAL_URI)
                .file(new MockMultipartFile("file", "fileName", "text/plain", "test data".getBytes()))
                .file(new MockMultipartFile("dto", "dto", "application/json", objectMapper.writeValueAsString(proposalDto).getBytes()))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .with(csrf())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void fetch_a_paginated_list_of_published_proposals() throws Exception {
        // GIVEN
        testData.registerESALAndProposal(PUBLISHED);
        JpaESAL different_esal = testData.createJpaESAL(JpaESAL.builder().id(UUID.randomUUID().toString()).name("Different ESAL").build());
        testData.createProposal(JpaProposal.builder()
                .id(UUID.randomUUID().toString())
                .title("Limpieza de playas")
                .location(JpaLocation.builder()
                        .id(UUID.randomUUID().toString())
                        .province("Santa Cruz de Tenerife")
                        .town("Santa Cruz de Tenerife")
                        .address("Avenida Weyler 4")
                        .zipCode("12345")
                        .island("Tenerife").build())
                .esal(different_esal)
                .startingProposalDate(new SimpleDateFormat("dd-MM-yyyy").parse("20-12-2020"))
                .closingProposalDate(new SimpleDateFormat("dd-MM-yyyy").parse("24-12-2020"))
                .startingVolunteeringDate(new SimpleDateFormat("dd-MM-yyyy").parse("25-12-2020"))
                .requiredDays("Weekends")
                .minimumAge(18)
                .maximumAge(26)
                .status(testData.getJpaStatus(PUBLISHED))
                .description("Recogida de ropa en la laguna")
                .durationInDays("1 semana")
                .category(ProposalCategory.ON_SITE.toString())
                .imageUrl(testData.createMockImageUrl().toString())
                .build());

        // WHEN
        String fetchResponse1 = mvc.perform(get(FETCH_PROPOSAL_URI + "/" + 0 + "/" + 1)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse()
                .getContentAsString();
        ListedProposalsDto proposalsFetch1 = objectMapper.readValue(fetchResponse1, ListedProposalsDto.class);

        String fetchResponse2 = mvc.perform(get(FETCH_PROPOSAL_URI + "/" + 1 + "/" + 1)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse()
                .getContentAsString();
        ListedProposalsDto proposalsFetch2 = objectMapper.readValue(fetchResponse2, ListedProposalsDto.class);

        // THEN
        assertAll(
                () -> assertThat(proposalsFetch1.getProposals()).isNotEmpty(),
                () -> assertThat(proposalsFetch2.getProposals()).isNotEmpty(),
                () -> assertThat(proposalsFetch1.getProposals().get(0).getId())
                        .isNotEqualTo(proposalsFetch2.getProposals().get(0).getId())
        );
    }

    @Test
    void return_200_and_an_empty_collection_when_there_is_no_proposals_to_fetch() throws Exception {
        // WHEN
        String fetchResponse = mvc.perform(get(FETCH_PROPOSAL_URI + "/" + 0 + "/" + 1)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse()
                .getContentAsString();
        ListedProposalsDto proposalsFetch = objectMapper.readValue(fetchResponse, ListedProposalsDto.class);

        // THEN
        assertThat(proposalsFetch.getProposals()).isEmpty();
    }

    private static Stream<ProposalRevisionDto> provideProposalRevisionDTO() {
        return Stream.of(
                new ProposalRevisionDto("Deberías profundizar más en la descripción"),
                new ProposalRevisionDto(null)
        );
    }

    @ParameterizedTest
    @MethodSource("provideProposalRevisionDTO")
    void return_200_and_send_an_email_to_contact_person_when_submitting_a_revision_as_reviser(ProposalRevisionDto revisionDto) throws Exception {
        // GIVEN
        JpaProposal jpaProposal = testData.registerESALAndProposal(REVIEW_PENDING);
        String proposalId = jpaProposal.getId();
        testData.createCredential(DEFAULT_EMAIL, UUID.randomUUID(), DEFAULT_PASSWORD, Roles.REVISER);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        // WHEN + THEN
        mvc.perform(post("/api/v1/proposals/revision/" + proposalId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(revisionDto))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        JpaProposal jpaProposalWithStatusChanged = jpaProposalRepository.findByNaturalId(proposalId)
                .orElseThrow(() -> new UserNotFoundException("Proposal not found. Account ID: " + proposalId));
        assertThat(jpaProposalWithStatusChanged.getStatus().getId()).isEqualTo(CHANGES_REQUESTED.getId());
    }

    @Test
    void return_404_when_the_given_proposal_id_does_not_review_pending() throws Exception {
        // GIVEN
        JpaProposal jpaProposal = testData.registerESALAndProposal(PUBLISHED);
        String proposalId = jpaProposal.getId();
        testData.createCredential(DEFAULT_EMAIL, UUID.randomUUID(), DEFAULT_PASSWORD, Roles.REVISER);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);
        ProposalRevisionDto revisionDto = new ProposalRevisionDto(null);

        // WHEN + THEN
        mvc.perform(post("/api/v1/proposals/revision/" + proposalId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(revisionDto))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isPreconditionFailed());
    }

    @Test
    void return_404_when_the_given_proposal_id_does_not_exist() throws Exception {
        // GIVEN
        testData.createCredential(DEFAULT_EMAIL, UUID.randomUUID(), DEFAULT_PASSWORD, Roles.REVISER);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);
        ProposalRevisionDto revisionDto = new ProposalRevisionDto("Deberías profundizar más en la descripción");
        String nonExistingProposalId = "abcdefg";

        // WHEN + THEN
        mvc.perform(post("/api/v1/proposals/revision/" + nonExistingProposalId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(revisionDto))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void list_every_existing_proposal_that_is_not_inadequate_as_reviser() throws Exception {
        // GIVEN
        testData.createCredential(DEFAULT_EMAIL, UUID.randomUUID(), DEFAULT_PASSWORD, Roles.REVISER);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);
        testData.registerESALAndProposal(FINISHED);
        JpaESAL different_esal = testData.createJpaESAL(JpaESAL.builder().id(UUID.randomUUID().toString()).name("Different ESAL").build());
        testData.createProposal(JpaProposal.builder()
                .id(UUID.randomUUID().toString())
                .title("Limpieza de playas")
                .location(JpaLocation.builder()
                        .id(UUID.randomUUID().toString())
                        .province("Santa Cruz de Tenerife")
                        .town("Santa Cruz de Tenerife")
                        .address("Avenida Weyler 4")
                        .zipCode("12345")
                        .island("Tenerife").build())
                .esal(different_esal)
                .startingProposalDate(new SimpleDateFormat("dd-MM-yyyy").parse("20-12-2020"))
                .closingProposalDate(new SimpleDateFormat("dd-MM-yyyy").parse("24-12-2020"))
                .startingVolunteeringDate(new SimpleDateFormat("dd-MM-yyyy").parse("25-12-2020"))
                .requiredDays("Weekends")
                .minimumAge(18)
                .maximumAge(26)
                .status(testData.getJpaStatus(REVIEW_PENDING))
                .description("Recogida de ropa en la laguna")
                .durationInDays("1 semana")
                .category(ProposalCategory.ON_SITE.toString())
                .imageUrl(testData.createMockImageUrl().toString())
                .build());

        // WHEN
        String fetchResponse = mvc.perform(get(FETCH_PROPOSAL_URI + "/" + 0 + "/" + 5 + "/reviser")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse()
                .getContentAsString();
        ListedProposalsDto listedProposals = objectMapper.readValue(fetchResponse, ListedProposalsDto.class);

        assertThat(listedProposals.getProposals().size()).isEqualTo(2);
    }


    @Test
    void return_200_and_a_list_of_volunteers_from_a_proposal_as_reviser() throws Exception {

        // GIVEN
        String proposalId = testData.registerESALAndProposalWithInscribedVolunteers().getId();
        testData.createCredential("revisor@huellapositiva.com", UUID.randomUUID(), DEFAULT_PASSWORD, Roles.REVISER);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, "revisor@huellapositiva.com", DEFAULT_PASSWORD);

        MockHttpServletResponse fetchResponse = mvc.perform(get(FETCH_PROPOSAL_URI + proposalId + "/volunteers")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();


        ArrayList<VolunteerDto> listedVolunteers = objectMapper.readValue(fetchResponse.getContentAsString(), ArrayList.class);
        assertThat(listedVolunteers.size()).isEqualTo(2);

    }

    @Test
    void return_404_when_proposal_not_found() throws Exception{

        // GIVEN
        String proposalId = "5be08393-7a09-465a-931f-239b168c4642";
        testData.createCredential(DEFAULT_EMAIL, UUID.randomUUID(), DEFAULT_PASSWORD, Roles.REVISER);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        // WHEN
        mvc.perform(get(FETCH_PROPOSAL_URI + proposalId + "/volunteers")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void return_200_and_the_proposal_with_volunteers_as_reviser() throws Exception {

        // GIVEN
        String proposalId = testData.registerESALAndProposalWithInscribedVolunteers().getId();
        testData.createCredential("revisor@huellapositiva.com", UUID.randomUUID(), DEFAULT_PASSWORD, Roles.REVISER);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, "revisor@huellapositiva.com", DEFAULT_PASSWORD);

        MockHttpServletResponse fetchResponse = mvc.perform(get(FETCH_PROPOSAL_URI + proposalId + "/proposal")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        ProposalResponseDto proposalDto = objectMapper.readValue(fetchResponse.getContentAsString(), ProposalResponseDto.class);
        assertThat(proposalDto.getId()).isEqualTo(proposalId);
        assertThat(proposalDto.getInscribedVolunteers().size()).isPositive();

    }

    @Test
    void return_404_when_proposal_with_volunteers_not_found() throws Exception{

        // GIVEN
        String proposalId = "999";
        testData.createCredential(DEFAULT_EMAIL, UUID.randomUUID(), DEFAULT_PASSWORD, Roles.REVISER);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_EMAIL, DEFAULT_PASSWORD);

        // WHEN
        mvc.perform(get(FETCH_PROPOSAL_URI + proposalId + "/proposal")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void return_204_when_cancel_a_proposal_successfully() throws Exception {
        //GIVEN
        String proposalId = testData.registerESALAndProposalWithInscribedVolunteers().getId();
        testData.createCredential("revisor@huellapositiva.com", UUID.randomUUID(), DEFAULT_PASSWORD, Roles.REVISER);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, "revisor@huellapositiva.com", DEFAULT_PASSWORD);

        //WHEN + THEN
        mvc.perform(post(FETCH_PROPOSAL_URI + proposalId + "/cancel")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(new ProposalCancelReasonDto(DEFAULT_CANCEL_REASON)))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andReturn().getResponse();

        JpaProposal jpaProposal = jpaProposalRepository.findByNaturalId(proposalId).orElseThrow(EntityNotFoundException::new);
        assertThat(jpaProposal.getStatus().getId()).isEqualTo(CANCELLED.getId());
        assertThat(jpaProposal.getCancelReason()).isEqualTo(DEFAULT_CANCEL_REASON);
    }

    @Test
    void return_412_when_cancel_a_finished_proposal() throws Exception {
        //GIVEN
        String proposalId = testData.registerESALAndProposal(FINISHED).getId();
        testData.createCredential("revisor@huellapositiva.com", UUID.randomUUID(), DEFAULT_PASSWORD, Roles.REVISER);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, "revisor@huellapositiva.com", DEFAULT_PASSWORD);

        //WHEN + THEN
        mvc.perform(post(FETCH_PROPOSAL_URI + proposalId + "/cancel")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(new ProposalCancelReasonDto(DEFAULT_CANCEL_REASON)))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isPreconditionFailed())
                .andReturn().getResponse();

        JpaProposal jpaProposal = jpaProposalRepository.findByNaturalId(proposalId).orElseThrow(EntityNotFoundException::new);
        assertThat(jpaProposal.getStatus().getId()).isEqualTo(FINISHED.getId());
    }

    @Test
    void return_412_when_cancel_an_inadequate_proposal() throws Exception {
        //GIVEN
        String proposalId = testData.registerESALAndProposal(INADEQUATE).getId();
        testData.createCredential("revisor@huellapositiva.com", UUID.randomUUID(), DEFAULT_PASSWORD, Roles.REVISER);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, "revisor@huellapositiva.com", DEFAULT_PASSWORD);

        //WHEN + THEN
        mvc.perform(post(FETCH_PROPOSAL_URI + proposalId + "/cancel")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(new ProposalCancelReasonDto(DEFAULT_CANCEL_REASON)))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isPreconditionFailed())
                .andReturn().getResponse();

        JpaProposal jpaProposal = jpaProposalRepository.findByNaturalId(proposalId).orElseThrow(EntityNotFoundException::new);
        assertThat(jpaProposal.getStatus().getId()).isEqualTo(INADEQUATE.getId());
    }

    @Test
    void return_412_when_cancel_an_already_cancelled_proposal() throws Exception {
        //GIVEN
        String proposalId = testData.registerESALAndProposal(CANCELLED).getId();
        testData.createCredential("revisor@huellapositiva.com", UUID.randomUUID(), DEFAULT_PASSWORD, Roles.REVISER);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, "revisor@huellapositiva.com", DEFAULT_PASSWORD);

        //WHEN + THEN
        mvc.perform(post(FETCH_PROPOSAL_URI + proposalId + "/cancel")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(new ProposalCancelReasonDto(DEFAULT_CANCEL_REASON)))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isPreconditionFailed())
                .andReturn().getResponse();

        JpaProposal jpaProposal = jpaProposalRepository.findByNaturalId(proposalId).orElseThrow(EntityNotFoundException::new);
        assertThat(jpaProposal.getStatus().getId()).isEqualTo(CANCELLED.getId());
    }

    @Test
    void return_404_when_trying_to_cancel_a_non_existing_proposal() throws Exception {
        //GIVEN
        String proposalId = "999";
        testData.createCredential("revisor@huellapositiva.com", UUID.randomUUID(), DEFAULT_PASSWORD, Roles.REVISER);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, "revisor@huellapositiva.com", DEFAULT_PASSWORD);

        //WHEN + THEN
        mvc.perform(post(FETCH_PROPOSAL_URI + proposalId + "/cancel")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(new ProposalCancelReasonDto(DEFAULT_CANCEL_REASON)))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();
    }

    @Test
    void return_204_when_change_status_rejected_or_confirmed() throws Exception {
        // GIVEN
        testData.registerESALAndProposalWithInscribedVolunteers();

        // WHEN
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_ESAL_CONTACT_PERSON_EMAIL, DEFAULT_PASSWORD);
        List<ChangeStatusVolunteerDto> changeStatusVolunteerDtos = new ArrayList<>();
        List<JpaVolunteerProposal> jpaVolunteerProposals = jpaVolunteersProposalsRepository.findAll();
        for(JpaVolunteerProposal volunteerProposal : jpaVolunteerProposals){
            if(changeStatusVolunteerDtos.isEmpty()) {
                changeStatusVolunteerDtos.add(new ChangeStatusVolunteerDto(volunteerProposal.getProposal(), volunteerProposal.getVolunteer().getId(), false));
            } else {
                changeStatusVolunteerDtos.add(new ChangeStatusVolunteerDto(volunteerProposal.getProposal(), volunteerProposal.getVolunteer().getId(), true));
            }
        }

        // THEN
        mvc.perform(post(FETCH_PROPOSAL_URI + "changeStatusVolunteerProposal")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(changeStatusVolunteerDtos))
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        List<JpaVolunteerProposal> volunteersProposalsModified = jpaVolunteersProposalsRepository.findAll();
        assertThat(volunteersProposalsModified.get(0).isConfirmed()).isFalse();
        assertThat(volunteersProposalsModified.get(1).isConfirmed()).isTrue();
    }

    @ParameterizedTest
    @MethodSource("provideCorrectProposalInformation")
    void return_200_when_updates_proposal_and_change_status_to_review_pending(UpdateProposalRequestDto.UpdateProposalRequestDtoBuilder updateProposalRequestDtoBuilder) throws Exception {
        // GIVEN
        JpaProposal jpaProposal = testData.registerESALAndProposal(REVIEW_PENDING);
        JwtResponseDto jwtResponseDto = loginAndGetJwtTokens(mvc, DEFAULT_ESAL_CONTACT_PERSON_EMAIL, DEFAULT_PASSWORD);

        // WHEN
        UpdateProposalRequestDto updateProposalRequestDto = updateProposalRequestDtoBuilder.id(jpaProposal.getId()).build();

        // THEN
        mvc.perform(post(FETCH_PROPOSAL_URI + "/updateProposal")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtResponseDto.getAccessToken())
                .content(objectMapper.writeValueAsString(updateProposalRequestDto))
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        Proposal proposal = proposalRepository.fetch(jpaProposal.getId());
        assertThat(proposal.getTitle()).isEqualTo(updateProposalRequestDto.getTitle());
        assertThat(proposal.getLocation().getProvince()).isEqualTo(updateProposalRequestDto.getProvince());
        assertThat(proposal.getLocation().getTown()).isEqualTo(updateProposalRequestDto.getTown());
        assertThat(proposal.getLocation().getAddress()).isEqualTo(updateProposalRequestDto.getAddress());
        assertThat(proposal.getLocation().getIsland()).isEqualTo(updateProposalRequestDto.getIsland());
        assertThat(proposal.getLocation().getZipCode()).isEqualTo(updateProposalRequestDto.getZipCode());
        assertThat(proposal.getRequiredDays()).isEqualTo(updateProposalRequestDto.getRequiredDays());
        assertThat(proposal.getPermittedAgeRange().getMinimum()).isEqualTo(updateProposalRequestDto.getMinimumAge());
        assertThat(proposal.getPermittedAgeRange().getMaximum()).isEqualTo(updateProposalRequestDto.getMaximumAge());
        if (updateProposalRequestDto.getStartingProposalDate() != null) {
            assertThat(proposal.getStartingProposalDate().toString()).isEqualTo(new ProposalDate(new SimpleDateFormat("yyyy-MM-dd").parse(updateProposalRequestDto.getStartingProposalDate().toString())).toString());
        }
        assertThat(proposal.getClosingProposalDate().toString()).isEqualTo(new ProposalDate(new SimpleDateFormat("yyyy-MM-dd").parse(updateProposalRequestDto.getClosingProposalDate().toString())).toString());
        assertThat(proposal.getStartingVolunteeringDate().toString()).isEqualTo(new ProposalDate(new SimpleDateFormat("yyyy-MM-dd").parse(updateProposalRequestDto.getStartingVolunteeringDate().toString())).toString());
        //System.out.println("Hola");
    }

    private static Stream<UpdateProposalRequestDto.UpdateProposalRequestDtoBuilder> provideCorrectProposalInformation() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return Stream.of(
                UpdateProposalRequestDto.builder()
                        .title("Titulo menor a 75 caracteres")
                        .province(VALID_PROVINCE)
                        .town(VALID_TOWN)
                        .address(VALID_ADDRESS)
                        .island(VALID_ISLAND)
                        .zipCode(VALID_ZIPCODE)
                        .requiredDays("Weekend")
                        .minimumAge(18)
                        .maximumAge(55)
                        .startingProposalDate(LocalDate.parse(simpleDateFormat.format(Date.from(now().plus(5, DAYS)))))
                        .closingProposalDate(LocalDate.parse(simpleDateFormat.format(Date.from(now().plus(10, DAYS)))))
                        .startingVolunteeringDate(LocalDate.parse(simpleDateFormat.format(Date.from(now().plus(15, DAYS)))))
                        .description("Una descripcion menor de 200 caracteres")
                        .durationInDays("5")
                        .category("MIXED")
                        .skills(new String[][]{{"Comunicador", "Excelente comunicador"},{"Guapo", "La belleza por delante"}})
                        .requirements(new String[]{"Traer DNI"})
                        .extraInfo("Una extra info menor de 200 caracteres")
                        .instructions("Una instructions menor de 200 caracteres"),
                UpdateProposalRequestDto.builder()
                        .title("Titulo menor a 75 caracteres")
                        .province(VALID_PROVINCE)
                        .town(VALID_TOWN)
                        .address(VALID_ADDRESS)
                        .island(VALID_ISLAND)
                        .zipCode(VALID_ZIPCODE)
                        .requiredDays("Weekend")
                        .minimumAge(18)
                        .maximumAge(55)
                        .closingProposalDate(LocalDate.parse(simpleDateFormat.format(Date.from(now().plus(10, DAYS)))))
                        .startingVolunteeringDate(LocalDate.parse(simpleDateFormat.format(Date.from(now().plus(15, DAYS)))))
                        .description("Una descripcion menor de 200 caracteres")
                        .durationInDays("5")
                        .category("MIXED")
        );
    }
}